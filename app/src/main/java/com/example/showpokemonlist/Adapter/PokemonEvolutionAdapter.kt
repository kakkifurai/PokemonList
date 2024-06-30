package com.example.showpokemonlist.Adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.showpokemonlist.Common.Common
import com.example.showpokemonlist.Model.Evolution
import com.google.android.material.chip.Chip

class PokemonEvolutionAdapter(
    private val context: Context,
    private val evolutionList: List<Evolution>
) : RecyclerView.Adapter<PokemonEvolutionAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var chip: Chip = itemView.findViewById(R.id.chip)

        init {
            chip.setOnClickListener {
                // LocalBroadcastManagerのインスタンスを取得
                val localBroadcastManager = LocalBroadcastManager.getInstance(context)

                // Intentを作成して、ブロードキャスト用のActionを設定
                val intent = Intent(Common.KEY_NUM_EVOLUTION).apply {
                    // "num" をキーにして、選択された進化の "num" を追加
                    putExtra("num", evolutionList[adapterPosition].num)
                }

                // ブロードキャストを送信
                localBroadcastManager.sendBroadcast(intent)
            }
        }


        fun bind(evolution: Evolution) {
            // ポケモンの名前を日本語に変換して設定
            val japaneseName = evolution.name?.let { Common.getPokemonNameInJapanese(context, it) } ?: "不明なポケモン"
            chip.text = japaneseName

            val pokemon = Common.findPokemonByNum(evolution.num)
            val pokemonType = pokemon?.type?.getOrNull(0)

            try {
                if (pokemonType != null) {
                    // 色を直接取得して設定
                    val color = Common.getColorByType(pokemonType)
                    chip.chipBackgroundColor = ColorStateList.valueOf(color)
                } else {
                    // デフォルトの色を設定
                    val defaultColor = context.resources.getColor(R.color.default_chip_color, null)
                    chip.chipBackgroundColor = ColorStateList.valueOf(defaultColor)
                }
            } catch (e: IllegalArgumentException) {
                // デフォルトの色を設定
                val defaultColor = context.resources.getColor(R.color.default_chip_color, null)
                chip.chipBackgroundColor = ColorStateList.valueOf(defaultColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(context).inflate(R.layout.chip_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val evolution = evolutionList[position]
        holder.bind(evolution)
    }

    override fun getItemCount(): Int {
        return evolutionList.size
    }
}
