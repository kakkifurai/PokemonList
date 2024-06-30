package com.example.showpokemonlist.Common


import android.content.Context
import android.graphics.Color
import com.example.myapplication.R
import com.example.showpokemonlist.Model.Pokemon
import java.util.Locale

//オブジェクト宣言を使って単一のインスタンスを持つオブジェクトとして定義されています。これにより、Common 内のメンバーは静的にアクセスできます。
object Common {

    //num で指定された番号でポケモンを検索し、見つかったポケモンを返します。見つからない場合は null を返します。
    fun findPokemonByNum(num:String?):Pokemon?{
        for (pokemon:Pokemon in pokemonList)
            if (pokemon.num.equals(num))
                return pokemon
        return null
    }

    //ポケモンのタイプに応じて色を取得します。
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

    // タイプ名を日本語に変換する関数
    fun getLocalizedTypeName(context: Context, type: String): String {
        return context.resources.getString(
            when (type.toLowerCase(Locale.getDefault())) {
                "normal" -> R.string.Normal
                "fire" -> R.string.Fire
                "water" -> R.string.Water
                "electric" -> R.string.Electric
                "grass" -> R.string.Grass
                "ice" -> R.string.Ice
                "fighting" -> R.string.Fighting
                "poison" -> R.string.Poison
                "ground" -> R.string.Ground
                "flying" -> R.string.Flying
                "psychic" -> R.string.Psychic
                "bug" -> R.string.Bug
                "rock" -> R.string.Rock
                "ghost" -> R.string.Ghost
                "dragon" -> R.string.Dragon
                else -> return type // もし他のタイプがあればそのまま返す
            }
        )
    }

    //指定された type を持つポケモンのリストを返します。
    fun findPokemonByType(type: String): List<Pokemon> {
        return pokemonList.filter { pokemon ->
            pokemon.type?.contains(type) == true
        }
    }


    //英語のポケモン名を日本語に変換する関数です。
    fun getPokemonNameInJapanese(context: Context, englishName: String): String {
        val resources = context.resources
        val resourceId = resources.getIdentifier(englishName, "string", context.packageName)
        return if (resourceId != 0) {
            resources.getString(resourceId)
        } else {
            englishName // 日本語名が見つからない場合は英語名を返す
        }
    }

    //ポケモンのタイプを日本語に変換する関数です。
    fun getPokemonTypeInJapanese(context: Context, type: String): String {
        val resourceId = context.resources.getIdentifier(type, "string", context.packageName)
        return if (resourceId != 0) {
            context.getString(resourceId)
        } else {
            "未知のタイプ"
        }
    }

    var pokemonList:List<Pokemon> = ArrayList()  //ポケモンのリストを保持する変数です。初期値は空のリストです。

    //ポケモン関連のデータを識別するために使用されます。
    const val KEY_ENABLE_HOME = "position"
    const val KEY_NUM_EVOLUTION = "evolution"
    const val KEY_POKEMON_TYPE = "type"
}