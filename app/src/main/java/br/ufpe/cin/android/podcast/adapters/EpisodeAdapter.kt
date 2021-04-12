package br.ufpe.cin.android.podcast.adapters

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.DownloadActivity
import br.ufpe.cin.android.podcast.EpisodeDetailActivity
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding
import br.ufpe.cin.android.podcast.services.MusicPlayerService

class EpisodeAdapter(private val inflater: LayoutInflater) : ListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback) {
    //A classe view holder é o fixador de visualização. O atributo binding está chamando o id do itemfeed.xml,
    // onde estão os componentes que serão vinculados à lista dinâmica

    class EpisodeViewHolder(private val binding: ItemfeedBinding) : RecyclerView.ViewHolder(binding.root) {

        //Variáveis relacionadas ao Service de tocar o episódio. isBound vai indicar se o service está ligado ao contexto
        internal var isBound = false
        //Iniciando o service como null
        private var musicPlayerService: MusicPlayerService? = null

        //Inicializa o binding na raiz, que no caso é o componente itemfeed.xml, tornando-o clicável
        //Ele vai receber o contexto e o texto atribuídos ao textview de título.
        //O título será passado para a activity intencionada (EpisodDetailActivity) por meio do método putExtra
        init {
            binding.root.setOnClickListener {
                val context = binding.itemTitle.context
                val title = binding.itemTitle.text.toString()
                val intentEp = Intent(context, EpisodeDetailActivity::class.java)
                intentEp.putExtra("title", title)
                context.startActivity(intentEp)
            }
        }

        //Aqui são feitos os bindings entre os componentes da view e o episódio salvo no BD
        fun bindTo(episode: Episode){
            binding.itemTitle.text = episode.titulo
            binding.itemDate.text = episode.dataPublicacao

            //Vê se o episódio está baixado na memória. Caso positivo, irá renderizar apenas o botão play.
            //Se não, irá renderizar o botão para download do episódio
            if(!episode.linkArquivo.isEmpty()){
                binding.itemPlay.visibility = View.VISIBLE
                binding.itemAction.visibility = View.INVISIBLE
            } else{
                binding.itemAction.visibility = View.VISIBLE
                binding.itemPlay.visibility = View.INVISIBLE
            }

            //Torna o botão de download clicável, cuja ação inicia a activity de Download.
            //O putExtra foi usado dentro do método apply. Dessa forma, pode-se passar mais de um extra na mesma ação
            binding.itemAction.setOnClickListener {
                val context = binding.itemTitle.context
                val title = binding.itemTitle.text.toString()
                val intentDownload = Intent(context, DownloadActivity::class.java).apply {
                    putExtra("titleDownloaded", title)

                }
                context.startActivity(intentDownload)
            }

            //Ação do botão play para iniciar o Service de tocar o episódio
            binding.itemPlay.setOnClickListener {
                val context = binding.itemTitle.context
                val serviceIntent = Intent(context, MusicPlayerService::class.java)
                context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }

        //Objeto que inicia a conexão do service
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
    }

    //Aqui são implementados os métodos obrigatórios do adapter
    //O primeiro cria o fixador de visualização, indicando qual layout será ligado (ItemFeedBinding)
    //O segundo associa o fixador aos dados, preenchendo-o de acordo com o que foi informado no método bindTo
    //O último trata repetição de itens e conteúdos
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemfeedBinding.inflate(inflater, parent, false)
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    object EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.linkEpisodio == newItem.linkEpisodio
        }
    }
}

