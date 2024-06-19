package com.example.showpokemonlist.Retrofit

import android.database.Observable
import com.example.showpokemonlist.Model.Pokedex
import retrofit2.http.GET

interface IPokemonList {
    @GET("pokedex.json")
    suspend fun listPokemon(): Pokedex
}