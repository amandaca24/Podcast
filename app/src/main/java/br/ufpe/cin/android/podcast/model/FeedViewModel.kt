package br.ufpe.cin.android.podcast.model

import androidx.lifecycle.*
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.data.FeedWithEpisodes
import br.ufpe.cin.android.podcast.repositories.FeedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class FeedViewModel(val feedRepo: FeedRepository) : ViewModel() {
    val feed = feedRepo.shows

    val episodes = feedRepo.episodes

    var current = MutableLiveData<LiveData<List<Episode>>>()

    fun insert(feed: Feed) {
        viewModelScope.launch(Dispatchers.IO) {
            feedRepo.insertShow(feed)
        }
    }

    fun delete(feed: Feed) {
        viewModelScope.launch(Dispatchers.IO) {
            feedRepo.deleteShow(feed)
        }
    }

    fun findByTitle(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            feedRepo.findByTitle(title)
        }
    }

    fun findByUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            feedRepo.findByByUrlFeed(url)
        }
    }


}

class FeedViewModelFactory(private val repo: FeedRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeedViewModel(repo) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}