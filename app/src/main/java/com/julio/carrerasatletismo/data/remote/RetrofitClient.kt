package com.julio.carrerasatletismo.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * PATRÓN SINGLETON con "object": Kotlin crea UNA sola instancia de RetrofitClient.
 * "by lazy" construye Retrofit la primera vez que se usa y después reutiliza la misma.
 * Además, Hilt lo entrega como @Singleton en di/ModuloDatos.kt.
 */
object RetrofitClient {

    private const val BASE_URL = "https://rundida.com/"

    val api: RunDidaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RunDidaApi::class.java)
    }
}
