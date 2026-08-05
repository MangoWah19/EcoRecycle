package com.example.fyp1.offline

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fyp1.RecyclingLog
import com.example.fyp1.Redemption
import com.example.fyp1.api.BackendPointsEvent
import com.example.fyp1.api.BackendContent
import com.example.fyp1.api.BackendContentBlock
import com.example.fyp1.api.BackendMission
import com.example.fyp1.api.BackendSubmission
import com.example.fyp1.api.PointsData
import com.example.fyp1.api.SubmissionMissionSummary
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_missions")
data class CachedMissionEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val longDescription: String?,
    val imageUrl: String?,
    val guideJson: String?,
    val targetQuantity: Int?,
    val targetDays: Int?,
    val type: String,
    val startAt: String,
    val endAt: String,
    val submissionCap: Int?,
    val points: Int,
    val autoApprove: Boolean,
    val isActive: Boolean,
    val status: String,
    val createdById: String,
    val createdAt: String,
    val updatedAt: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_mission_submissions")
data class CachedMissionSubmissionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val missionId: String,
    val status: String,
    val proofText: String?,
    val proofImageUrl: String?,
    val quantity: Int?,
    val photoUrl: String?,
    val note: String?,
    val reviewNote: String?,
    val submittedAt: String?,
    val reviewedAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val missionTitle: String?,
    val missionSlug: String?,
    val missionPoints: Int?,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_content")
data class CachedContentEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val title: String,
    val body: String,
    val summary: String?,
    val imageUrl: String?,
    val estimatedReadMinutes: Int?,
    val contentBlocksJson: String?,
    val tagsJson: String,
    val status: String,
    val version: Int,
    val createdById: String,
    val createdAt: String,
    val updatedAt: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_content")
