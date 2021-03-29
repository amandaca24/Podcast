package br.ufpe.cin.android.podcast.model

import androidx.lifecycle.*
import br.ufpe.cin.android.podcast.EpisodeRepository
import br.ufpe.cin.android.podcast.data.Episode
import kotlinx.coroutines.launch

class EpisodeViewModel(private val repository: EpisodeRepository) : ViewModel() {

    val allEpisodes: LiveData<List<Episode>> = repository.allEpisodes.asLiveData()

    fun insert(episode: Episode) = viewModelScope.launch {
        repository.insert(episode)
    }

    fun findByPk(pk: String) = viewModelScope.launch {
        repository.findByPk(pk)
    }

    fun findByTitle(title: String) = viewModelScope.launch {
        repository.findByTitle(title)
    }

    fun findByDate(date: String) = viewModelScope.launch {
        repository.findByDate(date)
    }

    fun delete(episode: Episode) = viewModelScope.launch {
        repository.delete(episode)
    }

    class EpisodeViewModelFactory(private val repository: EpisodeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EpisodeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EpisodeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}