package br.ufpe.cin.android.podcast.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodios")
data class Episode(
    @PrimaryKey val linkEpisodio: String,
    val titulo: String,
    val descricao: String,
    val linkArquivo: String,
    val audio: String,
    val dataPublicacao: String,
    val episodeImage : String,
    val feedId: String
) {
    override fun toString(): String {
        return "$titulo ($dataPublicacao) => $descricao"
    }
}