package br.ufpe.cin.android.podcast.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import br.ufpe.cin.android.podcast.dao.EpisodeDAO
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.FeedWithEpisodes
import kotlinx.coroutines.flow.Flow

class EpisodeRepository(private val episodeDao: EpisodeDAO) {

    val allEpisodes: LiveData<List<Episode>> = episodeDao.getAll()

    //A anotação @WorkerThread garante que os métodos serão chamados apenas na Worker Thread,
    //ou seja, de forma concorrente com contextos paralelos
    //de forma a não atrapalhar a visualização da interface do usuário

    @WorkerThread
    suspend fun findByPk(pk: String) {
        episodeDao.findByPk(pk)
    }

    @WorkerThread
    suspend fun findByTitle(title: String): Episode {
        return episodeDao.findByTitle(title)
    }

    @WorkerThread
    suspend fun findByDate(date: String) {
        episodeDao.findByDate(date)
    }

    @WorkerThread
    fun findByFeed(feed: String): LiveData<List<Episode>> {
        return episodeDao.findByFeed(feed)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(episode: Episode) {
        episodeDao.insert(episode)
    }

    @WorkerThread
    suspend fun delete(episode: Episode) {
        episodeDao.delete(episode)
    }

    @WorkerThread
    suspend fun update(episode: Episode) {
        episodeDao.update(episode)
    }
}