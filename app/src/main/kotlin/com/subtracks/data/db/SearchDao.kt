package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.subtracks.data.model.SearchIndex

@Dao
interface SearchDao {
    @Query("DELETE FROM search_index WHERE sourceId = :sourceId")
    suspend fun clear(sourceId: String)

    @Insert
    suspend fun insert(items: List<SearchIndex>)

    @Query(
        """
        SELECT * FROM search_index
        WHERE search_index MATCH '"' || replace(:query, '"', '""') || '"'
          AND sourceId = :sourceId
        ORDER BY rank
        LIMIT :limit
        """,
    )
    suspend fun searchPhrase(
        sourceId: String,
        query: String,
        limit: Int,
    ): List<SearchIndex>

    suspend fun search(
        sourceId: String,
        query: String,
        limit: Int,
    ): List<SearchIndex> {
        val phrase = query.trim()
        if (phrase.length < MIN_QUERY_LENGTH) return emptyList()
        return searchPhrase(sourceId, phrase, limit)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 3
    }
}
