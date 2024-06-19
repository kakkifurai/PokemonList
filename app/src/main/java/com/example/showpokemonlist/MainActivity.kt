package com.example.showpokemonlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myapplication.R
import com.example.showpokemonlist.Common.Common

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar

    private val showDetail = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Common.KEY_ENABLE_HOME) {
                // アクションバーの設定
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setDisplayShowHomeEnabled(true)
                }

                // フラグメントのインスタンスを作成
                val detailFragment = PokemonDetail.newInstance()
                val position = intent.getIntExtra("position", -1)

                // バンドルを設定
                val bundle = Bundle().apply {
                    putInt("position", position)
                }
                detailFragment.arguments = bundle

                // フラグメントトランザクションの設定
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.list_pokemon_fragment, detailFragment)
                    addToBackStack("detail")
                    commit()
                }

                // ツールバーのタイトルを設定
                val pokemon = Common.pokemonList.getOrNull(position)
                pokemon?.let {
                    toolbar.title = it.name
                }
            }
        }
    }

    private val showEvolution = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Common.KEY_NUM_EVOLUTION) {
                // アクションバーの設定
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setDisplayShowHomeEnabled(true)
                }

                // フラグメントのインスタンスを作成
                val detailFragment = PokemonDetail.newInstance()
                val num = intent.getStringExtra("num")

                // バンドルを設定
                val bundle = Bundle().apply {
                    putString("num", num)
                }
                detailFragment.arguments = bundle

                // フラグメントトランザクションの設定
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.list_pokemon_fragment, detailFragment)
                    addToBackStack("detail")
                    commit()
                }

                // ツールバーのタイトルを設定
                val pokemon = Common.findPokemonByNum(num)
                pokemon?.let {
                    toolbar.title = it.name
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "POKEMON_LIST"
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // LocalBroadcastManagerを使用したレシーバの登録
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(showDetail, IntentFilter(Common.KEY_ENABLE_HOME))

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(showEvolution, IntentFilter(Common.KEY_NUM_EVOLUTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        // レシーバの登録解除
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showDetail)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showEvolution)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                toolbar.title = "POKEMON_LIST"
                supportFragmentManager.popBackStack("detail", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setDisplayShowHomeEnabled(false)
            }
        }
        return true
    }
}
