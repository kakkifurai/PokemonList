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
import com.example.showpokemonlist.Retrofit.IPokemonList
import com.example.showpokemonlist.Retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokemonType : Fragment() {
    private lateinit var typeList: List<Pokemon>
    private lateinit var pokemonRecyclerView: RecyclerView//pokemonRecyclerViewは、ポケモンのリストを表示するためのRecyclerViewを指します。
    private lateinit var adapter: PokemonListAdapter//adapterは、RecyclerViewにデータを供給する
    private var lastSuggest: MutableList<String> = ArrayList()//lastSuggestは、検索バーに表示するポケモンの名前を保持するリストです。
    private lateinit var searchBar: SearchView//検索バー
    private lateinit var searchAdapter: SimpleCursorAdapter//検索結果を表示するためのアダプター

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

    //フラグメントのビューを作成する際に呼び出されるメソッドです。
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(
            R.layout.fragment_pokemon_list,
            container,
            false
        )

        //RecyclerViewをitemViewから取得し、固定サイズに設定します。
        //GridLayoutManagerを使用して2列のグリッドレイアウトを設定します。
        pokemonRecyclerView = itemView.findViewById(R.id.pokemon_recyclerview)
        pokemonRecyclerView.setHasFixedSize(true)
        pokemonRecyclerView.layoutManager = GridLayoutManager(context, 2)

        //アイテム間のスペースを設定するために、ItemOffsetDecorationを追加します。
        val itemDecoration = ItemOffsetDecoration(requireContext(), R.dimen.spacing)
        pokemonRecyclerView.addItemDecoration(itemDecoration)

        //ポケモンデータを取得するためのメソッドを呼び出します。
       // fetchData()

        // SearchViewの設定(検索)
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


        if (arguments != null) {
            val type = arguments?.getString("type")
            if (type != null) {
                typeList = Common.findPokemonByType(type)
                adapter = PokemonListAdapter(requireActivity(), typeList)
                pokemonRecyclerView.adapter = adapter

                loadSuggest()
            }
        }

        // SimpleCursorAdapterの設定
        //検索結果を表示するためのSimpleCursorAdapterを設定し、SearchViewに関連付けます。
        val from = arrayOf("suggestion")
        val to = intArrayOf(android.R.id.text1)
        searchAdapter = SimpleCursorAdapter(requireContext(), android.R.layout.simple_list_item_1, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        searchBar.suggestionsAdapter = searchAdapter

        // OnSuggestionListenerの設定
        //OnSuggestionListenerを設定し、ユーザーが提案された検索結果をクリックしたときに検索クエリを更新します。
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

        //作成したビューを返します。
        return itemView
    }

    private fun loadSuggest() {
        lastSuggest.clear()
        if (typeList.isNotEmpty()) {
            for (pokemon: Pokemon in typeList) {
                pokemon.name?.let { name ->
                    lastSuggest.add(name)
                }
            }
        }

        // 検索候補のアダプターを更新する
        val cursor = MatrixCursor(arrayOf("_id", "suggestion"))
        lastSuggest.forEachIndexed { index, suggestion ->
            cursor.addRow(arrayOf(index, suggestion))
        }

        if (::searchAdapter.isInitialized) {
            searchAdapter.changeCursor(cursor)
        } else {
            val from = arrayOf("suggestion")
            val to = intArrayOf(android.R.id.text1)
            searchAdapter = SimpleCursorAdapter(requireContext(), android.R.layout.simple_list_item_1, cursor, from, to, 0)
            searchBar.suggestionsAdapter = searchAdapter
        }
    }


    //検索クエリに基づいて提案された検索結果を更新し、カーソルに追加します。
    private fun updateSuggestions(query: String) {
        val suggestions = lastSuggest.filter { it.lowercase().contains(query.lowercase()) }
        val cursor = MatrixCursor(arrayOf("_id", "suggestion"))
        suggestions.forEachIndexed { index, suggestion ->
            cursor.addRow(arrayOf(index, suggestion))
        }
        searchAdapter.changeCursor(cursor)
    }

    //ユーザーの入力に基づいてポケモンを検索し、結果をRecyclerViewに表示します。
    private fun startSearch(text: String) {
        if (typeList.isNotEmpty()) {
            // 検索結果のリストをフィルタリング
            val result = typeList.filter { pokemon ->
                pokemon.name?.lowercase()?.contains(text.lowercase()) == true
            }

            activity?.let { safeActivity ->
                if (::adapter.isInitialized) {
                    // アダプターが既に初期化されている場合はリストを更新
                    adapter.updateList(result)
                } else {
                    // アダプターが初期化されていない場合は新しいアダプターを作成
                    adapter = PokemonListAdapter(safeActivity, result)
                    pokemonRecyclerView.adapter = adapter
                }
            } ?: run {
                // Activity が利用できない場合のエラーハンドリング
                Toast.makeText(context, "Activity is not available", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //検索をリセットして、全てのポケモンを表示します。
    private fun resetSearch() {
        adapter.updateList(Common.pokemonList)
        searchBar.clearFocus()
    }

    //非同期でポケモンデータを取得し、RecyclerViewに設定します。また、lastSuggestリストを更新します。
//    private fun fetchData() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            try {
//                val pokemonDex = withContext(Dispatchers.IO) {
//                    iPokemonList.listPokemon()
//                }
//                Common.pokemonList = pokemonDex.pokemon ?: emptyList()
//                adapter = PokemonListAdapter(requireActivity(), Common.pokemonList)
//                pokemonRecyclerView.adapter = adapter
//
//                lastSuggest.clear()
//                lastSuggest.addAll(Common.pokemonList.mapNotNull { it.name })
//                searchBar.visibility = View.VISIBLE
//            } catch (e: Exception) {
//                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }


    //ユーザーが選択したポケモンを検索し、詳細画面を表示します
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

    //フラグメントが破棄される際に呼ばれるメソッド
    override fun onDestroy() {
        super.onDestroy()
        // Coroutines は自動的にキャンセルされるため、特別な処理は不要
    }
}
