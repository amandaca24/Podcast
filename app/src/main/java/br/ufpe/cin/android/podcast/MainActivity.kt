package br.ufpe.cin.android.podcast

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.adapters.FeedAdapter
import br.ufpe.cin.android.podcast.data.Episode
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.data.PodcastDatabase
import br.ufpe.cin.android.podcast.databinding.ActivityMainBinding
import br.ufpe.cin.android.podcast.model.EpisodeViewModel
import br.ufpe.cin.android.podcast.model.EpisodeViewModelFactory
import br.ufpe.cin.android.podcast.model.FeedViewModel
import br.ufpe.cin.android.podcast.model.FeedViewModelFactory
import br.ufpe.cin.android.podcast.repositories.EpisodeRepository
import br.ufpe.cin.android.podcast.repositories.FeedRepository
import com.prof.rssparser.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var parser: Parser
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    //private lateinit var lista: List<Episode>

    private val defaultfeed =
        "https://www.omnycontent.com/d/playlist/651a251e-06e1-47e0-9336-ac5a00f41628/220ec2a9-bb93-469e-aad7-acd8013110b7/11cd2462-4550-482d-88ea-acd8013110cf/podcast.rss"

    private val feedViewModel: FeedViewModel by viewModels() {
        val feedRepo = FeedRepository(PodcastDatabase.getDatabase(this).feedDAO())
        FeedViewModelFactory(feedRepo)

    }

    private val episodeViewModel: EpisodeViewModel by viewModels() {
        val episodeRepo = EpisodeRepository(PodcastDatabase.getDatabase(this).episodeDAO())
        EpisodeViewModelFactory(episodeRepo)
    }

    companion object {
        val rss_feed = "RSS Feed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.favorite -> {
                    startActivity(Intent(this, PreferencesActivity::class.java))
                    true
                }

                else -> false
            }
        }


        val recyclerViewFeed = binding.feedView
        val feedAdapter = FeedAdapter(layoutInflater)

        recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = feedAdapter

        }

        feedViewModel.feed.observe(
            this,
            Observer {
                feedAdapter.submitList(it.toList())
            })

        //Apaga o podcast arrastando o item da lista dinâmica
        val mIth = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
                ): Boolean {
                    return true // true if moved, false otherwise
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    scope.launch(Dispatchers.IO) {
                        feedViewModel.delete(feedAdapter.currentList[viewHolder.adapterPosition])
                    }
                    Toast.makeText(this@MainActivity, "Podcast deleted!", Toast.LENGTH_SHORT).show()
                }

            })
        mIth.attachToRecyclerView(recyclerViewFeed)

        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(24L * 60L * 60L * 100L)
            .build()

        feedAdapter.notifyDataSetChanged()

    }

    override fun onStart() {
        super.onStart()
        //Nesta parte é feito o parser do feed.
        //Primeiro pega o link das preferências do usuário (se não for indicado um, pega o link default indicado acima)
        //O parser pega o link e permite pegar os dados do xml. Assim, é possível salvar as informações no Banco de Dados
        val preference: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val podcastFeed = preference.getString(rss_feed, defaultfeed)

        scope.launch {
            val channel = withContext(Dispatchers.IO) {
                podcastFeed?.let { parser.getChannel(it) }
            }

            val show = podcastFeed?.let {
                Feed(
                    podcastFeed.toString(),
                    channel?.title.toString(),
                    channel?.link.toString(),
                    channel?.description.toString(),
                    channel?.image?.url.toString(),
                    160,
                    160 )}

            show?.let { feedViewModel.insert(it) }
            channel?.articles?.forEach { a ->
                var episode = show?.let {
                    Episode(
                        a.link.toString(),
                        a.title.toString(),
                        a.description.toString(),
                        "",
                        a.audio.toString(),
                        a.pubDate.toString(),
                        it?.urlFeed.toString() )}

                if (episode != null) {
                    episodeViewModel.insert(episode) }

                Log.i("FEED URL = ", show?.urlFeed.toString())
                Log.i("FEED TITULO = ", show?.titulo.toString())
                Log.i("FEED DESCRIÇÃO = ", show?.descricao.toString())
                Log.i("FEED IMAGEM = ", show?.imagemURL.toString())
                Log.i("EPISÓDIO LINK = ", episode?.linkEpisodio.toString())
                Log.i("EPISÓDIO TÍTULO = ", episode?.titulo.toString())
                Log.i("EPISÓDIO DESCRIÇÃO = ", episode?.descricao.toString())
                Log.i("EPISÓDIO LINK ARQUIVO = ", episode?.linkArquivo.toString())
                Log.i("EPISÓDIO DATA = ", episode?.dataPublicacao.toString())
                Log.i("EPISÓDIO FEED = ", episode?.feedId.toString())
                Log.i("EPISODIO AUDIO = ", episode?.audio.toString())
            }

            //Limpa o link nas preferências
            preference.edit().clear().apply()
        }
    }

}