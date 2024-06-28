package com.example.showpokemonlist.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myapplication.R
import com.example.showpokemonlist.Common.Common
import com.example.showpokemonlist.Model.Pokemon

class PokemonListAdapter(
    private val context: Context,
    private var pokemonList: List<Pokemon>
) : RecyclerView.Adapter<PokemonListAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPokemon: ImageView = itemView.findViewById(R.id.pokemon_image)
        private val txtPokemon: TextView = itemView.findViewById(R.id.pokemon_name)

        fun bind(pokemon: Pokemon) {
            txtPokemon.text = pokemon.name

            Glide.with(context)
                .load(pokemon.img)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.error_image1)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("GlideError", "Image load failed", e)
                        Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("GlideSuccess", "Image loaded successfully")
                        return false
                    }
                })
                .into(imgPokemon)

            itemView.setOnClickListener {
                val num = pokemon.num ?: return@setOnClickListener
                val intent = Intent(Common.KEY_ENABLE_HOME).apply {
                    putExtra("num", num)
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.pokemon_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        holder.bind(pokemon)
    }

    override fun getItemCount(): Int = pokemonList.size

    fun updateList(newList: List<Pokemon>) {
        pokemonList = newList
        notifyDataSetChanged()
    }
}