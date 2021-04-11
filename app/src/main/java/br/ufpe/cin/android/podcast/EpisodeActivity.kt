package br.ufpe.cin.android.podcast

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.adapters.EpisodeAdapter
import br.ufpe.cin.android.podcast.data.PodcastDatabase
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import br.ufpe.cin.android.podcast.model.EpisodeViewModelFactory
import br.ufpe.cin.android.podcast.repositories.EpisodeRepository
import br.ufpe.cin.android.podcast.services.MusicPlayerService

const val EP_LINK = "episode link"

class EpisodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEpisodeBinding

    private val episodeViewModel : EpisodeViewModel by viewModels{
        val repo = EpisodeRepository(PodcastDatabase.getDatabase(this).episodeDAO())
        EpisodeViewModelFactory(repo)
    }

    private lateinit var episodeAdapter: EpisodeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewEpisodes = binding.episodeRV
        episodeAdapter = EpisodeAdapter(layoutInflater)

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



        /*val title = intent.getStringExtra("title")
        if(title != null){
            feedEpViewModel.getEpisodesByFeed(title)

        }else{
            Toast.makeText(this,"That's been some kind of error!", Toast.LENGTH_SHORT).show()
            finish()
        }




        feedEpViewModel.current.observe(
            this,
            Observer {
                episodeAdapter.submitList(it.toList())
            }
        )*/


    }

}





