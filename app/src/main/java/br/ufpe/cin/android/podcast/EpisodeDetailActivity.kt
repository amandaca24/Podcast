package br.ufpe.cin.android.podcast

import android.content.*
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
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeDetailBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import br.ufpe.cin.android.podcast.model.EpisodeViewModelFactory
import br.ufpe.cin.android.podcast.repositories.EpisodeRepository
import br.ufpe.cin.android.podcast.services.MusicPlayerService
import br.ufpe.cin.android.podcast.utils.KEY_LINK_URI
import com.squareup.picasso.Picasso

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

                    binding.actionsBtn.visibility = View.INVISIBLE

                    val img = Uri.parse(it.episodeImage)

                    Picasso.get().load(img).into(binding.imgEpisode)
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

                    val img = Uri.parse(it.episodeImage)

                    Picasso.get().load(img).into(binding.imgEpisode)

                    downloadEp(it.audio)
                    Log.i("EPISODE AUDIO = ", it.audio)

                })

        } else {
            Toast.makeText(this, "That's been some kind of error!", Toast.LENGTH_SHORT).show()
            finish()
        }

        episodeViewModel.current.observe(
            this,
            Observer {
                if (it.linkArquivo.isNotEmpty()) {
                    binding.actionsBtn.visibility = View.VISIBLE
                    val i = Intent(
                        binding.root.context,
                        MusicPlayerService::class.java
                    )
                    i.putExtra("audio", it.linkArquivo)

                }
            })

        checkDownload()
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
            }
        }

        binding.stopBtn.setOnClickListener {
            if (isBound) {
                musicPlayerService?.stopMusic()
            }
        }

        binding.rewindBtn.setOnClickListener {
            if (isBound) {
                musicPlayerService?.rewindMusic()
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
                    it.episodeImage,
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

    fun checkDownload() {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val audio = outputUri.toString()

            if (!audio.isNullOrEmpty()) {
                musicPlayerService?.let { }

                musicPlayerService?.startMusic(audio)
                isBound = true
            }

        }
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

    //Métodos só pra ajustar o input/output do download, passando String para Uri
    // e fazendo o build do dado
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

    internal fun setLinkUri(uri: String?) {
        linkUri = uriOrNull(uri)
    }

    internal fun setOutputUri(outUri: String?) {
        outputUri = uriOrNull(outUri)
    }

}