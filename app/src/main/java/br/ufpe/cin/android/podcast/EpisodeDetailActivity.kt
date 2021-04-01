package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.PodcastDatabase
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeDetailBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import br.ufpe.cin.android.podcast.repositories.EpisodeRepository

class EpisodeDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEpisodeDetailBinding

    private val episodeViewModel: EpisodeViewModel by viewModels {
        val repo = EpisodeRepository(PodcastDatabase.getDatabase(this).episodeDAO())
        EpisodeViewModel.EpisodeViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title")

        if(title != null){
            episodeViewModel.findByTitle(title)
        }
        else{
            Toast.makeText(this,"That's been some kind of error!", Toast.LENGTH_SHORT).show()
            finish()
        }

        episodeViewModel.current.observe(
            this,
            Observer {
                binding.titleEpisode.text = it.titulo
                binding.dateEpisode.text = it.dataPublicacao
                binding.descriptionEpisode.text = it.descricao
                binding.linkEpisode.text = it.linkEpisodio
                binding.archiveEpisode.text = it.linkArquivo

            }
        )


    }
}