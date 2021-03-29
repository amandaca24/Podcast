package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeDetailBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel

class EpisodeDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEpisodeDetailBinding

    private val episodeViewModel: EpisodeViewModel by viewModels {
        EpisodeViewModel.EpisodeViewModelFactory((application as EpisodeApplication).repository)
    }

    private val pk: String = intent.getStringExtra("EP_LINK").toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val episode = episodeViewModel.findByPk(pk)


        //binding.titleEpisode.text =


    }
}