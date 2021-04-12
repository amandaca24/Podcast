package br.ufpe.cin.android.podcast

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.adapters.FeedAdapter
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.data.PodcastDatabase
import br.ufpe.cin.android.podcast.databinding.ActivityMainBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import br.ufpe.cin.android.podcast.model.EpisodeViewModelFactory
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

    private val defaultfeed =
        "https://www.omnycontent.com/d/playlist/651a251e-06e1-47e0-9336-ac5a00f41628/220ec2a9-bb93-469e-aad7-acd8013110b7/11cd2462-4550-482d-88ea-acd8013110cf/podcast.rss"

    private val feedViewModel: FeedViewModel by viewModels() {
        val feedRepo = FeedRepository(PodcastDatabase.getDatabase(this).feedDAO())
        FeedViewModelFactory(feedRepo)

    }

    private val episodeViewModel : EpisodeViewModel by viewModels(){
        val episodeRepo = EpisodeRepository(PodcastDatabase.getDatabase(this).episodeDAO())
        EpisodeViewModelFactory(episodeRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId){
                R.id.favorite -> {
                    startActivity(Intent(this, PreferencesActivity::class.java))
                    true
                }

                else -> false
            }
        }
        

        val recyclerViewFeed = binding.feedView
        val feedAdapter = FeedAdapter(layoutInflater)

        recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = feedAdapter

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
                        podcastFeed.toString(),
                        channel?.title.toString(),
                        channel?.link.toString(),
                        channel?.description.toString(),
                        channel?.image?.link.toString(),
                        10,
                        10 )}

                show?.let { feedViewModel.insert(it) }

                channel?.articles?.forEach { a ->
                    var episode = show?.let {
                        Episode(
                            a.link.toString(),
                            a.title.toString(),
                            a.description.toString(),
                            "",
                            a.pubDate.toString(),
                            it?.urlFeed.toString())
                    }

                    if (episode != null) {
                        episodeViewModel.insert(episode)
                    }

                    Log.i("FEED URL = ", show?.urlFeed.toString())
                    Log.i("FEED TITULO = ", show?.titulo.toString())
                    Log.i("FEED DESCRIÇÃO = ", show?.descricao.toString())
                    Log.i("FEED IMAGEM = ", show?.imagemURL.toString())
                    Log.i("EPISÓDIO LINK = ", episode?.linkEpisodio.toString())
                    Log.i("EPISÓDIO TÍTULO = ", episode?.titulo.toString())
                    Log.i("EPISÓDIO DESCRIÇÃO = ", episode?.descricao.toString())
                    Log.i("EPISÓDIO LINK ARQUIVO = ", episode?.linkArquivo.toString())
                    Log.i("EPISÓDIO DATA = ", episode?.dataPublicacao.toString())
                    Log.i("EPISÓDIO FEED = ", episode?.feedId.toString())
                }
            }

    }

    companion object {
        val rss_feed = "RSS Feed"
    }
}