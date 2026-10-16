package com.annhien.womenshealth.data.repository

import com.annhien.womenshealth.core.database.dao.BookmarkDao
import com.annhien.womenshealth.core.database.dao.KnowledgeArticleDao
import com.annhien.womenshealth.core.database.entity.BookmarkEntity
import com.annhien.womenshealth.core.database.entity.KnowledgeArticleEntity
import com.annhien.womenshealth.domain.repository.KnowledgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnowledgeRepositoryImpl @Inject constructor(
    private val knowledgeArticleDao: KnowledgeArticleDao,
    private val bookmarkDao: BookmarkDao
) : KnowledgeRepository {

    override fun observeArticles(category: String?): Flow<List<KnowledgeArticleEntity>> {
        return if (category != null) {
            knowledgeArticleDao.observeByCategory(category)
        } else {
            knowledgeArticleDao.observeAll()
        }
    }

    override suspend fun getArticleById(id: String): KnowledgeArticleEntity? {
        return knowledgeArticleDao.getById(id)
    }

    override suspend fun searchArticles(query: String): List<KnowledgeArticleEntity> {
        return knowledgeArticleDao.search(query)
    }

    override fun observeBookmarks(): Flow<List<String>> {
        return bookmarkDao.observeAll().map { bookmarks ->
            bookmarks.map { it.articleId }
        }
    }

    override suspend fun toggleBookmark(articleId: String) {
        val existing = bookmarkDao.get(articleId)
        if (existing != null) {
            bookmarkDao.delete(existing)
        } else {
            bookmarkDao.upsert(
                BookmarkEntity(
                    id = UUID.randomUUID().toString(),
                    articleId = articleId,
                    createdAt = Instant.now()
                )
            )
        }
    }
}
