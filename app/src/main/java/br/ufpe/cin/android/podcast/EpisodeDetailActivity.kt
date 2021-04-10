package br.ufpe.cin.android.podcast

import android.content.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.work.*
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.PodcastDatabase
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeDetailBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import br.ufpe.cin.android.podcast.repositories.EpisodeRepository
import br.ufpe.cin.android.podcast.services.MusicPlayerService
import br.ufpe.cin.android.podcast.utils.KEY_IMAGEFILE_URI
import br.ufpe.cin.android.podcast.utils.KEY_LINK_URI

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

            episodeViewModel.current.observe(
                this,
                Observer {
                    binding.titleEpisode.text = it.titulo
                    binding.dateEpisode.text = it.dataPublicacao
                    binding.descriptionEpisode.text = it.descricao
                    binding.linkEpisode.text = it.linkEpisodio
                })

        } else {
            Toast.makeText(this, "That's been some kind of error!", Toast.LENGTH_SHORT).show()
            finish()

        }

        binding.btnDownload.setOnClickListener{
            val intent = Intent(this, DownloadActivity::class.java)
            intent.putExtra("title", binding.titleEpisode.text.toString())
            startActivity(intent)
        }

    }

}