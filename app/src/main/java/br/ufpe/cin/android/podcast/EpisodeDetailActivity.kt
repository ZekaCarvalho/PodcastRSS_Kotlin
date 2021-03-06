package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeDetailBinding
class EpisodeDetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityEpisodeDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val description = intent.getStringExtra("description")
        val link = intent.getStringExtra("link")

        if (description != null ){
            binding.descricao.text = description
        }

        if ( link != null ){
            binding.linkEpisodio.text = link
        }


    }
}