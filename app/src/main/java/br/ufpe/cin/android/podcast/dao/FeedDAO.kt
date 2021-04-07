package br.ufpe.cin.android.podcast.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.data.FeedWithEpisodes


@Dao
interface FeedDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertShow(vararg feed: Feed)

    @Delete
    suspend fun delete(feed: Feed)

    @Query("SELECT * FROM feeds ORDER BY titulo ASC")
    fun getAll() : LiveData<List<Feed>>

    @Query("SELECT * FROM feeds WHERE titulo LIKE :title")
    suspend fun getByTitle(title:String) : List<Feed>

    @Query("SELECT * FROM feeds WHERE urlFeed = :urlFeed")
    suspend fun getByUrlFeed(urlFeed:String) : Feed



}