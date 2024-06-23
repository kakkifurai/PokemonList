package com.example.showpokemonlist

import PokemonListAdapter
import android.database.MatrixCursor
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
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
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

    private var lastSuggest: MutableList<String> = ArrayList()
    private lateinit var searchBar: SearchView
    private lateinit var searchAdapter: SimpleCursorAdapter

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
                newText?.let {
                    if (it.isEmpty()) {
                        // 検索ボックスが空になった場合は全てのポケモンを表示
                        resetSearch()
                    } else {
                        updateSuggestions(it)
                    }
                }
                return true
            }
        })

        // SimpleCursorAdapterの設定
        val from = arrayOf("suggestion")
        val to = intArrayOf(android.R.id.text1)
        searchAdapter = SimpleCursorAdapter(requireContext(), android.R.layout.simple_list_item_1, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        searchBar.suggestionsAdapter = searchAdapter

        // OnSuggestionListenerの設定
        searchBar.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = searchAdapter.cursor
                cursor.moveToPosition(position)
                try {
                    val suggestion = cursor.getString(cursor.getColumnIndexOrThrow("suggestion"))
                    searchBar.setQuery(suggestion, true)
                    onPokemonSelected(suggestion)  // ポケモンが選択されたらそのポケモンを表示する
                } catch (e: IllegalArgumentException) {
                    // 列が見つからない場合の処理
                    Toast.makeText(context, "Error: Column not found", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        })

        return itemView
    }

    private fun updateSuggestions(query: String) {
        val suggestions = lastSuggest.filter { it.lowercase().contains(query.lowercase()) }
        val cursor = MatrixCursor(arrayOf("_id", "suggestion"))
        suggestions.forEachIndexed { index, suggestion ->
            cursor.addRow(arrayOf(index, suggestion))
        }
        searchAdapter.changeCursor(cursor)
    }

    private fun startSearch(text: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.Default) {
                Common.pokemonList.filter { it.name?.lowercase()?.contains(text.lowercase()) == true }
            }
            adapter.updateList(result)
        }
    }

    private fun resetSearch() {
        adapter.updateList(Common.pokemonList)
        searchBar.clearFocus()
    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val pokemonDex = withContext(Dispatchers.IO) {
                    iPokemonList.listPokemon()
                }
                Common.pokemonList = pokemonDex.pokemon ?: emptyList()
                adapter = PokemonListAdapter(requireActivity(), Common.pokemonList)
                pokemonRecyclerView.adapter = adapter

                lastSuggest.clear()
                lastSuggest.addAll(Common.pokemonList.mapNotNull { it.name })
                searchBar.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onPokemonSelected(name: String) {
        val selectedPokemon = Common.pokemonList.find { it.name == name }
        selectedPokemon?.let {
            val detailFragment = PokemonDetail.newInstance(it.num!!)
            val fragmentTransaction = parentFragmentManager.beginTransaction()

            // フラグメントトランザクションの設定
            fragmentTransaction.replace(R.id.list_pokemon_fragment, detailFragment) // 修正箇所
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        } ?: run {
            Toast.makeText(context, "Pokemon not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Coroutines は自動的にキャンセルされるため、特別な処理は不要
    }
}
