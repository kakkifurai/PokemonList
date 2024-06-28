package com.example.showpokemonlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
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

    // BroadcastReceiver for showing Pokemon type
    private val showPokemonType = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Common.KEY_POKEMON_TYPE) {
                val type = intent.getStringExtra("type")
                if (!type.isNullOrEmpty()) {
                    // 現在のフラグメントを確認
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.list_pokemon_fragment)
                    if (currentFragment !is PokemonType || currentFragment.arguments?.getString("type") != type) {
                        // トランザクションを一度に行う
                        supportFragmentManager.beginTransaction().apply {
                            currentFragment?.let { remove(it) }
                            val typeFragment = PokemonType.newInstance(type)
                            replace(R.id.list_pokemon_fragment, typeFragment)
                            addToBackStack(null)
                            commitAllowingStateLoss()
                        }
                        toolbar?.title = "POKEMON TYPE: ${type.toUpperCase()}"
                    }
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
                if (num != null) {
                    // Remove existing Fragment if any
                    supportFragmentManager.findFragmentById(R.id.list_pokemon_fragment)?.let {
                        supportFragmentManager.beginTransaction().remove(it).commit()
                    }

                    // Add new Fragment
                    val detailFragment = PokemonDetail.newInstance(num)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.list_pokemon_fragment, detailFragment)
                        addToBackStack("top")
                        commit()
                    }

                    supportFragmentManager.addOnBackStackChangedListener {
                        val pokemon = Common.findPokemonByNum(num)
                        pokemon?.let {
                            toolbar.title = it.name
                        } ?: run {
                            Toast.makeText(this@MainActivity, "Pokemon not found", Toast.LENGTH_SHORT).show()
                        }
                    }
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
                if (num != null) {
                    // Remove existing Fragment if any
                    supportFragmentManager.findFragmentById(R.id.list_pokemon_fragment)?.let {
                        supportFragmentManager.beginTransaction().remove(it).commit()
                    }

                    // Add new Fragment
                    val detailFragment = PokemonDetail.newInstance(num)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.list_pokemon_fragment, detailFragment)
                        addToBackStack("top")
                        commit()
                    }

                    supportFragmentManager.addOnBackStackChangedListener {
                        val pokemon = Common.findPokemonByNum(num)
                        pokemon?.let {
                            toolbar.title = it.name
                        } ?: run {
                            Toast.makeText(this@MainActivity, "Pokemon not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Invalid pokemon number", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        setupWindowInsets()
        setupBroadcastReceivers()
        setupBackStackListener()
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
            registerReceiver(showPokemonDetailReceiver, IntentFilter(Common.KEY_ENABLE_HOME))
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

    private val showPokemonDetailReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Common.KEY_ENABLE_HOME || intent?.action == Common.KEY_NUM_EVOLUTION) {
                showPokemonDetail(intent)
            }
        }
    }

    private fun showPokemonDetail(intent: Intent?) {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val num = intent?.getStringExtra("num")
        if (num != null) {
            try {
                val detailFragment = PokemonDetail.newInstance(num)
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.list_pokemon_fragment, detailFragment)
                    addToBackStack("top")
                    commit()
                }

                supportFragmentManager.addOnBackStackChangedListener {
                    val pokemon = Common.findPokemonByNum(num)
                    pokemon?.let {
                        toolbar.title = it.name
                    } ?: run {
                        Toast.makeText(this, "Pokemon not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Invalid pokemon number", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).apply {
            unregisterReceiver(showPokemonDetailReceiver)
            unregisterReceiver(showEvolution)
            unregisterReceiver(showPokemonType)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // バックスタックをクリア
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                // PokemonListフラグメントを表示
                showPokemonListFragment()

                // ツールバーの設定を更新
                toolbar.title = "POKEMON_LIST"
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setDisplayShowHomeEnabled(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun showPokemonListFragment() {
        val fragment = PokemonList.newInstance()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.list_pokemon_fragment, fragment)
            commit()
        }
        toolbar.title = "POKEMON LIST"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
