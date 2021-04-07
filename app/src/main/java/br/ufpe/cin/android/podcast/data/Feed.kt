package br.ufpe.cin.android.podcast.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "feeds")
data class Feed(
    @PrimaryKey
    val urlFeed: String,
    var titulo: String,
    var descricao: String,
    var linkSite: String,
    var imagemURL: String,
    var imagemLargura: Int,
    var imagemAltura: Int
) {
    override fun toString(): String {
        return "$titulo => $linkSite"
    }
}
