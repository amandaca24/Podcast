package br.ufpe.cin.android.podcast.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.FeedWithEpisodes

@Dao
interface EpisodeDAO {
    //Suspend fun indica que os métodos serão executados em co-rotina,
    //garantindo que eles executem fora da Main Thread.
    @Query("SELECT * FROM episodios ORDER BY titulo ASC")
    fun getAll(): LiveData<List<Episode>>

    @Query("SELECT * FROM episodios WHERE linkEpisodio LIKE :linkEpisodio")
    suspend fun findByPk(linkEpisodio: String): Episode

    @Query("SELECT * FROM episodios WHERE titulo LIKE :title")
    suspend fun findByTitle(title: String): Episode

    @Query("SELECT * FROM episodios WHERE dataPublicacao LIKE :date_publi")
    suspend fun findByDate(date_publi: String): Episode

    @Query("SELECT * FROM episodios WHERE linkArquivo LIKE :link" )
    suspend fun findByLinkArchive(link: String) : Episode

    @Query("SELECT * FROM episodios WHERE feedId LIKE :feed")
    fun findByFeed(feed: String) : LiveData<List<Episode>>

    //A estratégia aqui é de substituir os dados do episódio em caso de conflito (mesma PK)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg episode: Episode)

    @Delete
    suspend fun delete(episode: Episode)

    @Update
    suspend fun update(vararg episode: Episode)

}