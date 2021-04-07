package br.ufpe.cin.android.podcast

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
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

    private var linkUri: Uri? = null
    private var outputUri: Uri? = null
    private lateinit var workId: String

    private val workManager = WorkManager.getInstance(this)

    internal var isBound = false
    internal var TAG = "MusicPlayerBinding"

    private var musicPlayerService : MusicPlayerService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serviceIntent = Intent(this, MusicPlayerService::class.java)

        val title = intent.getStringExtra("title")
        val downloaded = intent.getStringExtra("downloaded")
        val titleDownload = intent.getStringExtra("titleDownloaded")

        if(title != null){
            episodeViewModel.findByTitle(title)
        }
        else {
            if(downloaded == "true" && titleDownload != null){
                episodeViewModel.findByTitle(titleDownload)
                //Configurar restrições

                val downloadRequest =
                    OneTimeWorkRequestBuilder<DownloadEpisodeWorker>()
                        .setInputData(createInputData())
                        .build()

                episodeViewModel.current.observe(
                    this,
                    Observer {
                        binding.titleEpisode.text = it.titulo
                        binding.dateEpisode.text = it.dataPublicacao
                        binding.descriptionEpisode.text = it.descricao
                        binding.linkEpisode.text = it.linkEpisodio
                        binding.feedId.text = it.feedId
                    }
                )
                val _title = binding.titleEpisode.text.toString()
                val _dateEpisode = binding.dateEpisode.text.toString()
                val _description = binding.descriptionEpisode.text.toString()
                val _linkEpisode = binding.linkEpisode.text.toString()
                val _linkArchive = downloadRequest.id.toString()
                val _feedId = binding.feedId.text.toString()

                val episode = Episode(_linkEpisode, _title, _dateEpisode, _description, _linkArchive, _feedId)

                episodeViewModel.update(episode)

                workId = downloadRequest.id.toString()
                workManager.enqueue(downloadRequest)
                val liveData = workManager.getWorkInfoByIdLiveData(downloadRequest.id)

                liveData.observe(
                    this,
                    Observer {
                        var success = false
                        var message = ""
                        when(it.state){
                            WorkInfo.State.SUCCEEDED -> {
                                message = "Download completed"
                                success = true
                                binding.actionsBtn.visibility = View.VISIBLE
                            }
                            WorkInfo.State.CANCELLED -> {
                                message = "Download canceled"
                                //binding.botaoDownload.isEnabled = true
                            }
                            WorkInfo.State.BLOCKED -> {
                                message = "Blocked"
                            }
                            WorkInfo.State.FAILED -> {
                                message = "Download has failed"
                            }
                            WorkInfo.State.RUNNING -> {
                                message = "Running"
                            }
                        }
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this,"That's been some kind of error!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        episodeViewModel.current.observe(
            this,
            Observer {
                binding.titleEpisode.text = it.titulo
                binding.dateEpisode.text = it.dataPublicacao
                binding.descriptionEpisode.text = it.descricao
                binding.linkEpisode.text = it.linkEpisodio
            }
        )

        binding.play.setOnClickListener {
            if (isBound) {
                musicPlayerService?.playMusic()
            } else{
                startService(serviceIntent)
            }

        }

        binding.pause.setOnClickListener {
            if (isBound) {
                musicPlayerService?.pauseMusic()
            }

        }

    }

    override fun onStop() {
        if(isBound){
            unbindService(serviceConnection)
            isBound = false
        }
        super.onStop()

    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isBound = true

            val musicBinder = service as MusicPlayerService.MusicBinder
            musicPlayerService = musicBinder.service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicPlayerService = null
        }

    }

    fun createInputData() : Data {
        val builder = Data.Builder()
        linkUri?.let {
            builder.putString(KEY_LINK_URI,it.toString())
        }
        return builder.build()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /**
     * Setters
     */
    internal fun setLinkUri(uri: String?) {
        linkUri = uriOrNull(uri)
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }
}