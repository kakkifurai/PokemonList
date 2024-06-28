package com.example.showpokemonlist

import PokemonListAdapter
import android.database.MatrixCursor
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.showpokemonlist.Common.Common
import com.example.showpokemonlist.Common.ItemOffsetDecoration
import com.example.showpokemonlist.Model.Pokemon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokemonType : Fragment() {
    private lateinit var typeList: List<Pokemon>
    private lateinit var pokemonRecyclerView: RecyclerView
    private lateinit var adapter: PokemonListAdapter
    private var lastSuggest: MutableList<String> = ArrayList()
    private lateinit var searchBar: SearchView
    private lateinit var searchAdapter: SimpleCursorAdapter

    companion object {
        private const val ARG_NUM = "num"

        fun newInstance(num: String): PokemonType {
            val fragment = PokemonType()
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
        val itemView = inflater.inflate(
            R.layout.fragment_pokemon_type,
            container,
            false
        )

        // RecyclerViewの設定
        pokemonRecyclerView = itemView.findViewById(R.id.pokemon_recyclerview)
        pokemonRecyclerView.setHasFixedSize(true)
        pokemonRecyclerView.layoutManager = GridLayoutManager(context, 2)
        val itemDecoration = ItemOffsetDecoration(requireContext(), R.dimen.spacing)
        pokemonRecyclerView.addItemDecoration(itemDecoration)

        // 検索バーの設定
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
                        resetSearch()
                    } else {
                        updateSuggestions(it)
                    }
                }
                return true
            }
        })

        arguments?.getString("type")?.let { type ->
            typeList = Common.findPokemonByType(type)
            adapter = PokemonListAdapter(requireActivity(), typeList)
            pokemonRecyclerView.adapter = adapter
            loadSuggest()
        } ?: run {
            // 引数がない場合やtypeがnullの場合の処理
            Toast.makeText(context, "Pokemon type not found", Toast.LENGTH_SHORT).show()
        }

        // 検索候補のアダプターの設定
        val from = arrayOf("suggestion")
        val to = intArrayOf(android.R.id.text1)
        searchAdapter = SimpleCursorAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            null,
            from,
            to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        searchBar.suggestionsAdapter = searchAdapter

        // 検索候補のクリックリスナーの設定
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
                    onPokemonSelected(suggestion)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(context, "Error: Column not found", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        })

        return itemView
    }

    private fun loadSuggest() {
        lastSuggest.clear()
        if (typeList.isNotEmpty()) {
            typeList.forEach { pokemon ->
                pokemon.name?.let { lastSuggest.add(it) }
            }
        }

        val cursor = MatrixCursor(arrayOf("_id", "suggestion"))
        lastSuggest.forEachIndexed { index, suggestion ->
            cursor.addRow(arrayOf(index, suggestion))
        }

        searchAdapter.changeCursor(cursor)
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
        if (typeList.isNotEmpty()) {
            val result = typeList.filter { pokemon ->
                pokemon.name?.lowercase()?.contains(text.lowercase()) == true
            }

            if (::adapter.isInitialized) {
                adapter.updateList(result)
                adapter.notifyDataSetChanged()
            } else {
                adapter = PokemonListAdapter(requireActivity(), result)
                pokemonRecyclerView.adapter = adapter
            }
        } else {
            Toast.makeText(context, "No Pokemon found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetSearch() {
        adapter.updateList(typeList)
        adapter.notifyDataSetChanged()
        searchBar.clearFocus()
    }

    private fun onPokemonSelected(name: String) {
        val selectedPokemon = typeList.find { it.name == name }
        selectedPokemon?.let {
            val detailFragment = PokemonDetail.newInstance(it.num!!)
            parentFragmentManager.beginTransaction()
                .replace(R.id.list_pokemon_fragment, detailFragment)
                .addToBackStack(null)
                .commit()
        } ?: run {
            Toast.makeText(context, "Pokemon not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
