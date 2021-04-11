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

    class EpisodeViewHolder(private val binding: ItemfeedBinding) : RecyclerView.ViewHolder(binding.root) {
        internal var isBound = false

        private var musicPlayerService: MusicPlayerService? = null

        init {
            binding.root.setOnClickListener {
                val context = binding.itemTitle.context
                val title = binding.itemTitle.text.toString()
                val intentEp = Intent(context, EpisodeDetailActivity::class.java)
                intentEp.putExtra("title", title)
                context.startActivity(intentEp)
            }
        }

        fun bindTo(episode: Episode){
            binding.itemTitle.text = episode.titulo
            binding.itemDate.text = episode.dataPublicacao

            if(!episode.linkArquivo.isEmpty()){
                binding.itemPlay.visibility = View.VISIBLE
                binding.itemAction.visibility = View.INVISIBLE
            } else{
                binding.itemAction.visibility = View.VISIBLE
                binding.itemPlay.visibility = View.INVISIBLE
            }

            binding.itemAction.setOnClickListener {
                val context = binding.itemTitle.context
                val title = binding.itemTitle.text.toString()
                val intentDownload = Intent(context, DownloadActivity::class.java).apply {
                    putExtra("titleDownloaded", title)

                }
                context.startActivity(intentDownload)
            }

            binding.itemPlay.setOnClickListener {
                val context = binding.itemTitle.context
                val serviceIntent = Intent(context, MusicPlayerService::class.java)
                context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
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
    }

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

