package com.example.showpokemonlist

import com.example.showpokemonlist.Adapter.PokemonListAdapter
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
        private const val ARG_TYPE = "type"

        fun newInstance(type: String): PokemonType {
            return PokemonType().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(R.layout.fragment_pokemon_type, container, false)
        val type = arguments?.getString(ARG_TYPE)
        type?.let {
            (activity as? MainActivity)?.updateToolbar("TYPE: ${it.uppercase()}", true)
        }

        setupRecyclerView(itemView)
        setupSearchBar(itemView)
        loadPokemonByType()
        setupSearchAdapter()

        return itemView
    }

    private fun setupRecyclerView(view: View) {
        pokemonRecyclerView = view.findViewById(R.id.pokemon_recyclerview)
        pokemonRecyclerView.setHasFixedSize(true)
        pokemonRecyclerView.layoutManager = GridLayoutManager(context, 2)
        val itemDecoration = ItemOffsetDecoration(requireContext(), R.dimen.spacing)
        pokemonRecyclerView.addItemDecoration(itemDecoration)
    }

    private fun setupSearchBar(view: View) {
        searchBar = view.findViewById(R.id.search_bar)
        searchBar.queryHint = getString(R.string.enter_pokemon_name)
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { startSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isEmpty()) resetSearch() else updateSuggestions(it)
                }
                return true
            }
        })
    }

    private fun loadPokemonByType() {
        viewLifecycleOwner.lifecycleScope.launch {
            val type = arguments?.getString(ARG_TYPE)
            if (type != null) {
                typeList = withContext(Dispatchers.Default) {
                    Common.findPokemonByType(type)
                }
                if (typeList.isNotEmpty()) {
                    initializeAdapter()
                    loadSuggest()
                } else {
                    Toast.makeText(context, getString(R.string.no_pokemon_found), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, getString(R.string.pokemon_type_not_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeAdapter() {
        adapter = PokemonListAdapter(requireActivity(), typeList)
        pokemonRecyclerView.adapter = adapter
    }

    private fun setupSearchAdapter() {
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

        searchBar.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int) = true

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = searchAdapter.cursor
                cursor.moveToPosition(position)
                try {
                    val suggestion = cursor.getString(cursor.getColumnIndexOrThrow("suggestion"))
                    searchBar.setQuery(suggestion, true)
                    onPokemonSelected(suggestion)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(context, getString(R.string.error_column_not_found), Toast.LENGTH_SHORT).show()
                }
                return true
            }
        })
    }

    private fun loadSuggest() {
        lastSuggest.clear()
        lastSuggest.addAll(typeList.mapNotNull { it.name })
        updateSuggestionsCursor()
    }

    private fun updateSuggestions(query: String) {
        val suggestions = lastSuggest.filter { it.lowercase().contains(query.lowercase()) }
        updateSuggestionsCursor(suggestions)
    }

    private fun updateSuggestionsCursor(suggestions: List<String> = lastSuggest) {
        val cursor = MatrixCursor(arrayOf("_id", "suggestion"))
        suggestions.forEachIndexed { index, suggestion ->
            cursor.addRow(arrayOf(index, suggestion))
        }
        searchAdapter.changeCursor(cursor)
    }

    private fun startSearch(text: String) {
        if (!::adapter.isInitialized) {
            Toast.makeText(context, "Adapter is not initialized", Toast.LENGTH_SHORT).show()
            return
        }

        val result = typeList.filter { pokemon ->
            pokemon.name?.lowercase()?.contains(text.lowercase()) == true
        }
        adapter.updateList(result)
    }

    private fun resetSearch() {
        if (::adapter.isInitialized) {
            adapter.updateList(typeList)
        }
        searchBar.clearFocus()
    }

    private fun onPokemonSelected(name: String) {
        typeList.find { it.name == name }?.let {
            val detailFragment = PokemonDetail.newInstance(it.num ?: return)
            parentFragmentManager.beginTransaction()
                .replace(R.id.list_pokemon_fragment, detailFragment)
                .addToBackStack(null)
                .commit()
        } ?: Toast.makeText(context, getString(R.string.pokemon_not_found), Toast.LENGTH_SHORT).show()
    }
}
