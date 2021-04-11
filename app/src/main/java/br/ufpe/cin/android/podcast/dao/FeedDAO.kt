package br.ufpe.cin.android.podcast.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.data.FeedWithEpisodes


@Dao
interface FeedDAO {

    //Suspend fun é usado para longas execuções, como pesquisar apenas uma entrada no banco de dados
    //de forma a esperar que elas sejam concluídas, sem bloqueá-las

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShow(vararg feed: Feed)

    @Delete
    suspend fun delete(feed: Feed)

    @Query("SELECT * FROM feeds ORDER BY titulo ASC")
    fun getAll() : LiveData<List<Feed>>

    @Query("SELECT * FROM feeds WHERE titulo LIKE :title")
    suspend fun getByTitle(title:String) : List<Feed>

    @Query("SELECT * FROM feeds WHERE urlFeed = :urlFeed")
    suspend fun getByUrlFeed(urlFeed:String) : Feed

    @Transaction
    @Query("SELECT * FROM feeds")
    fun getAllEpisodes() : LiveData<List<FeedWithEpisodes>>



}