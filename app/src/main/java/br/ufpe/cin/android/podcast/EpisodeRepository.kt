package br.ufpe.cin.android.podcast

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import br.ufpe.cin.android.podcast.dao.EpisodeDAO
import br.ufpe.cin.android.podcast.data.Episode
import kotlinx.coroutines.flow.Flow

class EpisodeRepository(private val episodeDao: EpisodeDAO) {

    val allEpisodes: LiveData<List<Episode>> = episodeDao.getAll()

    suspend fun findByPk(pk: String){
        episodeDao.findByPk(pk)
    }

    suspend fun findByTitle(title: String){
        episodeDao.findByTitle(title)
    }

    suspend fun findByDate(date: String){
        episodeDao.findByDate(date)
    }

    //The suspend modifier tells the compiler that this needs to be called from a coroutine or another suspending function.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(episode: Episode) {
        episodeDao.insert(episode)
    }

    @WorkerThread
    suspend fun delete(episode: Episode){
        episodeDao.delete(episode)
    }
}