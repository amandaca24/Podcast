package br.ufpe.cin.android.podcast.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.ufpe.cin.android.podcast.dao.EpisodeDAO

@Database(entities = arrayOf(Episode::class), version = 1, exportSchema = false)
abstract class EpisodeDatabase: RoomDatabase() {
    abstract fun episodeDAO(): EpisodeDAO

    companion object {
        // Implementa padrão Singleton de forma a previnir que múltiplas instâncias do banco de dados abra ao mesmo tempo
        @Volatile
        private var INSTANCE: EpisodeDatabase? = null

        fun getDatabase(context: Context): EpisodeDatabase {
            //Se a instância não for null, retorne-a
            //Se for null, crie a base de dados
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EpisodeDatabase::class.java,
                    "episode_database"
                ).build()
                INSTANCE = instance
                // retorna a instância
                instance
            }
        }
    }
}