package br.ufpe.cin.android.podcast

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.adapters.FeedAdapter
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.data.PodcastDatabase
import br.ufpe.cin.android.podcast.databinding.ActivityMainBinding
import br.ufpe.cin.android.podcast.model.FeedViewModel
import br.ufpe.cin.android.podcast.model.FeedViewModelFactory
import br.ufpe.cin.android.podcast.repositories.FeedRepository
import com.prof.rssparser.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var parser : Parser
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    companion object {
        val PODCAST_FEED = "https://jovemnerd.com.br/feed-nerdcast/"
    }

    private lateinit var lista: List<Episode>

    private val feedViewModel: FeedViewModel by viewModels() {
        val feedRepo = FeedRepository(PodcastDatabase.getDatabase(this).feedDAO())
        FeedViewModelFactory(feedRepo)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        

        val recyclerViewFeed = binding.feedView
        val feedAdapter = FeedAdapter(layoutInflater)

        recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = feedAdapter

        }

        binding.btnEpisode.setOnClickListener {
            startActivity(Intent(this, EpisodeActivity::class.java))
        }

        feedViewModel.feed.observe(
            this,
            Observer {
                feedAdapter.submitList(it.toList())
            })

        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(24L * 60L * 60L * 100L)
            .build()
    }

    override fun onStart() {
        super.onStart()
        scope.launch {
            val channel = withContext(Dispatchers.IO) {
                parser.getChannel(PODCAST_FEED)
            }

            channel.articles.forEach { a ->
                var episode = Episode(
                    a.link.toString(),
                    a.title.toString(),
                    a.description.toString(),
                    a.sourceUrl.toString(),
                    a.pubDate.toString())

                lista.toMutableList().add(episode)
            }

            val show = Feed(
                PODCAST_FEED,
                channel.title.toString(),
                channel.description.toString(),
                channel.link.toString(),
                channel.image.toString(),
                10,
                10,
                lista
            )

            feedViewModel.insert(show)

        }

    }
}