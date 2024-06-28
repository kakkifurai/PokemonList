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

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Common.KEY_POKEMON_TYPE -> handlePokemonType(intent)
                Common.KEY_ENABLE_HOME, Common.KEY_NUM_EVOLUTION -> handlePokemonDetail(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolbar()
        setupWindowInsets()
        setupBroadcastReceiver()
        setupBackPressedCallback()

        if (savedInstanceState == null) {
            showInitialFragment()
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBroadcastReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Common.KEY_POKEMON_TYPE)
            addAction(Common.KEY_ENABLE_HOME)
            addAction(Common.KEY_NUM_EVOLUTION)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 1) {
                    supportFragmentManager.popBackStack()
                } else {
                    showPokemonListFragment()
                }
            }
        })
    }

    private fun handlePokemonType(intent: Intent) {
        val type = intent.getStringExtra("type")
        if (!type.isNullOrEmpty()) {
            showFragment(PokemonType.newInstance(type), "TYPE: ${type.uppercase()}")
        } else {
            showError("Invalid pokemon type")
        }
    }

    private fun handlePokemonDetail(intent: Intent) {
        val num = intent.getStringExtra("num")
        if (!num.isNullOrEmpty()) {
            val pokemon = Common.findPokemonByNum(num)
            showFragment(PokemonDetail.newInstance(num), pokemon?.name ?: "Pokemon Detail")
        } else {
            showError("Invalid pokemon number")
        }
    }

    private fun showFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.list_pokemon_fragment, fragment)
            addToBackStack(null)
            commit()
        }
        updateToolbar(title, true)
    }

    private fun showInitialFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_pokemon_fragment, PokemonList.newInstance())
            .commit()
        updateToolbar("POKEMON LIST", false)
    }

    private fun showPokemonListFragment() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        showInitialFragment()
    }

    fun updateToolbar(title: String, showBackButton: Boolean) {
        toolbar.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(showBackButton)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }
}
