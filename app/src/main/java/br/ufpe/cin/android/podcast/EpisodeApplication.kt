package br.ufpe.cin.android.podcast

import android.app.Application
import br.ufpe.cin.android.podcast.data.EpisodeDatabase

class EpisodeApplication : Application() {
    val database by lazy { EpisodeDatabase.getDatabase(this) }
    val repository by lazy { EpisodeRepository(database.episodeDAO()) }
}