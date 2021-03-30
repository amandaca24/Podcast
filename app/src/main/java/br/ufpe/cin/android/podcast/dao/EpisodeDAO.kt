package br.ufpe.cin.android.podcast.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.ufpe.cin.android.podcast.data.Episode

@Dao
interface EpisodeDAO {
    @Query("SELECT * FROM episodios ORDER BY titulo ASC")
    fun getAll(): LiveData<List<Episode>>

    @Query("SELECT * FROM episodios WHERE linkEpisodio LIKE :linkEpisodio")
    suspend fun findByPk(linkEpisodio: String): Episode

    @Query("SELECT * FROM episodios WHERE titulo LIKE :title")
    suspend fun findByTitle(title: String): Episode

    @Query("SELECT * FROM episodios WHERE dataPublicacao LIKE :date_publi")
    suspend fun findByDate(date_publi: String): Episode

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg episode: Episode)

    @Delete
    suspend fun delete(episode: Episode)
}