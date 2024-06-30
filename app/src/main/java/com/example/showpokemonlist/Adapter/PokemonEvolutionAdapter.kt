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

//このクラスは、ポケモンの進化のリストを表示するために使用されます。

//RecyclerView.Adapter を継承 。MyViewHolder を指定。
class PokemonEvolutionAdapter(
    private val context: Context,//context はアプリケーションの状態やリソースにアクセスするために使用
    private val evolutionList: List<Evolution>//進化情報のリスト
) : RecyclerView.Adapter<PokemonEvolutionAdapter.MyViewHolder>() {

    //RecyclerView の各アイテムビューの保持と管理
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var chip: Chip = itemView.findViewById(R.id.chip)//chip_itemxmlからテンプレートを取得

        //クリックした時の処理を記入
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

            // ポケモンのタイプに基づいて色を設定
            val pokemon = Common.findPokemonByNum(evolution.num)
            val pokemonType = pokemon?.type?.getOrNull(0) ?: "default"

            // getColorByTypeで色を設定
            val color = Common.getColorByType(pokemonType)
            chip.chipBackgroundColor = ColorStateList.valueOf(color)
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
