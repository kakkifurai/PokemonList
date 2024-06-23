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

    private val showDetail = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Common.KEY_ENABLE_HOME) {
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setDisplayShowHomeEnabled(true)
                }

                val num = intent.getStringExtra("num")
                if (num != null) {
                    val detailFragment = PokemonDetail.newInstance(num)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.list_pokemon_fragment, detailFragment)
                        addToBackStack("detail")
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

    private val showEvolution = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Common.KEY_NUM_EVOLUTION) {
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setDisplayShowHomeEnabled(true)
                }

                val num = intent.getStringExtra("num")
                if (num != null) {
                    val detailFragment = PokemonDetail.newInstance(num)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.list_pokemon_fragment, detailFragment)
                        addToBackStack("detail")
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
            registerReceiver(showPokemonDetailReceiver, IntentFilter(Common.KEY_NUM_EVOLUTION))
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
            val detailFragment = PokemonDetail.newInstance(num)
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.list_pokemon_fragment, detailFragment)
                addToBackStack("detail")
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
        } else {
            Toast.makeText(this, "Invalid pokemon number", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showPokemonDetailReceiver)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                toolbar.title = "POKEMON_LIST"
                supportFragmentManager.popBackStack("detail", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setDisplayShowHomeEnabled(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}