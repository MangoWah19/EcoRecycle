package com.example.fyp1.api

import android.content.Context
import com.example.fyp1.LeaderboardResponse
import com.example.fyp1.RecyclingLog
import com.example.fyp1.Redemption
import com.example.fyp1.offline.CachedEarnedBadgeEntity
import com.example.fyp1.offline.CachedLeaderboardRankEntity
import com.example.fyp1.offline.LocalNotificationEntity
import com.example.fyp1.offline.OfflineDatabase
import com.example.fyp1.offline.toCachedRecyclingEntity
import com.example.fyp1.offline.toCachedRedemptionEntity
import com.example.fyp1.notifications.EcoReminderScheduler
import kotlinx.coroutines.flow.Flow

class NotificationRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao = OfflineDatabase.get(appContext).offlineDao()
    private val sessionManager = AuthSessionManager(appContext)

    fun observeNotifications(): Flow<List<LocalNotificationEntity>> = dao.observeNotifications()

    fun observeUnreadCount(): Flow<Int> = dao.observeUnreadNotificationCount()

    suspend fun markAsRead(id: String) {
        dao.markNotificationRead(id)
    }

    suspend fun markAllAsRead() {
        dao.markAllNotificationsRead()
    }

    suspend fun notifyMissionSubmissionChanges(submissions: List<BackendSubmission>) {
        val ids = submissions.map { it.id }
        if (ids.isEmpty()) return

        val previousById = dao.getCachedSubmissionsByIds(ids).associateBy { it.id }
        submissions.forEach { submission ->
            val previous = previousById[submission.id] ?: return@forEach
            if (previous.status.equals(submission.status, ignoreCase = true)) return@forEach

            val missionTitle = submission.mission?.title ?: "your mission"
            val status = submission.status.uppercase()
            val title = when (status) {
                "APPROVED" -> "Mission Approved"
                "REJECTED" -> "Mission Needs Review"
                "PENDING_REVIEW" -> "Mission Submitted"
                "ONGOING" -> "Mission Joined"
                else -> "Mission Updated"
            }
            val message = when (status) {
                "APPROVED" -> {
                    val points = submission.mission?.points ?: 0
                    if (points > 0) {
                        "Your $missionTitle proof was approved. +$points points earned."
                    } else {
                        "Your $missionTitle proof was approved."
                    }
                }
                "REJECTED" -> "Your $missionTitle proof was not approved. Please check the review note."
                "PENDING_REVIEW" -> "Your $missionTitle proof is waiting for review."
                "ONGOING" -> "You joined $missionTitle. Submit proof when you are ready."
                else -> "$missionTitle is now ${submission.status.toReadableStatus()}."
            }
            insertNotification(
                id = "mission-${submission.id}-$status",
                category = NotificationCategory.Mission,
                title = title,
                message = message,
                sourceId = submission.id
            )
        }
    }

    suspend fun notifyRecyclingSubmissionChanges(submissions: List<RecyclingLog>) {
        val cacheable = submissions.mapNotNull { it.toCachedRecyclingEntity() }
        if (cacheable.isEmpty()) return

        val previousById = dao.getCachedRecyclingSubmissionsByIds(cacheable.map { it.id }).associateBy { it.id }
        cacheable.forEach { submission ->
            val previous = previousById[submission.id]
            if (previous == null || previous.status.equals(submission.status, ignoreCase = true)) return@forEach

            val status = submission.status.uppercase()
            val material = submission.materialType.ifBlank { "recycling" }
            val title = when (status) {
                "APPROVED" -> "Recycling Approved"
                "REJECTED" -> "Recycling Needs Review"
                "PENDING", "PENDING_REVIEW" -> "Recycling Submitted"
                else -> "Recycling Updated"
            }
            val pointsText = if (submission.pointsAwarded > 0) " +${submission.pointsAwarded} points awarded." else ""
            val message = when (status) {
                "APPROVED" -> "Your $material deposit was approved.$pointsText"
                "REJECTED" -> "Your $material deposit was not approved. Please check the review note."
                "PENDING", "PENDING_REVIEW" -> "Your $material deposit is waiting for review."
                else -> "Your $material deposit is now ${submission.status.toReadableStatus()}."
            }
            insertNotification(
                id = "recycling-${submission.id}-$status",
                category = NotificationCategory.Recycling,
                title = title,
                message = message,
                sourceId = submission.id
            )
        }

        dao.upsertRecyclingSubmissions(cacheable)
    }

    suspend fun recordRecyclingSubmitted(submission: RecyclingLog, qrBased: Boolean = false) {
        val cacheable = submission.toCachedRecyclingEntity() ?: return
        dao.upsertRecyclingSubmissions(listOf(cacheable))
    }

    suspend fun notifyPointChanges(pointsData: PointsData) {
        val oldBalance = dao.getCachedPointBalance()
        val oldEventIds = dao.getCachedPointEvents().map { it.id }.toSet()
        if (oldBalance == null && oldEventIds.isEmpty()) return

        pointsData.events
            .filter { it.id !in oldEventIds }
            .filterNot { it.eventType.isMergedIntoActivityNotification() }
            .forEach { event ->
                val category = event.pointNotificationCategory()
                val earned = event.points >= 0
                val title = when {
                    event.eventType.equals("REWARD_REFUNDED", ignoreCase = true) -> "Reward Points Returned"
                    event.eventType.contains("REWARD", ignoreCase = true) -> "Reward Points Used"
                    earned -> "Points Earned"
                    else -> "Points Updated"
                }
                val message = when {
                    event.eventType.equals("REWARD_REFUNDED", ignoreCase = true) ->
                        "${event.points} points were returned to your balance."
                    earned -> "You gained ${event.points} points."
                    else -> "${kotlin.math.abs(event.points)} points were used."
                }
                insertNotification(
                    id = "points-${event.id}",
                    category = category,
                    title = title,
                    message = message,
                    sourceId = event.id
                )
            }
    }

    suspend fun notifyRewardChanges(redemptions: List<Redemption>) {
        val cacheable = redemptions.mapNotNull { it.toCachedRedemptionEntity() }
        if (cacheable.isEmpty()) return

        val previousById = dao.getCachedRedemptionsByIds(cacheable.map { it.id }).associateBy { it.id }
        cacheable.forEach { redemption ->
            val previous = previousById[redemption.id]
            if (previous == null || previous.status.equals(redemption.status, ignoreCase = true)) return@forEach

            val status = redemption.status.uppercase()
            val title = when (status) {
                "RESERVED" -> "Reward Reserved"
                "SENT", "CLAIMED", "COMPLETED" -> "Reward Collected"
                "CANCELLED", "REJECTED", "FAILED" -> "Reward Not Completed"
                else -> "Reward Updated"
            }
            val message = when (status) {
                "RESERVED" -> "Your ${redemption.itemName} is reserved for pickup."
                "SENT", "CLAIMED", "COMPLETED" -> "Your ${redemption.itemName} redemption has been completed."
                "CANCELLED", "REJECTED", "FAILED" -> "Your ${redemption.itemName} redemption was cancelled or could not be completed."
                else -> "Your ${redemption.itemName} redemption is now ${redemption.status.toReadableStatus()}."
            }
            insertNotification(
                id = "reward-${redemption.id}-$status",
                category = NotificationCategory.Reward,
                title = title,
                message = message,
                sourceId = redemption.id
            )
        }

        dao.upsertRedemptions(cacheable)
    }

    suspend fun recordRewardRedeemed(redemption: Redemption) {
        val cacheable = redemption.toCachedRedemptionEntity() ?: return
        val pointText = if (cacheable.pointsSpent > 0) {
            "${cacheable.pointsSpent} points were used."
        } else {
            "Your point balance will update shortly."
        }
        insertNotification(
            id = "reward-${cacheable.id}-redeemed",
            category = NotificationCategory.Reward,
            title = "Reward Redeemed",
            message = "Your ${cacheable.itemName} redemption was successful. $pointText",
            sourceId = cacheable.id
        )
        dao.upsertRedemptions(listOf(cacheable))
    }

    suspend fun notifyBadgeChanges(progress: BadgeProgressData) {
        val earned = progress.earned
        val previousIds = dao.getCachedEarnedBadgeIds().toSet()
        if (previousIds.isNotEmpty()) {
            earned
                .filter { it.badgeId !in previousIds }
                .forEach { badge ->
                    insertNotification(
                        id = "badge-${badge.badgeId}",
                        category = NotificationCategory.Reward,
                        title = "Badge Unlocked",
                        message = "You earned ${badge.name}.",
                        sourceId = badge.badgeId
                    )
                }
        }

        dao.upsertEarnedBadges(
            earned.map {
                CachedEarnedBadgeEntity(
                    badgeId = it.badgeId,
                    name = it.name,
                    tier = it.tier,
                    awardedAt = it.awardedAt
                )
            }
        )
    }

    suspend fun notifyLeaderboardChanges(timeframe: String, response: LeaderboardResponse) {
        val userId = sessionManager.getUser()?.id ?: return
        if (timeframe.equals("daily", ignoreCase = true)) {
            response.entries.minByOrNull { it.rank }?.let { champion ->
                EcoReminderScheduler.cacheDailyChampion(
                    context = appContext,
                    name = champion.full_name ?: "Today's top recycler",
                    points = champion.total_points,
                    generatedAt = response.generated_at
                )
            }
        }

        val current = response.entries.firstOrNull { it.user_id == userId } ?: return
        val previous = dao.getCachedLeaderboardRank(timeframe)

        if (timeframe.equals("weekly", ignoreCase = true) && previous != null && current.rank < previous.rank) {
            val climbedPlaces = previous.rank - current.rank
            val enteredTopTen = previous.rank > 10 && current.rank <= 10
            val climbedTenPlaces = climbedPlaces >= 10
            if (enteredTopTen || climbedTenPlaces) {
                val title = if (enteredTopTen) "Weekly Top 10" else "Big Rank Climb"
                val message = if (enteredTopTen) {
                    "You entered the weekly top 10 at Rank #${current.rank}."
                } else {
                    "You climbed $climbedPlaces places this week, from #${previous.rank} to #${current.rank}."
                }
                insertNotification(
                    id = "leaderboard-$timeframe-${previous.rank}-${current.rank}-${response.generated_at}",
                    category = NotificationCategory.Leaderboard,
                    title = title,
                    message = message,
                    sourceId = userId
                )
            }
        }

        dao.upsertLeaderboardRank(
            CachedLeaderboardRankEntity(
                timeframe = timeframe,
                userId = userId,
                rank = current.rank,
                totalPoints = current.total_points,
                lifetimePoints = current.lifetime_points
            )
        )
    }

    private suspend fun insertNotification(
        id: String,
        category: NotificationCategory,
        title: String,
        message: String,
        sourceId: String? = null
    ) {
        dao.insertNotification(
            LocalNotificationEntity(
                id = id,
                category = category.value,
                title = title,
                message = message,
                sourceId = sourceId
            )
        )
    }

    private fun BackendPointsEvent.pointNotificationCategory(): NotificationCategory {
        return when {
            eventType.contains("MISSION", ignoreCase = true) -> NotificationCategory.Mission
            eventType.contains("RECYCLING", ignoreCase = true) -> NotificationCategory.Recycling
            else -> NotificationCategory.Reward
        }
    }

    private enum class NotificationCategory(val value: String) {
        Mission("MISSION"),
        Recycling("RECYCLING"),
        Reward("REWARD"),
        Leaderboard("LEADERBOARD")
    }
}

private fun String.toReadableStatus(): String =
    lowercase()
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }

private fun String.isMergedIntoActivityNotification(): Boolean =
    equals("MISSION_COMPLETED", ignoreCase = true) ||
        equals("MISSION_APPROVED", ignoreCase = true) ||
        equals("RECYCLING_APPROVED", ignoreCase = true) ||
        equals("REWARD_REDEEMED", ignoreCase = true)
