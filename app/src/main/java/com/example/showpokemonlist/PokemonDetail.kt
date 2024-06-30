package com.example.showpokemonlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.showpokemonlist.Adapter.PokemonEvolutionAdapter
import com.example.showpokemonlist.Adapter.PokemonTypeAdapter
import com.example.showpokemonlist.Common.Common
import com.example.showpokemonlist.Model.Evolution
import com.example.showpokemonlist.Model.Pokemon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val pokemon = withContext(Dispatchers.Default) {
                    Common.findPokemonByNum(num)
                }
                if (pokemon != null) {
                    setDetailPokemon(pokemon)
                } else {
                    showErrorMessage("Pokemon not found")
                }
            } catch (e: Exception) {
                showErrorMessage("Error loading Pokemon details: ${e.message}")
            }
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        // オプション: エラーメッセージを表示するUIを更新
    }

    private fun setDetailPokemon(pokemon: Pokemon) {
        // Glideで画像を読み込む
        Glide.with(this).load(pokemon.img).into(pokemon_img)

        // 日本語名を取得して設定
        val pokemonJapaneseName = pokemon.name?.let { Common.getPokemonNameInJapanese(requireContext(), it) } ?: "不明なポケモン"
        pokemon_name.text = pokemonJapaneseName
        pokemon_height.text = "高さ: ${pokemon.height}"
        pokemon_weight.text = "重さ: ${pokemon.weight}"

        val typeAdapter = PokemonTypeAdapter(requireActivity(), pokemon.type ?: emptyList())
        recycler_type.adapter = typeAdapter

        val weaknessAdapter = PokemonTypeAdapter(requireActivity(), pokemon.weaknesses ?: emptyList())
        recycler_weakness.adapter = weaknessAdapter

        // 進化前のポケモン名を日本語に変換してリストに格納
        val prevEvolutionList = pokemon.prev_evolution?.map { evolution ->
            val japaneseName = evolution.name?.let { Common.getPokemonNameInJapanese(requireContext(), it) } ?: ""
            Evolution(
                num = evolution.num ?: "",
                name = japaneseName
            )
        } ?: emptyList()

        // 進化後のポケモン名を日本語に変換してリストに格納
        val nextEvolutionList = pokemon.next_evolution?.map { evolution ->
            Evolution(
                num = evolution.num,
                name = Common.getPokemonNameInJapanese(requireContext(), evolution.name)
            )
        } ?: emptyList()

        val prevEvolutionAdapter = PokemonEvolutionAdapter(requireActivity(), prevEvolutionList)
        recycler_prev_evolution.adapter = prevEvolutionAdapter

        val nextEvolutionAdapter = PokemonEvolutionAdapter(requireActivity(), nextEvolutionList)
        recycler_next_evolution.adapter = nextEvolutionAdapter
    }

}
