package br.ufpe.cin.android.podcast.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.FeedWithEpisodes

@Dao
interface FeedWithEpisodesDAO {

    @Transaction
    @Query("SELECT * FROM feeds WHERE urlFeed LIKE :url")
    suspend fun getEpisodesByFeed(url:String) : List<FeedWithEpisodes>
}