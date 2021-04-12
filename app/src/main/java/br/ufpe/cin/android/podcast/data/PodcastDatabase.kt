package br.ufpe.cin.android.podcast.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.ufpe.cin.android.podcast.dao.EpisodeDAO
import br.ufpe.cin.android.podcast.dao.FeedDAO

//Classe que cria o Banco de Dados e indica as entidades que formarão as tabelas
//Também indica o objeto de acesso aos dados (Dao)
@Database(entities = [Episode::class, Feed::class], version = 1)
abstract class PodcastDatabase: RoomDatabase() {
    abstract fun episodeDAO(): EpisodeDAO
    abstract fun feedDAO(): FeedDAO

    companion object {
        // Implementa padrão Singleton de forma a previnir que múltiplas instâncias do banco de dados abra ao mesmo tempo
        @Volatile
        private var INSTANCE: PodcastDatabase? = null

        fun getDatabase(context: Context): PodcastDatabase {
            //Se a instância do BD já foi iniciada, retorne-a
            //Se não, crie-a
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