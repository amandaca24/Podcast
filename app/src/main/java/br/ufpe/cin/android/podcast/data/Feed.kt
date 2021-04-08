package br.ufpe.cin.android.podcast.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "feeds")
data class Feed(
    @PrimaryKey val urlFeed: String,
    val titulo: String,
    val descricao: String,
    val linkSite: String,
    val imagemURL: String,
    val imagemLargura: Int,
    val imagemAltura: Int
) {
    override fun toString(): String {
        return "$titulo => $linkSite"
    }
}
