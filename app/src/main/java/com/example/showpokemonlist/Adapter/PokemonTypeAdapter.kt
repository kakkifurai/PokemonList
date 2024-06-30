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
import com.google.android.material.chip.Chip
import java.util.Locale

//RecyclerViewを使ってポケモンのタイプを表示するために使用される
class PokemonTypeAdapter(
    private val context: Context,
    private val typeList: List<String>) : //ポケモンのタイプリストを保持
    RecyclerView.Adapter<PokemonTypeAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var chip: Chip = itemView.findViewById(R.id.chip) //chip_item.xmlからテンプレートを取得
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)
        //タイプがタップされたときの処理を記入
        init {
            chip.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < typeList.size) {  //クリックされたアイテムが実際にリスト内に存在すること.そのアイテムの位置が、リストの範囲内にあることを確認。例えば、ユーザーが素早くスクロールしたり、リストが更新されている最中にクリックしたりした場合でも、アプリが正しく動作することを保証
                    val intent = Intent(Common.KEY_POKEMON_TYPE).apply {
                        putExtra("type", typeList[adapterPosition])  //KEY_POKEMON_TYPEというアクションを持つインテントを作成し、クリックされたタイプを"type"としてインテントに追加してブロードキャストを送信します。
                    }
                    localBroadcastManager.sendBroadcast(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(context).inflate(R.layout.chip_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val type = typeList[position] ?: "default"

        // タイプ名を日本語に変換してセット
        holder.chip.text = Common.getLocalizedTypeName(context,type)

        // 色を直接取得して設定
        val color = Common.getColorByType(type)
        holder.chip.chipBackgroundColor = ColorStateList.valueOf(color)
    }

    override fun getItemCount(): Int {
        return typeList.size
    }

}
