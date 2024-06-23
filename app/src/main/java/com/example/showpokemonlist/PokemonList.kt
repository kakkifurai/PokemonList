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
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import kotlinx.coroutines.*

class PokemonList : Fragment() {

    private lateinit var iPokemonList: IPokemonList
    private lateinit var pokemonRecyclerView: RecyclerView
    private lateinit var adapter: PokemonListAdapter

    private var last_suggest: MutableList<String> = ArrayList()
    private lateinit var searchBar: SearchView

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

        // SearchViewの設定
        searchBar = itemView.findViewById(R.id.search_bar)
        searchBar.queryHint = "Enter Pokemon Name"
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { startSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { updateSuggestions(it) }
                return true
            }
        })
        return itemView
    }

    private fun updateSuggestions(query: String) {
        val suggestions = last_suggest.filter { it.lowercase().contains(query.lowercase()) }
        // ここで検索候補を表示する処理を実装
    }

    private fun startSearch(text: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.Default) {
                Common.pokemonList.filter { it.name?.lowercase()?.contains(text.lowercase()) == true }
            }
            adapter.updateList(result)
        }
    }


    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemonDex = iPokemonList.listPokemon()
                withContext(Dispatchers.Main) {
                    Common.pokemonList = pokemonDex.pokemon!!
                    adapter = PokemonListAdapter(requireActivity(), Common.pokemonList)
                    pokemonRecyclerView.adapter = adapter

                    last_suggest = Common.pokemonList.mapNotNull { it.name }.toMutableList()
                    searchBar.visibility = View.VISIBLE
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
