package br.ufpe.cin.android.podcast

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.PodcastDatabase
import br.ufpe.cin.android.podcast.databinding.ActivityDownloadBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import br.ufpe.cin.android.podcast.model.EpisodeViewModelFactory
import br.ufpe.cin.android.podcast.repositories.EpisodeRepository
import br.ufpe.cin.android.podcast.services.MusicPlayerService
import br.ufpe.cin.android.podcast.utils.KEY_LINK_URI

class DownloadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadBinding

    private var linkUri: Uri? = null
    private var outputUri: Uri? = null
    private lateinit var workId: String

    private val workManager = WorkManager.getInstance(this)

    internal var isBound = false

    private var musicPlayerService: MusicPlayerService? = null

    private val episodeViewModel: EpisodeViewModel by viewModels {
        val repo = EpisodeRepository(PodcastDatabase.getDatabase(this).episodeDAO())
        EpisodeViewModelFactory(repo)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titleDownload = intent.getStringExtra("titleDownloaded")

        if (titleDownload != null) {
            episodeViewModel.findByTitle(titleDownload)
        }

        episodeViewModel.current.observe(
            this,
            Observer {
                binding.epDownloaded.text = it.titulo
                binding.archive.text = it.linkEpisodio

                val texturi = binding.archive.text.toString()
                Log.i("TEXTO LINK = ", texturi)
                downloadEp(texturi)

            })


        binding.playBtn.setOnClickListener {
            val serviceIntent = Intent(this, MusicPlayerService::class.java)
            if (isBound) {
                musicPlayerService?.playMusic()
            } else {
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            }

        }

        binding.pauseBtn.setOnClickListener {
            if (isBound) {
                musicPlayerService?.pauseMusic()
            } else {
                Toast.makeText(this, "You must play the episode first", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStart() {
        binding.updateBtn.setOnClickListener {
            updateEpisode()
        }
        super.onStart()
    }

    fun downloadEp(uri: String?) {

        setLinkUri(uri)

        Log.i("URI BY OBSERVER = ", linkUri.toString())

        val downloadRequest =
            OneTimeWorkRequestBuilder<DownloadEpisodeWorker>()
                .setInputData(createInputData())
                .build()

        workId = downloadRequest.id.toString()
        workManager.enqueue(downloadRequest)
        val liveData = workManager.getWorkInfoByIdLiveData(downloadRequest.id)

        liveData.observe(
            this,
            Observer {
                var success = false
                var message = ""
                when (it.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        message = "Download completed"
                        success = true
                        setOutputUri(it.outputData.toString())
                        binding.actionsBtn.visibility = View.VISIBLE
                    }
                    WorkInfo.State.CANCELLED -> {
                        message = "Download canceled"
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
                if (success) {
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                }
            }
        )
    }

    fun updateEpisode(){
        episodeViewModel.current.observe(
            this,
            Observer {
                val episode = Episode(
                    it.linkEpisodio,
                    it.titulo,
                    it.dataPublicacao,
                    it.descricao,
                    outputUri.toString(),
                    it.feedId)

                episodeViewModel.update(episode)
                finish()

                var current = episode.linkArquivo
                Log.i("CURRENT = ", current)
            })
    }

    override fun onStop() {
        if (isBound) {
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

    fun createInputData(): Data {
        val builder = Data.Builder()
        linkUri?.let {
            builder.putString(KEY_LINK_URI, it.toString())
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

