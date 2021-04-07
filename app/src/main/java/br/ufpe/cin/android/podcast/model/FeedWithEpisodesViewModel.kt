package br.ufpe.cin.android.podcast.model

import androidx.lifecycle.*
import br.ufpe.cin.android.podcast.data.FeedWithEpisodes
import br.ufpe.cin.android.podcast.repositories.FeedWithEpisodesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedWithEpisodesViewModel(private val feedWithEpisodesRepository: FeedWithEpisodesRepository) : ViewModel() {
    
    var current = MutableLiveData<List<FeedWithEpisodes>>()

    fun getEpisodesByFeed(url: String) {
        viewModelScope.launch{
            val eps = feedWithEpisodesRepository.getEpsByFeed(url)
            withContext(Dispatchers.Main.immediate){
                current.value = eps
            }
        }
    }

    class FeedWithEpisodesViewModelFactory(private val repository: FeedWithEpisodesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FeedWithEpisodesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FeedWithEpisodesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}