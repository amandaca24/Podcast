package br.ufpe.cin.android.podcast

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.EpisodeDatabase
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import com.prof.rssparser.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val EP_LINK = "episode link"

class EpisodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEpisodeBinding

    //private val newEpisodeActivityRequestCode = 1
    private val episodeViewModel: EpisodeViewModel by viewModels {
        val repo = EpisodeRepository(EpisodeDatabase.getDatabase(this).episodeDAO())
        EpisodeViewModel.EpisodeViewModelFactory(repo)
    }

    private lateinit var parser : Parser
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    companion object {
        val PODCAST_FEED = "https://jovemnerd.com.br/feed-nerdcast/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_episode)

        val recyclerViewEpisodes = binding.recyclerView
        val episodeAdapter = EpisodeAdapter(layoutInflater)

        recyclerViewEpisodes.apply {
            layoutManager = LinearLayoutManager(this@EpisodeActivity)
            addItemDecoration(DividerItemDecoration(this@EpisodeActivity, DividerItemDecoration.VERTICAL))
            adapter = episodeAdapter

        }

        episodeViewModel.allEpisodes.observe(
            this,
            Observer {
                episodeAdapter.submitList(it.toList())
            }
        )


        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(24L * 60L * 60L * 100L)
            .build()
    }

    override fun onStart() {
        super.onStart()
        scope.launch {
            val channel = withContext(Dispatchers.IO) {
                parser.getChannel(EpisodeActivity.PODCAST_FEED)
            }

            channel.articles.forEach { a ->
                var episode = Episode(
                    a.link.toString(),
                    a.title.toString(),
                    a.description.toString(),
                    a.sourceUrl.toString(),
                    a.pubDate.toString())

                episodeViewModel.insert(episode)
                }
            }
    }

}




