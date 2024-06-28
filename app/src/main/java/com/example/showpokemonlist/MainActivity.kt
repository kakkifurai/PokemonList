package com.example.showpokemonlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myapplication.R
import com.example.showpokemonlist.Common.Common

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar

    // BroadcastReceiver for showing Pokemon type
    private val showPokemonType = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Common.KEY_POKEMON_TYPE) {
                val type = intent.getStringExtra("type")
                if (!type.isNullOrEmpty()) {
                    showFragment(PokemonType.newInstance(type), "POKEMON TYPE: ${type.uppercase()}")
                } else {
                    Toast.makeText(this@MainActivity, "Invalid pokemon type", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // BroadcastReceiver for showing Pokemon detail
    private val showDetail = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Common.KEY_ENABLE_HOME) {
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setDisplayShowHomeEnabled(true)
                }
                val num = intent.getStringExtra("num")
                if (!num.isNullOrEmpty()) {
                    showFragment(PokemonDetail.newInstance(num), Common.findPokemonByNum(num)?.name ?: "Pokemon Detail")
                } else {
                    Toast.makeText(this@MainActivity, "Invalid pokemon number", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // BroadcastReceiver for showing Pokemon evolution
    private val showEvolution = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Common.KEY_NUM_EVOLUTION) {
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setDisplayShowHomeEnabled(true)
                }
                val num = intent.getStringExtra("num")
                if (!num.isNullOrEmpty()) {
                    showFragment(PokemonDetail.newInstance(num), Common.findPokemonByNum(num)?.name ?: "Pokemon Detail")
                } else {
                    Toast.makeText(this@MainActivity, "Invalid pokemon number", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // ツールバーのセットアップ
        setupToolbar()
        // ウィンドウインセットのセットアップ
        setupWindowInsets()
        // ブロードキャストレシーバーのセットアップ
        setupBroadcastReceivers()
        // バックスタックリスナーのセットアップ
        setupBackStackListener()
        setupBackPressedCallback()

        // 初期フラグメントを表示
        if (savedInstanceState == null) {
            showInitialFragment()
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "POKEMON_LIST"
        setSupportActionBar(toolbar)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).apply {
            registerReceiver(showDetail, IntentFilter(Common.KEY_ENABLE_HOME))
            registerReceiver(showEvolution, IntentFilter(Common.KEY_NUM_EVOLUTION))
            registerReceiver(showPokemonType, IntentFilter(Common.KEY_POKEMON_TYPE))
        }
    }

    private fun setupBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.list_pokemon_fragment)
            if (fragment is PokemonDetail) {
                displayHomeAsUpEnabled(true)
            } else {
                displayHomeAsUpEnabled(false)
                toolbar.title = "POKEMON_LIST"
            }
        }
    }

    private fun displayHomeAsUpEnabled(enabled: Boolean) {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(enabled)
            setDisplayShowHomeEnabled(enabled)
        }
    }

    private fun showFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.list_pokemon_fragment, fragment)
            addToBackStack(null)
            commit()
        }
        toolbar.title = title
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).apply {
            unregisterReceiver(showDetail)
            unregisterReceiver(showEvolution)
            unregisterReceiver(showPokemonType)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                showPokemonListFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun showPokemonListFragment() {
        val fragment = PokemonList.newInstance()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.list_pokemon_fragment, fragment)
            commit()
        }
        toolbar.title = "POKEMON LIST"
        displayHomeAsUpEnabled(false)
    }

    private fun showInitialFragment() {
        val initialFragment = PokemonList.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_pokemon_fragment, initialFragment)
            .commit()
    }
}
