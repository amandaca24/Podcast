package br.ufpe.cin.android.podcast

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding

class EpisodeAdapter(private val inflater: LayoutInflater) : ListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback){

    //RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    class EpisodeViewHolder(private val binding: ItemfeedBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val context = binding.itemTitle.context
                val title = binding.itemTitle.toString()
                val detail = Intent(context, EpisodeDetailActivity::class.java)
                detail.putExtra("title", title)
                context.startActivity(detail)
            }
        }
        fun bindTo(episode: Episode){
            binding.itemTitle.text = episode.titulo
            binding.itemDate.text = episode.dataPublicacao
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemfeedBinding.inflate(inflater, parent, false)
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

}

object EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
    override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
        return oldItem == newItem
    }
    override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
        return oldItem.linkEpisodio == newItem.linkEpisodio
    }
}

