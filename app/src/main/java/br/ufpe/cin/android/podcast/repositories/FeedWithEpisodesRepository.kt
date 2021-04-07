package br.ufpe.cin.android.podcast.repositories

import androidx.lifecycle.LiveData
import br.ufpe.cin.android.podcast.dao.FeedWithEpisodesDAO
import br.ufpe.cin.android.podcast.data.FeedWithEpisodes

class FeedWithEpisodesRepository(private val feedWithEpisodesDAO: FeedWithEpisodesDAO) {

    suspend fun getEpsByFeed(feedId: String) : List<FeedWithEpisodes> {
        return feedWithEpisodesDAO.getEpisodesByFeed(feedId)
    }
}