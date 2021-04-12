package br.ufpe.cin.android.podcast.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.EpisodeActivity
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.databinding.FeedRvBinding

class FeedAdapter(private val inflater: LayoutInflater) : ListAdapter<Feed, FeedAdapter.FeedViewHolder>(FeedDiffer) {

    class FeedViewHolder(private val binding: FeedRvBinding) : RecyclerView.ViewHolder(binding.root){

        fun bindTo(feed: Feed){
            binding.feedTitle.text = feed.titulo
            binding.feedDescription.text = feed.descricao
            binding.feedUrl.text = feed.linkSite

            binding.root.setOnClickListener {
                val context = binding.feedUrl.context
                val url = feed.urlFeed
                val intentFeed = Intent(context, EpisodeActivity::class.java)
                intentFeed.putExtra("url", url)
                context.startActivity(intentFeed)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = FeedRvBinding.inflate(inflater, parent, false)
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bindTo(getItem(position))

    }

    private object FeedDiffer : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }

    }

}
