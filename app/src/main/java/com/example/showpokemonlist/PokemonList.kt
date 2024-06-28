package com.example.showpokemonlist

import com.example.showpokemonlist.Adapter.PokemonListAdapter
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

    private lateinit var iPokemonList: IPokemonList//iPokemonListは、ポケモンのリストを取得するためのインターフェースです。lateinitキーワードを使用して後から初期化
    private lateinit var pokemonRecyclerView: RecyclerView//pokemonRecyclerViewは、ポケモンのリストを表示するためのRecyclerViewを指します。
    private lateinit var adapter: PokemonListAdapter//adapterは、RecyclerViewにデータを供給する
    private var lastSuggest: MutableList<String> = ArrayList()//lastSuggestは、検索バーに表示するポケモンの名前を保持するリストです。
    private lateinit var searchBar: SearchView//検索バー
    private lateinit var searchAdapter: SimpleCursorAdapter//検索結果を表示するためのアダプター

    companion object{
        internal var instance:PokemonList? = null


        fun getInstance():PokemonList{
            if (instance == null)
                instance = PokemonList()
            return  instance!!
        }

        fun newInstance(): Fragment {
            return PokemonList()
        }
    }

    //フラグメントのビューを作成する際に呼び出されるメソッドです。
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(R.layout.fragment_pokemon_list, container, false)//レイアウトファイルfragment_pokemon_list.xmlをインフレートして、ビューを作成します。

        //Retrofitクライアントを使用してIPokemonListインターフェースのインスタンスを取得します。
        //Retrofitとは、型安全な Android 向けの HTTP クライアントライブラリ
        val retrofit = RetrofitClient.instance
        iPokemonList = retrofit.create(IPokemonList::class.java)

        //RecyclerViewをitemViewから取得し、固定サイズに設定します。
        //GridLayoutManagerを使用して2列のグリッドレイアウトを設定します。
        pokemonRecyclerView = itemView.findViewById(R.id.pokemon_recyclerview)
        pokemonRecyclerView.setHasFixedSize(true)
        pokemonRecyclerView.layoutManager = GridLayoutManager(context, 2)

        //アイテム間のスペースを設定するために、ItemOffsetDecorationを追加します。
        val itemDecoration = ItemOffsetDecoration(requireContext(), R.dimen.spacing)
        pokemonRecyclerView.addItemDecoration(itemDecoration)

        //ポケモンデータを取得するためのメソッドを呼び出します。
        fetchData()

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
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.Default) {
                Common.pokemonList.filter { it.name?.lowercase()?.contains(text.lowercase()) == true }
            }
            adapter.updateList(result)
        }
    }

    //検索をリセットして、全てのポケモンを表示します。
    private fun resetSearch() {
        adapter.updateList(Common.pokemonList)
        searchBar.clearFocus()
    }

    //非同期でポケモンデータを取得し、RecyclerViewに設定します。また、lastSuggestリストを更新します。
    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val pokemonDex = withContext(Dispatchers.IO) {
                    iPokemonList.listPokemon()
                }
                Common.pokemonList = pokemonDex.pokemon ?: emptyList()

                // フラグメントがアクティブであることを確認
                if (isAdded && view != null) {
                    adapter = PokemonListAdapter(requireActivity(), Common.pokemonList)
                    pokemonRecyclerView.adapter = adapter

                    lastSuggest.clear()
                    lastSuggest.addAll(Common.pokemonList.mapNotNull { it.name })
                    searchBar.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // フラグメントがアクティブであることを確認
                if (isAdded && view != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


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
