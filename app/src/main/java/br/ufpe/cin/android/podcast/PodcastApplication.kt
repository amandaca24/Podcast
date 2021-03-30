package br.ufpe.cin.android.podcast

import android.app.Application
import br.ufpe.cin.android.podcast.data.PodcastDatabase
import br.ufpe.cin.android.podcast.repositories.EpisodeRepository

class PodcastApplication : Application() {
    val database by lazy { PodcastDatabase.getDatabase(this) }
    val repository by lazy { EpisodeRepository(database.episodeDAO()) }
}