data class SavedContentEntity(
    @PrimaryKey val contentId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_mission_submissions")
data class PendingMissionSubmissionEntity(
    @PrimaryKey val localId: String,
    val missionId: String,
    val userId: String,
    val proofText: String,
    val quantity: Int?,
    val localImagePath: String,
    val mimeType: String,
    val fileName: String,
    val status: String = PendingMissionStatus.PendingUpload.value,
    val retryCount: Int = 0,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_point_balance")
data class CachedPointBalanceEntity(
    @PrimaryKey val id: String = POINT_LEDGER_CACHE_ID,
    val total: Int,
    val lifetimeTotal: Int,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_point_events")
data class CachedPointEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val missionId: String?,
    val submissionId: String?,
    val recyclingSubmissionId: String?,
    val redemptionId: String?,
    val points: Int,
    val eventType: String,
    val status: String,
    val approvedAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val sortAt: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_notifications")
data class LocalNotificationEntity(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val message: String,
    val sourceId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_recycling_submissions")
data class CachedRecyclingSubmissionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val source: String?,
    val qrCodeId: String?,
    val materialType: String,
    val quantity: Double,
    val status: String,
    val pointsAwarded: Int,
    val submittedAt: String?,
    val reviewedAt: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_redemptions")
data class CachedRedemptionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val rewardId: String?,
    val itemName: String,
    val pointsSpent: Int,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_earned_badges")
data class CachedEarnedBadgeEntity(
    @PrimaryKey val badgeId: String,
    val name: String,
    val tier: String,
    val awardedAt: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_leaderboard_ranks")
data class CachedLeaderboardRankEntity(
    @PrimaryKey val timeframe: String,
    val userId: String,
    val rank: Int,
    val totalPoints: Int,
    val lifetimePoints: Int,
    val cachedAt: Long = System.currentTimeMillis()
)

enum class PendingMissionStatus(val value: String) {
    PendingUpload("PENDING_UPLOAD"),
    Uploading("UPLOADING"),
    Failed("FAILED_UPLOAD")
}

@Dao
interface OfflineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMissions(missions: List<CachedMissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMission(mission: CachedMissionEntity)

    @Query("SELECT * FROM cached_missions ORDER BY startAt DESC, title ASC")
    suspend fun getCachedMissions(): List<CachedMissionEntity>

    @Query("SELECT * FROM cached_missions WHERE id = :id LIMIT 1")
    suspend fun getCachedMission(id: String): CachedMissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContent(content: List<CachedContentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContentItem(content: CachedContentEntity)

    @Query("SELECT * FROM cached_content ORDER BY updatedAt DESC, title ASC")
    suspend fun getCachedContent(): List<CachedContentEntity>

    @Query("SELECT * FROM cached_content WHERE id = :id LIMIT 1")
    suspend fun getCachedContentById(id: String): CachedContentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveContent(saved: SavedContentEntity)

    @Query("DELETE FROM saved_content WHERE contentId = :contentId")
    suspend fun unsaveContent(contentId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_content WHERE contentId = :contentId)")
    suspend fun isContentSaved(contentId: String): Boolean

    @Query("SELECT contentId FROM saved_content")
    suspend fun getSavedContentIds(): List<String>

    @Query("""
        SELECT cached_content.* FROM cached_content
        INNER JOIN saved_content ON saved_content.contentId = cached_content.id
        ORDER BY saved_content.savedAt DESC
    """)
    suspend fun getSavedContent(): List<CachedContentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubmissions(submissions: List<CachedMissionSubmissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubmission(submission: CachedMissionSubmissionEntity)

    @Query("SELECT * FROM cached_mission_submissions ORDER BY COALESCE(submittedAt, createdAt, '') DESC")
    suspend fun getCachedSubmissions(): List<CachedMissionSubmissionEntity>

    @Query("SELECT * FROM cached_mission_submissions WHERE id IN (:ids)")
    suspend fun getCachedSubmissionsByIds(ids: List<String>): List<CachedMissionSubmissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPendingMissionSubmission(submission: PendingMissionSubmissionEntity)

    @Query("SELECT * FROM pending_mission_submissions ORDER BY createdAt DESC")
    suspend fun getPendingMissionSubmissions(): List<PendingMissionSubmissionEntity>

    @Query("SELECT * FROM pending_mission_submissions WHERE status IN ('PENDING_UPLOAD', 'FAILED_UPLOAD') ORDER BY createdAt ASC")
    suspend fun getUploadableMissionSubmissions(): List<PendingMissionSubmissionEntity>

    @Query("UPDATE pending_mission_submissions SET status = :status, retryCount = :retryCount, errorMessage = :errorMessage, updatedAt = :updatedAt WHERE localId = :localId")
    suspend fun updatePendingMissionStatus(
        localId: String,
        status: String,
        retryCount: Int,
        errorMessage: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM pending_mission_submissions WHERE localId = :localId")
    suspend fun deletePendingMissionSubmission(localId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPointBalance(balance: CachedPointBalanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPointEvents(events: List<CachedPointEventEntity>)

    @Query("SELECT * FROM cached_point_balance WHERE id = :id LIMIT 1")
    suspend fun getCachedPointBalance(id: String = POINT_LEDGER_CACHE_ID): CachedPointBalanceEntity?

    @Query("SELECT * FROM cached_point_events ORDER BY sortAt DESC, id DESC")
    suspend fun getCachedPointEvents(): List<CachedPointEventEntity>

    @Query("DELETE FROM cached_point_events")
    suspend fun clearCachedPointEvents()

    @Transaction
    suspend fun replacePointLedger(pointsData: PointsData) {
        upsertPointBalance(pointsData.toCachedBalance())
        clearCachedPointEvents()
        upsertPointEvents(pointsData.events.map { it.toCachedEntity() })
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: LocalNotificationEntity): Long

    @Query("SELECT * FROM local_notifications ORDER BY createdAt DESC")
    fun observeNotifications(): Flow<List<LocalNotificationEntity>>

    @Query("SELECT COUNT(*) FROM local_notifications WHERE isRead = 0")
    fun observeUnreadNotificationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM local_notifications WHERE isRead = 0")
    suspend fun getUnreadNotificationCount(): Int

    @Query("UPDATE local_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)

    @Query("UPDATE local_notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    @Query("DELETE FROM local_notifications")
    suspend fun clearNotifications()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecyclingSubmissions(submissions: List<CachedRecyclingSubmissionEntity>)

    @Query("SELECT * FROM cached_recycling_submissions WHERE id IN (:ids)")
    suspend fun getCachedRecyclingSubmissionsByIds(ids: List<String>): List<CachedRecyclingSubmissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRedemptions(redemptions: List<CachedRedemptionEntity>)

    @Query("SELECT * FROM cached_redemptions WHERE id IN (:ids)")
    suspend fun getCachedRedemptionsByIds(ids: List<String>): List<CachedRedemptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEarnedBadges(badges: List<CachedEarnedBadgeEntity>)

    @Query("SELECT badgeId FROM cached_earned_badges")
    suspend fun getCachedEarnedBadgeIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLeaderboardRank(rank: CachedLeaderboardRankEntity)

    @Query("SELECT * FROM cached_leaderboard_ranks WHERE timeframe = :timeframe LIMIT 1")
    suspend fun getCachedLeaderboardRank(timeframe: String): CachedLeaderboardRankEntity?
}

@Database(
    entities = [
        CachedMissionEntity::class,
        CachedMissionSubmissionEntity::class,
        CachedContentEntity::class,
        SavedContentEntity::class,
        PendingMissionSubmissionEntity::class,
        CachedPointBalanceEntity::class,
        CachedPointEventEntity::class,
        LocalNotificationEntity::class,
        CachedRecyclingSubmissionEntity::class,
        CachedRedemptionEntity::class,
        CachedEarnedBadgeEntity::class,
        CachedLeaderboardRankEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {
    abstract fun offlineDao(): OfflineDao

    companion object {
        @Volatile private var instance: OfflineDatabase? = null

        fun get(context: Context): OfflineDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "eco_recycle_offline.db"
                ).addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cached_point_balance` (
                        `id` TEXT NOT NULL,
                        `total` INTEGER NOT NULL,
                        `lifetimeTotal` INTEGER NOT NULL,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cached_point_events` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `missionId` TEXT,
                        `submissionId` TEXT,
                        `recyclingSubmissionId` TEXT,
                        `redemptionId` TEXT,
                        `points` INTEGER NOT NULL,
                        `eventType` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `approvedAt` TEXT,
                        `createdAt` TEXT,
                        `updatedAt` TEXT,
                        `sortAt` TEXT NOT NULL,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `local_notifications` (
                        `id` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `sourceId` TEXT,
                        `isRead` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cached_recycling_submissions` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `source` TEXT,
                        `qrCodeId` TEXT,
                        `materialType` TEXT NOT NULL,
                        `quantity` REAL NOT NULL,
                        `status` TEXT NOT NULL,
                        `pointsAwarded` INTEGER NOT NULL,
                        `submittedAt` TEXT,
                        `reviewedAt` TEXT,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cached_redemptions` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `rewardId` TEXT,
                        `itemName` TEXT NOT NULL,
                        `pointsSpent` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAt` TEXT,
                        `updatedAt` TEXT,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cached_earned_badges` (
                        `badgeId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `tier` TEXT NOT NULL,
                        `awardedAt` TEXT,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`badgeId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cached_leaderboard_ranks` (
                        `timeframe` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `rank` INTEGER NOT NULL,
                        `totalPoints` INTEGER NOT NULL,
                        `lifetimePoints` INTEGER NOT NULL,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`timeframe`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}

private const val POINT_LEDGER_CACHE_ID = "current"
private val offlineGson = Gson()
private val contentBlockListType = object : TypeToken<List<BackendContentBlock>>() {}.type
private val stringListType = object : TypeToken<List<String>>() {}.type

fun BackendMission.toCachedEntity(): CachedMissionEntity = CachedMissionEntity(
    id = id,
    slug = slug,
    title = title,
    description = description,
    longDescription = longDescription,
    imageUrl = imageUrl,
    guideJson = guide?.toString(),
    targetQuantity = targetQuantity,
    targetDays = targetDays,
    type = type,
    startAt = startAt,
    endAt = endAt,
    submissionCap = submissionCap,
    points = points,
    autoApprove = autoApprove,
    isActive = isActive,
    status = status,
    createdById = createdById,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CachedMissionEntity.toBackendMission(): BackendMission = BackendMission(
    id = id,
    slug = slug,
    title = title,
    description = description,
    longDescription = longDescription,
    imageUrl = imageUrl,
    guide = guideJson?.toJsonElementOrNull(),
    targetQuantity = targetQuantity,
    targetDays = targetDays,
    type = type,
    startAt = startAt,
    endAt = endAt,
    submissionCap = submissionCap,
    points = points,
    autoApprove = autoApprove,
    isActive = isActive,
    status = status,
    createdById = createdById,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BackendSubmission.toCachedEntity(): CachedMissionSubmissionEntity = CachedMissionSubmissionEntity(
    id = id,
    userId = userId,
    missionId = missionId,
    status = status,
    proofText = proofText,
    proofImageUrl = proofImageUrl,
    quantity = quantity,
    photoUrl = photoUrl,
    note = note,
    reviewNote = reviewNote,
    submittedAt = submittedAt,
    reviewedAt = reviewedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    missionTitle = mission?.title,
    missionSlug = mission?.slug,
    missionPoints = mission?.points
)

fun CachedMissionSubmissionEntity.toBackendSubmission(): BackendSubmission = BackendSubmission(
    id = id,
    userId = userId,
    missionId = missionId,
    status = status,
    proofText = proofText,
    proofImageUrl = proofImageUrl,
    quantity = quantity,
    photoUrl = photoUrl,
    note = note,
    reviewNote = reviewNote,
    submittedAt = submittedAt,
    reviewedAt = reviewedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    mission = if (missionTitle != null && missionSlug != null && missionPoints != null) {
        SubmissionMissionSummary(missionId, missionTitle, missionSlug, missionPoints)
    } else {
        null
    }
)

fun PendingMissionSubmissionEntity.toBackendSubmission(): BackendSubmission = BackendSubmission(
    id = localId,
    userId = userId,
    missionId = missionId,
    status = status,
    proofText = proofText,
    proofImageUrl = localImagePath,
    quantity = quantity,
    photoUrl = localImagePath,
    note = errorMessage,
    reviewNote = "Saved on this device. It will upload automatically when connection is available.",
    submittedAt = createdAt.toIsoLikeString(),
    createdAt = createdAt.toIsoLikeString(),
    updatedAt = updatedAt.toIsoLikeString()
)

fun BackendContent.toCachedEntity(): CachedContentEntity = CachedContentEntity(
    id = id,
    slug = slug,
    title = title,
    body = body,
    summary = summary,
    imageUrl = imageUrl,
    estimatedReadMinutes = estimatedReadMinutes,
    contentBlocksJson = contentBlocks?.let { offlineGson.toJson(it) },
    tagsJson = offlineGson.toJson(tags),
    status = status,
    version = version,
    createdById = createdById,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CachedContentEntity.toBackendContent(): BackendContent = BackendContent(
    id = id,
    slug = slug,
    title = title,
    body = body,
    summary = summary,
    imageUrl = imageUrl,
    estimatedReadMinutes = estimatedReadMinutes,
    contentBlocks = contentBlocksJson?.let { runCatching { offlineGson.fromJson<List<BackendContentBlock>>(it, contentBlockListType) }.getOrNull() },
    tags = runCatching { offlineGson.fromJson<List<String>>(tagsJson, stringListType) }.getOrNull() ?: emptyList(),
    status = status,
    version = version,
    createdById = createdById,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PointsData.toCachedBalance(): CachedPointBalanceEntity = CachedPointBalanceEntity(
    total = total,
    lifetimeTotal = lifetimeTotal
)

fun BackendPointsEvent.toCachedEntity(): CachedPointEventEntity = CachedPointEventEntity(
    id = id,
    userId = userId,
    missionId = missionId,
    submissionId = submissionId,
    recyclingSubmissionId = recyclingSubmissionId,
    redemptionId = redemptionId,
    points = points,
    eventType = eventType,
    status = status,
    approvedAt = approvedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sortAt = approvedAt ?: createdAt ?: updatedAt ?: ""
)

fun CachedPointEventEntity.toBackendPointsEvent(): BackendPointsEvent = BackendPointsEvent(
    id = id,
    userId = userId,
    missionId = missionId,
    submissionId = submissionId,
    recyclingSubmissionId = recyclingSubmissionId,
    redemptionId = redemptionId,
    points = points,
    eventType = eventType,
    status = status,
    approvedAt = approvedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RecyclingLog.toCachedRecyclingEntity(): CachedRecyclingSubmissionEntity? {
    val safeId = id ?: return null
    return CachedRecyclingSubmissionEntity(
        id = safeId,
        userId = user_id,
        source = source,
        qrCodeId = qr_code_id,
        materialType = material_type,
        quantity = quantity,
        status = status,
        pointsAwarded = points_awarded,
        submittedAt = created_at,
        reviewedAt = null
    )
}

fun Redemption.toCachedRedemptionEntity(): CachedRedemptionEntity? {
    val safeId = id ?: return null
    return CachedRedemptionEntity(
        id = safeId,
        userId = user_id,
        rewardId = reward_id,
        itemName = item_name,
        pointsSpent = points_spent,
        status = status,
        createdAt = created_at,
        updatedAt = completed_at ?: claimed_at ?: cancelled_at ?: reserved_at
    )
}

private fun String.toJsonElementOrNull(): JsonElement? =
    runCatching { JsonParser.parseString(this) }.getOrNull()

private fun Long.toIsoLikeString(): String = java.time.Instant.ofEpochMilli(this).toString()
