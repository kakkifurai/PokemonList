package com.example.showpokemonlist.Common


import android.graphics.Color
import com.example.showpokemonlist.Model.Pokemon



object Common {


    fun findPokemonByNum(num:String?):Pokemon?{
        for (pokemon:Pokemon in pokemonList)
            if (pokemon.num.equals(num))
                return pokemon
        return null
    }

    fun getColorByType(type: String): Int {
        return when (type) {
            "Normal" -> Color.parseColor("#A4A27A")
            "Dragon" -> Color.parseColor("#743BFB")
            "Psychic" -> Color.parseColor("#F15B85")
            "Electric" -> Color.parseColor("#E9CA3C")
            "Ground" -> Color.parseColor("#D9BF6C")
            "Grass" -> Color.parseColor("#81C85B")
            "Poison" -> Color.parseColor("#A441A3")
            "Steel" -> Color.parseColor("#BAB7D2")
            "Fairy" -> Color.parseColor("#DDA2DF")
            "Fire" -> Color.parseColor("#F48130")
            "Fight" -> Color.parseColor("#BE3027")
            "Bug" -> Color.parseColor("#A8B822")
            "Ghost" -> Color.parseColor("#705693")
            "Dark" -> Color.parseColor("#745945")
            "Ice" -> Color.parseColor("#9BD8D8")
            "Water" -> Color.parseColor("#658FF1")
            else -> Color.parseColor("#658FA0") // デフォルトの色
        }
    }

    fun findPokemonByType(type: String): List<Pokemon> {
        return pokemonList.filter { pokemon ->
            pokemon.type?.contains(type) == true
        }
    }



    var pokemonList:List<Pokemon> = ArrayList()
    const val KEY_ENABLE_HOME = "position"
    const val KEY_NUM_EVOLUTION = "evolution"
    const val KEY_POKEMON_TYPE = "type"
}