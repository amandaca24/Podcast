package br.ufpe.cin.android.podcast.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

data class FeedWithEpisodes(
    @Embedded val feed: Feed,
    @Relation(
        parentColumn = "urlFeed",
        entityColumn = "feedId"
    )
    val episodes: List<Episode>
)
