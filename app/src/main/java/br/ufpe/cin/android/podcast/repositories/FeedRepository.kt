package br.ufpe.cin.android.podcast.repositories

import androidx.annotation.WorkerThread
import br.ufpe.cin.android.podcast.dao.FeedDAO
import br.ufpe.cin.android.podcast.data.Feed

class FeedRepository(private val feedDAO: FeedDAO) {
    val shows = feedDAO.getAll()

    @WorkerThread
    suspend fun insertShow(feed: Feed){
        feedDAO.insertShow(feed)

    }

    @WorkerThread
    suspend fun deleteShow(feed: Feed){
        feedDAO.delete(feed)
    }

    @WorkerThread
    suspend fun findByByUrlFeed(url_feed: String) : Feed {
        return feedDAO.getByUrlFeed(url_feed)
    }

    @WorkerThread
    suspend fun findByTitle(title: String) : List<Feed>{
        return feedDAO.getByTitle(title)
    }
}