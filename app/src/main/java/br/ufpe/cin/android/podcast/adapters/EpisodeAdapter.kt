package br.ufpe.cin.android.podcast.adapters

import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.EpisodeDetailActivity
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding
import br.ufpe.cin.android.podcast.services.MusicPlayerService
import java.io.File

class EpisodeAdapter(private val inflater: LayoutInflater) :
    ListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback) {
    //A classe view holder é o fixador de visualização. O atributo binding está chamando o id do itemfeed.xml,
    // onde estão os componentes que serão vinculados à lista dinâmica

    class EpisodeViewHolder(private val binding: ItemfeedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //Aqui são feitos os bindings entre os componentes da view e o episódio salvo no BD
        fun bindTo(episode: Episode) {
            binding.itemTitle.text = episode.titulo
            binding.itemDate.text = episode.dataPublicacao

            val context = binding.itemTitle.context
            val title = episode.titulo

            //Torna o card clicável
            //Ele vai receber o contexto e o texto do episódio
            //O título será passado para a activity intencionada (EpisodDetailActivity) por meio do método putExtra
            binding.root.setOnClickListener {
                val intentEp = Intent(context, EpisodeDetailActivity::class.java).apply {
                    putExtra("title", title)
                    putExtra("detail", "true")
                }
                context.startActivity(intentEp)
            }

            //Vê se o episódio está baixado localmente
            //Se não, irá renderizar o botão para download do episódio
            //Ao terminar o download, o botão de play será renderizado
            //Clicando no botão download, ele irá abrir a activity de detalhes do episódio
            if (episode.linkArquivo.isEmpty()) {
                binding.itemAction.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24)
                binding.itemAction.setOnClickListener {
                    binding.itemAction.isEnabled = false
                    val intentDownload = Intent(context, EpisodeDetailActivity::class.java).apply {
                        putExtra("title", title)
                        putExtra("detail", "false")
                    }
                    context.startActivity(intentDownload)
                }
            } else {
                //Com o episódio baixado localmente, o botão de play será renderizado.
                // Ao clicar em play, o episódio baixado será tocado
                var play = false
                binding.itemAction.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                binding.itemAction.isEnabled = true
                binding.itemAction.setOnClickListener {
                    if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                        val audioFile = File(episode.linkArquivo)
                        if (audioFile.exists()) {
                            if (!play) {
                                val i = Intent(
                                    binding.root.context,
                                    MusicPlayerService::class.java
                                )
                                i.putExtra("audio", episode.linkArquivo)
                                binding.root.context.startService(i)
                                play = true
                                binding.itemAction.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                                binding.itemAction.setOnClickListener {

                                }
                            } else {
                                binding.root.context.stopService(
                                    Intent(
                                        binding.root.context,
                                        MusicPlayerService::class.java
                                    )
                                )
                                play = false
                            }
                        } else {
                            Toast.makeText(
                                binding.root.context,
                                "This file does not exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            binding.root.context,
                            "External environment is not mounted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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

