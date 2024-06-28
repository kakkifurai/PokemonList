package com.example.showpokemonlist.Adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.showpokemonlist.Common.Common
import com.google.android.material.chip.Chip

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
        val type = typeList[position]
        holder.chip.text = type

        try {
            // 色を直接取得して設定
            val color = Common.getColorByType(type)
            holder.chip.chipBackgroundColor = ColorStateList.valueOf(color)
        } catch (e: IllegalArgumentException) {
            // デフォルトの色を設定
            val defaultColor = context.resources.getColor(R.color.default_chip_color, null)
            holder.chip.chipBackgroundColor = ColorStateList.valueOf(defaultColor)
        }
    }

    override fun getItemCount(): Int {
        return typeList.size
    }
}
