package br.ufpe.cin.android.podcast

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import java.io.File

class DownloadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadBinding

    private var linkUri: Uri? = null
    private var outputUri: Uri? = null
    private lateinit var workId: String

    private val workManager = WorkManager.getInstance(this)

    internal var isBound = false

    private var musicPlayerService: MusicPlayerService? = null

    //Inicializa o objeto do ViewModel para acessar os dados, garantindo a integridade deles
    private val episodeViewModel: EpisodeViewModel by viewModels {
        val repo = EpisodeRepository(PodcastDatabase.getDatabase(this).episodeDAO())
        EpisodeViewModelFactory(repo)
    }

    companion object {
        val DOWNLOAD_COMPLETE = "br.ufpe.android.podcast.DOWNLOAD_COMPLETE"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Pega o título do espisódio que foi passado via intent ao clicar no botão de download
        val titleDownload = intent.getStringExtra("titleDownloaded")

        //Verifica se não está null. Caso positivo, vai procurar o episódio pelo título
        if (titleDownload != null) {
            episodeViewModel.findByTitle(titleDownload)
        }

        //Vai pegar o episódio corrente e fazer o bind nos componentes da view
        //Também vai fazer o download da mp3 e atualizar o episódio, com o caminho onde ficou baixado
        episodeViewModel.current.observe(
            this,
            Observer {
                binding.epDownloaded.text = it.titulo
                binding.linkAudio.text = it.audio

                val texturi = it.audio
                Log.i("TEXTO LINK = ", texturi)
                downloadEp(texturi)

                binding.archive.visibility = View.INVISIBLE
                binding.linkAudio.visibility = View.INVISIBLE

            })
        Log.i("EPISODE ARCHIVE PATH = ", binding.archive.toString())

    }

    override fun onStart() {
        val serviceIntent = Intent(this, MusicPlayerService::class.java)
        //serviceIntent.putExtra("audio", binding.archive.toString())

        //Faz o bind com os botões de ação play e pause
        binding.playBtn.setOnClickListener {
            if (isBound) {
                musicPlayerService?.playMusic()
            } else {
                startService(serviceIntent)
            }
        }


        binding.pauseBtn.setOnClickListener {
            if (isBound) {
                musicPlayerService?.pauseMusic()
            } else {
                Toast.makeText(this, "You must play the episode first", Toast.LENGTH_SHORT).show()
            }
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
                        sendBroadcast(Intent(DOWNLOAD_COMPLETE))
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

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (success) {
                    updateEpisode()
                    val intentPlay = Intent(this, DownloadActivity::class.java)
                    intentPlay.putExtra("play", true)
                    startActivity(intentPlay)
                    Toast.makeText(this, "Episode updated and ready to play", Toast.LENGTH_SHORT).show()

                }
            }
        )
    }

    private fun updateEpisode(){
        episodeViewModel.current.observe(
            this,
            Observer {
                val episode = Episode(
                    it.linkEpisodio,
                    it.titulo,
                    it.descricao,
                    outputUri.toString(),
                    it.audio,
                    it.dataPublicacao,
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

    internal fun setOutputUri(outUri: String?) {
        outputUri = uriOrNull(outUri)
    }
}

