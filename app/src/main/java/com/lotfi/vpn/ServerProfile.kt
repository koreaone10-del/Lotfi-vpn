package com.lotfi.vpn

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ServerProfile(val id:String,val name:String,val protocol:String,val endpoint:String)

class ProfileStore(context:Context) {
    private val prefs=context.getSharedPreferences("lotfi_profiles",Context.MODE_PRIVATE)
    fun load():List<ServerProfile> = try {
        val a=JSONArray(prefs.getString("items","[]"))
        buildList { for(i in 0 until a.length()) { val o=a.getJSONObject(i);add(ServerProfile(o.optString("id"),o.optString("name"),o.optString("protocol"),o.optString("endpoint"))) } }
    } catch (_:Exception) { emptyList() }
    fun save(items:List<ServerProfile>) {
        val a=JSONArray()
        items.forEach { p -> a.put(JSONObject().apply { put("id",p.id);put("name",p.name);put("protocol",p.protocol);put("endpoint",p.endpoint) }) }
        prefs.edit().putString("items",a.toString()).apply()
    }
}
