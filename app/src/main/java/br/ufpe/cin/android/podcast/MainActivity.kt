package br.ufpe.cin.android.podcast

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.adapters.FeedAdapter
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.data.PodcastDatabase
import br.ufpe.cin.android.podcast.databinding.ActivityMainBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import br.ufpe.cin.android.podcast.model.FeedViewModel
import br.ufpe.cin.android.podcast.model.FeedViewModelFactory
import br.ufpe.cin.android.podcast.repositories.EpisodeRepository
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

    //private lateinit var lista: List<Episode>

    private val defaultfeed = "https://jovemnerd.com.br/feed-nerdcast/"

    private val feedViewModel: FeedViewModel by viewModels() {
        val feedRepo = FeedRepository(PodcastDatabase.getDatabase(this).feedDAO())
        FeedViewModelFactory(feedRepo)

    }

    private val episodeViewModel : EpisodeViewModel by viewModels(){
        val episodeRepo = EpisodeRepository(PodcastDatabase.getDatabase(this).episodeDAO())
        EpisodeViewModel.EpisodeViewModelFactory(episodeRepo)
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

        binding.btnPrefs.setOnClickListener{
            startActivity(Intent(this, PreferencesActivity::class.java))
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
        val preference: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val podcastFeed = preference.getString(rss_feed, defaultfeed)

            scope.launch {
                val channel = withContext(Dispatchers.IO) {
                    podcastFeed?.let { parser.getChannel(it) }
                }

                val show = podcastFeed?.let {
                    Feed(
                        it,
                        channel?.title.toString(),
                        channel?.description.toString(),
                        channel?.link.toString(),
                        channel?.image.toString(),
                        10,
                        10
                    )

                }
                show?.let { feedViewModel.insert(it) }

                channel?.articles?.forEach { a ->
                    var episode = Episode(
                        a.link.toString(),
                        a.title.toString(),
                        a.description.toString(),
                        a.sourceUrl.toString(),
                        a.pubDate.toString(),
                        channel?.title.toString()
                    )

                    episodeViewModel.insert(episode)
                }

            }
    }

    companion object {
        val rss_feed = "RSS Feed"
    }
}