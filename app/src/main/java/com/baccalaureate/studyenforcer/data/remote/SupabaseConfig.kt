package com.baccalaureate.studyenforcer.data.remote

import android.content.Context
import android.content.SharedPreferences

class SupabaseConfig(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("supabase_config_prefs", Context.MODE_PRIVATE)

    var url: String
        get() = prefs.getString(KEY_URL, DEFAULT_URL) ?: DEFAULT_URL
        set(value) = prefs.edit().putString(KEY_URL, value.trim()).apply()

    var anonKey: String
        get() = prefs.getString(KEY_KEY, DEFAULT_KEY) ?: DEFAULT_KEY
        set(value) = prefs.edit().putString(KEY_KEY, value.trim()).apply()

    val isConfigured: Boolean
        get() = url.isNotBlank() && url != DEFAULT_URL && anonKey.isNotBlank() && anonKey != DEFAULT_KEY

    companion object {
        private const val KEY_URL = "supabase_url"
        private const val KEY_KEY = "supabase_anon_key"
        const val DEFAULT_URL = "https://your-project-id.supabase.co"
        const val DEFAULT_KEY = "your-anon-key"
    }
}
