package com.lotfi.vpn

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    private const val API="https://api.github.com/repos/koreaone10-del/Lotfi-vpn/releases/latest"
    suspend fun check():String = withContext(Dispatchers.IO) {
        var c:HttpURLConnection?=null
        try {
            c=URL(API).openConnection() as HttpURLConnection
            c.requestMethod="GET";c.connectTimeout=8000;c.readTimeout=8000
            c.setRequestProperty("Accept","application/vnd.github+json")
            c.setRequestProperty("User-Agent","Lotfi-VPN-Android")
            if(c.responseCode !in 200..299) return@withContext "تعذر جلب الإصدار. HTTP ${c.responseCode}"
            val j=JSONObject(c.inputStream.bufferedReader().use{it.readText()})
            "آخر إصدار منشور: ${j.optString("tag_name","غير معروف")}\n${j.optString("html_url","https://github.com/koreaone10-del/Lotfi-vpn/releases")}\n\nالتثبيت يتطلب موافقة المستخدم."
        } catch(e:Exception) { "تعذر فحص التحديثات: ${e.message ?: e.javaClass.simpleName}" }
        finally { c?.disconnect() }
    }
}
