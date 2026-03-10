package dev.tidesapp.wearos.core.network

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TidesQueryParamsInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val countryCode = extractCountryCode(original.header("Authorization"))
        val url = original.url.newBuilder()
            .addQueryParameter("deviceType", "PHONE")
            .addQueryParameter("locale", "en_US")
            .addQueryParameter("platform", "ANDROID")
            .addQueryParameter("countryCode", countryCode)
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }

    private fun extractCountryCode(authHeader: String?): String {
        if (authHeader == null) return "US"
        return try {
            val token = authHeader.removePrefix("Bearer ")
            val parts = token.split(".")
            if (parts.size < 2) return "US"
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING))
            val json = Json.parseToJsonElement(payload).jsonObject
            json["cc"]?.jsonPrimitive?.content ?: "US"
        } catch (_: Exception) {
            "US"
        }
    }
}
