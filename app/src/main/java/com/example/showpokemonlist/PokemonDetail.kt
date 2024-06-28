package com.example.showpokemonlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.showpokemonlist.Adapter.PokemonEvolutionAdapter
import com.example.showpokemonlist.Adapter.PokemonTypeAdapter
import com.example.showpokemonlist.Common.Common
import com.example.showpokemonlist.Model.Pokemon

class PokemonDetail : Fragment() {

    private lateinit var pokemon_img: ImageView
    private lateinit var pokemon_name: TextView
    private lateinit var pokemon_height: TextView
    private lateinit var pokemon_weight: TextView

    private lateinit var recycler_type: RecyclerView
    private lateinit var recycler_weakness: RecyclerView
    private lateinit var recycler_prev_evolution: RecyclerView
    private lateinit var recycler_next_evolution: RecyclerView

    companion object {
        private const val ARG_NUM = "num"

        fun newInstance(num: String): PokemonDetail {
            val fragment = PokemonDetail()
            val args = Bundle()
            args.putString(ARG_NUM, num)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(R.layout.fragment_pokemon_detail, container, false)

        // Viewの初期化
        pokemon_img = itemView.findViewById(R.id.pokemon_image)
        pokemon_name = itemView.findViewById(R.id.pokemon_name)
        pokemon_height = itemView.findViewById(R.id.pokemon_height)
        pokemon_weight = itemView.findViewById(R.id.pokemon_weight)

        recycler_type = itemView.findViewById(R.id.recycler_type)
        recycler_type.setHasFixedSize(true)
        recycler_type.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        recycler_weakness = itemView.findViewById(R.id.recycler_weakness)
        recycler_weakness.setHasFixedSize(true)
        recycler_weakness.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        recycler_prev_evolution = itemView.findViewById(R.id.recycler_prev_evolution)
        recycler_prev_evolution.setHasFixedSize(true)
        recycler_prev_evolution.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        recycler_next_evolution = itemView.findViewById(R.id.recycler_next_evolution)
        recycler_next_evolution.setHasFixedSize(true)
        recycler_next_evolution.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        return itemView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val num = requireArguments().getString(ARG_NUM)
        val pokemon: Pokemon? = Common.findPokemonByNum(num)

        pokemon?.let {
            setDetailPokemon(it)
        }
    }

    private fun setDetailPokemon(pokemon: Pokemon) {
        // Glideで画像を読み込む
        Glide.with(this).load(pokemon.img).into(pokemon_img)

        // テキストを設定
        pokemon_name.text = pokemon.name
        pokemon_height.text = "Height: ${pokemon.height}"
        pokemon_weight.text = "Weight: ${pokemon.weight}"

        val typeAdapter = PokemonTypeAdapter(requireActivity(), pokemon.type ?: emptyList())
        recycler_type.adapter = typeAdapter

        val weaknessAdapter = PokemonTypeAdapter(requireActivity(), pokemon.weaknesses ?: emptyList())
        recycler_weakness.adapter = weaknessAdapter

        val prevEvolutionAdapter = PokemonEvolutionAdapter(requireActivity(), pokemon.prev_evolution ?: emptyList())
        recycler_prev_evolution.adapter = prevEvolutionAdapter

        val nextEvolutionAdapter = PokemonEvolutionAdapter(requireActivity(), pokemon.next_evolution ?: emptyList())
        recycler_next_evolution.adapter = nextEvolutionAdapter
    }
}
