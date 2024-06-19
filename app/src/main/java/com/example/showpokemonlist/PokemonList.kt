package com.example.showpokemonlist

import PokemonListAdapter
import com.example.showpokemonlist.Common.Common
import com.example.showpokemonlist.Common.ItemOffsetDecoration
import com.example.showpokemonlist.Retrofit.IPokemonList
import com.example.showpokemonlist.Retrofit.RetrofitClient
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import kotlinx.coroutines.*

class PokemonList : Fragment() {

    private lateinit var iPokemonList: IPokemonList
    private lateinit var pokemonRecyclerView: RecyclerView
    private lateinit var adapter: PokemonListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(R.layout.fragment_pokemon_list, container, false)

        val retrofit = RetrofitClient.instance
        iPokemonList = retrofit.create(IPokemonList::class.java)

        pokemonRecyclerView = itemView.findViewById(R.id.pokemon_recyclerview)
        pokemonRecyclerView.setHasFixedSize(true)
        pokemonRecyclerView.layoutManager = GridLayoutManager(context, 2)

        val itemDecoration = ItemOffsetDecoration(requireContext(), R.dimen.spacing)
        pokemonRecyclerView.addItemDecoration(itemDecoration)

        fetchData()

        return itemView
    }

    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemonDex = iPokemonList.listPokemon()
                withContext(Dispatchers.Main) {
                    Common.pokemonList = pokemonDex.pokemon!!
                    adapter = PokemonListAdapter(requireActivity(), Common.pokemonList)
                    pokemonRecyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Coroutines は自動的にキャンセルされるため、特別な処理は不要
    }
}
