package br.ufpe.cin.android.podcast.repositories

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import br.ufpe.cin.android.podcast.dao.FeedDAO
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.data.FeedWithEpisodes

class FeedRepository(private val feedDAO: FeedDAO) {
    val shows = feedDAO.getAll()

    val episodes: LiveData<List<FeedWithEpisodes>> = feedDAO.getAllEpisodes()

    //A anotação @WorkerThread garante que os métodos sejam chamados apenas numa worker thread, em vez da Main

    @WorkerThread
    suspend fun insertShow(feed: Feed) {
        feedDAO.insertShow(feed)

    }

    @WorkerThread
    suspend fun deleteShow(feed: Feed) {
        feedDAO.delete(feed)
    }

    @WorkerThread
    suspend fun findByByUrlFeed(url_feed: String): Feed {
        return feedDAO.getByUrlFeed(url_feed)
    }

    @WorkerThread
    suspend fun findByTitle(title: String): List<Feed> {
        return feedDAO.getByTitle(title)
    }


}