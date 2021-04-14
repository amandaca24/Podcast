package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
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

    //Inicializa o objeto do ViewModel para acessar os dados, garantindo a integridade deles
    private val episodeViewModel : EpisodeViewModel by viewModels{
        val repo = EpisodeRepository(PodcastDatabase.getDatabase(this).episodeDAO())
        EpisodeViewModelFactory(repo)
    }

    private lateinit var episodeAdapter: EpisodeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Fazendo o binding entre o layout da Activity
        val recyclerViewEpisodes = binding.episodeRV
        //Iniciando o adapter
        episodeAdapter = EpisodeAdapter(layoutInflater)

        //Aplicando o adapter do recycler view
        recyclerViewEpisodes.apply {
            layoutManager = LinearLayoutManager(this@EpisodeActivity)
            addItemDecoration(DividerItemDecoration(this@EpisodeActivity, DividerItemDecoration.VERTICAL))
            adapter = episodeAdapter

        }

        val feed = intent.getStringExtra("url")

        //Traz todos os episódios que estão no Banco de Dados
        //Fica observando as mudanças nos objetos do tipo LiveData
        //Cada episódio será alocado numa lista que será submetida ao adapter
        if (feed != null) {
            episodeViewModel.findByFeed(feed).observe(
                this,
                Observer {
                    episodeAdapter.submitList(it.toList())
                }
            )
            } else {
                Toast.makeText(this, "There are no episodes in this feed!", Toast.LENGTH_SHORT).show()
        }
    }

    //Recebe um broadcast informando que o download foi concluído e vai avisar ao adapter que houve modificação nos dados
    private val onDownloadComplete = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Toast.makeText(binding.root.context, "Download done", Toast.LENGTH_SHORT).show()
            episodeAdapter.notifyDataSetChanged()
        }
    }

    //Retoma o download no caso de sair da activity
    override fun onResume() {
        super.onResume()
        registerReceiver(onDownloadComplete, IntentFilter(EpisodeDetailActivity.DOWNLOAD_COMPLETE))
    }

    override fun onPause() {
        unregisterReceiver(onDownloadComplete)
        super.onPause()
    }

}







