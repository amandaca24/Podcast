package br.ufpe.cin.android.podcast.dao

import androidx.datastore.preferences.protobuf.LazyStringArrayList
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import br.ufpe.cin.android.podcast.data.Episode
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface EpisodeDAO {
    @Query("SELECT * FROM episodios ORDER BY titulo ASC")
    fun getAll(): Flow<List<Episode>>

    @Query("SELECT * FROM episodios WHERE linkEpisodio IN (:linkEpisodio)")
    fun loadAllByPKs(linkEpisodio: LazyStringArrayList): List<Episode>

    @Query("SELECT * FROM episodios WHERE linkEpisodio LIKE :linkEpisodio")
    fun findByPk(linkEpisodio: String): Episode

    @Query("SELECT * FROM episodios WHERE titulo LIKE :title")
    fun findByTitle(title: String): Episode

    @Query("SELECT * FROM episodios WHERE dataPublicacao LIKE :date_publi")
    fun findByDate(date_publi: String): Episode

    @Insert
    fun insert(vararg episode: Episode)

    @Delete
    fun delete(episode: Episode)
}