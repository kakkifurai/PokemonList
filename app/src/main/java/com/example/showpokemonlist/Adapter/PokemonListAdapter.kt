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
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myapplication.R
import com.example.showpokemonlist.Common.Common
import com.example.showpokemonlist.Model.Pokemon

//RecyclerViewを使ってポケモンリストを表示するために使用されます。
class PokemonListAdapter(
    private val context: Context,
    private var pokemonList: List<Pokemon>//ポケモンリストを保持
) : RecyclerView.Adapter<PokemonListAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPokemon: ImageView = itemView.findViewById(R.id.pokemon_image)//fragment_pokemon_list.xmlからpokemon_imageを取得
        private val txtPokemon: TextView = itemView.findViewById(R.id.pokemon_name)//fragment_pokemon_list.xmlからpokemon_nameを取得

        fun bind(pokemon: Pokemon) {
            //ポケモンの名前を日本語に変換します。pokemon.nameがnullの場合は空文字を使用します。
            val pokemonName = Common.getPokemonNameInJapanese(context, pokemon.name ?: "")
            txtPokemon.text = pokemonName

            Glide.with(context)//画像の読み込みを行う
                .load(pokemon.img)//pokemon.imgのURLから画像を読み込む
                //.transition(DrawableTransitionOptions.withCrossFade())//画像を読み込む際にフェードをかける
                .placeholder(R.drawable.ic_launcher_foreground)//画像が読み込まれる前に表示するプレースホルダー画像を設定
                .error(R.drawable.error_image1)//エラー時の画像
                //画像読み込みの結果を処理するリスナーを設定
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

            //ポケモンがタッチされたときの処理
            itemView.setOnClickListener {
                val num = pokemon.num ?: return@setOnClickListener//pokemon.numがnullの場合は終了
                val intent = Intent(Common.KEY_ENABLE_HOME).apply {  //KEY_ENABLE_HOMEというアクションを持つインテントを作成
                    putExtra("num", num)  //インテントにポケモンの番号を追加
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent) //送信
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
