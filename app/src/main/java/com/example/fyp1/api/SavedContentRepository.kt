package com.example.fyp1.api

import android.content.Context
import com.example.fyp1.offline.OfflineDatabase
import com.example.fyp1.offline.SavedContentEntity
import com.example.fyp1.offline.toBackendContent
import com.example.fyp1.offline.toCachedEntity

class SavedContentRepository(context: Context) {
    private val dao = OfflineDatabase.get(context.applicationContext).offlineDao()

    suspend fun getSavedContent(): List<BackendContent> =
        dao.getSavedContent().map { it.toBackendContent() }

    suspend fun getSavedIds(): Set<String> =
        dao.getSavedContentIds().toSet()

    suspend fun isSaved(contentId: String): Boolean =
        dao.isContentSaved(contentId)

    suspend fun save(content: BackendContent) {
        dao.upsertContentItem(content.toCachedEntity())
        dao.saveContent(SavedContentEntity(contentId = content.id))
    }

    suspend fun unsave(contentId: String) {
        dao.unsaveContent(contentId)
    }

    suspend fun toggle(content: BackendContent): Boolean {
        return if (isSaved(content.id)) {
            unsave(content.id)
            false
        } else {
            save(content)
            true
        }
    }
}
