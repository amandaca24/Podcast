package br.ufpe.cin.android.podcast.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.ufpe.cin.android.podcast.dao.EpisodeDAO
import br.ufpe.cin.android.podcast.dao.FeedDAO
import br.ufpe.cin.android.podcast.dao.FeedWithEpisodesDAO

@Database(entities = [Episode::class, Feed::class], version = 1)
abstract class PodcastDatabase: RoomDatabase() {
    abstract fun episodeDAO(): EpisodeDAO
    abstract fun feedDAO(): FeedDAO

    companion object {
        // Implementa padrão Singleton de forma a previnir que múltiplas instâncias do banco de dados abra ao mesmo tempo
        @Volatile
        private var INSTANCE: PodcastDatabase? = null

        fun getDatabase(context: Context): PodcastDatabase {
            //Se a instância não for null, retorne-a
            //Se for null, crie a base de dados
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PodcastDatabase::class.java,
                    "episode_database"
                ).build()
                INSTANCE = instance
                // retorna a instância
                instance
            }
        }
    }
}