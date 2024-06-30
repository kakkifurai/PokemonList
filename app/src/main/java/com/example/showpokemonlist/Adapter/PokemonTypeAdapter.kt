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

class PokemonTypeAdapter(private val context: Context, private val typeList: List<String>) :
    RecyclerView.Adapter<PokemonTypeAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var chip: Chip = itemView.findViewById(R.id.chip)

        init {
            chip.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < typeList.size) {
                    LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(Intent(Common.KEY_POKEMON_TYPE).putExtra("type", typeList[adapterPosition]))
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
        holder.chip.text = getLocalizedTypeName(type)

        // 色を直接取得して設定
        val color = Common.getColorByType(type)
        holder.chip.chipBackgroundColor = ColorStateList.valueOf(color)
    }



    override fun getItemCount(): Int {
        return typeList.size
    }

    // タイプ名を日本語に変換する関数

    fun getLocalizedTypeName(type: String): String {
        return when (type.toLowerCase(Locale.getDefault())) {
            "normal" -> context.getString(R.string.Normal)
            "fire" -> context.getString(R.string.Fire)
            "water" -> context.getString(R.string.Water)
            "electric" -> context.getString(R.string.Electric)
            "grass" -> context.getString(R.string.Grass)
            "ice" -> context.getString(R.string.Ice)
            "fighting" -> context.getString(R.string.Fighting)
            "poison" -> context.getString(R.string.Poison)
            "ground" -> context.getString(R.string.Ground)
            "flying" -> context.getString(R.string.Flying)
            "psychic" -> context.getString(R.string.Psychic)
            "bug" -> context.getString(R.string.Bug)
            "rock" -> context.getString(R.string.Rock)
            "ghost" -> context.getString(R.string.Ghost)
            "dragon" -> context.getString(R.string.Dragon)
            else -> type // もし他のタイプがあればそのまま返す
        }
    }
}
