package com.example.proyectointegrador.data.remote

import android.content.Context

object TokenManager {
    private const val PREFS = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    var token: String? = null
        private set

    fun load(context: Context) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        token = sp.getString(KEY_TOKEN, null)
    }

    fun save(context: Context, value: String) {
        token = value
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_TOKEN, value).commit()
    }

    fun clear(context: Context) {
        token = null
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().remove(KEY_TOKEN).commit()
    }
}


