package br.ufpe.cin.android.podcast

import android.content.*
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
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeDetailBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import br.ufpe.cin.android.podcast.model.EpisodeViewModelFactory
import br.ufpe.cin.android.podcast.repositories.EpisodeRepository
import br.ufpe.cin.android.podcast.services.MusicPlayerService
import br.ufpe.cin.android.podcast.utils.KEY_LINK_URI

class EpisodeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEpisodeDetailBinding

    //Objeto para o Broadcast Receiver
    companion object {
        val DOWNLOAD_COMPLETE = "br.ufpe.android.podcast.DOWNLOAD_COMPLETE"
    }

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
        binding = ActivityEpisodeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serviceIntent = Intent(this, MusicPlayerService::class.java)
        //Pega o título do espisódio que foi passado via intent ao clicar no card ou no botão download
        val title = intent.getStringExtra("title")
        val detail = intent.getStringExtra("detail")

        //Verifica se não está null e procura o episódio com o título informado
        //Fará o binding com os componentes da view
        //Esta condicional verifica se a intent veio de clicar no card. Assim, não vai baixar o mp3 do episódio
        if (title != null && detail.equals("true")) {
            episodeViewModel.findByTitle(title)

            episodeViewModel.current.observe(
                this,
                Observer {
                    binding.titleEpisode.text = it.titulo
                    binding.dateEpisode.text = it.dataPublicacao
                    binding.descriptionEpisode.text = it.descricao
                    binding.linkEpisode.text = it.linkEpisodio

                    if (it.linkArquivo != "") {
                        binding.actionsBtn.visibility = View.VISIBLE
                        val i = Intent(
                            binding.root.context,
                            MusicPlayerService::class.java
                        )
                        i.putExtra("audio", it.linkArquivo)
                    }
                    binding.actionsBtn.visibility = View.INVISIBLE

                })
            //Vai trabalhar a visibilidade dos botões de play e pause dinamicamente

        } else if (title != null && detail.equals("false")) {
            episodeViewModel.findByTitle(title)

            episodeViewModel.current.observe(
                this,
                Observer {
                    binding.titleEpisode.text = it.titulo
                    binding.dateEpisode.text = it.dataPublicacao
                    binding.descriptionEpisode.text = it.descricao
                    binding.linkEpisode.text = it.linkEpisodio

                    downloadEp(it.audio)
                    Log.i("EPISODE AUDIO = ", it.audio)

                })

        } else {
            Toast.makeText(this, "That's been some kind of error!", Toast.LENGTH_SHORT).show()
            finish()
        }

        //Vai ver se o serviço já está funcionando.
        //Se não, vai inicializá-lo
        binding.playBtn.setOnClickListener {
            serviceIntent.putExtra("audio", episodeViewModel.current.value?.linkArquivo)
            if (isBound) {
                musicPlayerService?.playMusic()
            } else {
                startService(serviceIntent)
            }
        }


        //Ao clicar, se o episódio estiver tocando, vai pausar
        binding.pauseBtn.setOnClickListener {
            if (isBound) {
                musicPlayerService?.pauseMusic()
            } else {
                Toast.makeText(this, "You must play the episode first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Este é o método que vai chamar o DownloadEpisodeWorker e passará o atributo uri que será baixado localmente.
    //Com a variável workManager, que vai enfileirar o processo de download, é possível acompanhar o processo
    //feito pela liveData e lançar uma mensagem para o usuário de acordo o estado do download.
    //Em caso de sucesso, vai atribuir a saída a uma variável, que será usada para fazer a atualização do atributo linkArquivo
    //Também vai enviar um broadcast avisando que o download foi realizado com sucesso
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
                        setOutputUri(it.outputData.getString("KEY_IMAGEFILE_URI"))
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
                    binding.actionsBtn.visibility = View.VISIBLE
                    sendBroadcast(Intent(DOWNLOAD_COMPLETE))

                }
            }
        )
    }

    private fun updateEpisode() {
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
                    it.feedId
                )

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