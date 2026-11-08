package com.lotfi.vpn

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ServerProfile(val id: Long, val name: String, val provider: String,
                         val host: String, val port: Int, val protocol: String)

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("lotfi_workspace", Context.MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private lateinit var profileList: LinearLayout
    private lateinit var logs: LinearLayout
    private lateinit var networkText: TextView
    private lateinit var countText: TextView
    private val bg = Color.rgb(9, 13, 24)
    private val cardColor = Color.rgb(19, 27, 45)
    private val raised = Color.rgb(29, 39, 62)
    private val purple = Color.rgb(139, 124, 255)
    private val cyan = Color.rgb(58, 210, 220)
    private val white = Color.rgb(241, 244, 255)
    private val muted = Color.rgb(151, 164, 190)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); isFillViewport = true }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(28))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        scroll.addView(root)
        setContentView(scroll)

        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val titles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        titles.addView(text("LOTFI VPN", 12, cyan, true))
        titles.addView(text("مركز التحكم", 28, white, true))
        titles.addView(text("إدارة ملفات الاتصال وحالة الشبكة", 13, muted))
        head.addView(titles, LinearLayout.LayoutParams(0, -2, 1f))
        head.addView(text("PRO", 12, white, true).apply {
            gravity = Gravity.CENTER; setPadding(dp(14), dp(8), dp(14), dp(8)); background = shape(purple, 30)
        })
        root.addView(head)
        gap(20)

        val hero = card()
        hero.addView(text("حالة خدمة VPN", 14, muted, true))
        hero.addView(text("المحرك غير مهيأ", 23, white, true))
        hero.addView(text("هذه النسخة تدير ملفات الخوادم محلياً، ولا تنشئ نفق VPN فعلياً.", 13, muted))
        val info = button("معلومات المحرك", purple)
        info.setOnClickListener {
            AlertDialog.Builder(this).setTitle("محرك VPN")
                .setMessage("لم تتم إضافة محرك اتصال بعد. لن يعرض التطبيق اتصالاً وهمياً ولن يغيّر مسار حركة الإنترنت. يلزم محرك بروتوكول موثوق وخادم تملكه أو لديك إذن باستخدامه.")
                .setPositiveButton("حسناً", null).show()
        }
        hero.addView(info)
        root.addView(hero)
        gap(12)

        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val netCard = card()
        netCard.addView(text("الشبكة الحالية", 12, muted))
        networkText = text("جارٍ الفحص", 18, cyan, true)
        netCard.addView(networkText)
        val profCard = card()
        profCard.addView(text("ملفات محفوظة", 12, muted))
        countText = text("0", 18, purple, true)
        profCard.addView(countText)
        stats.addView(netCard, LinearLayout.LayoutParams(0, -2, 1f))
        stats.addView(View(this), LinearLayout.LayoutParams(dp(10), 1))
        stats.addView(profCard, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(stats)
        gap(20)

        root.addView(text("ملفات الاتصال", 19, white, true))
        root.addView(text("أضف بيانات خادم مصرح لك باستخدامه", 12, muted))
        val add = button("+ إضافة ملف اتصال", purple)
        add.setOnClickListener { profileDialog(null) }
        root.addView(add)
        val refresh = button("تحديث حالة الشبكة", raised)
        refresh.setOnClickListener { refreshAll(); log("تم تحديث حالة الشبكة") }
        root.addView(refresh)
        gap(8)

        profileList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(profileList)
        gap(18)

        root.addView(text("سجل النشاط", 19, white, true))
        root.addView(text("أحداث محلية داخل التطبيق", 12, muted))
        logs = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(logs)
        val clear = button("مسح السجل", raised)
        clear.setOnClickListener { prefs.edit().remove("logs").apply(); renderLogs(); log("تم مسح السجل") }
        root.addView(clear)
        gap(18)
        root.addView(text("Lotfi VPN Workspace • الإصدار 1.1.0", 11, muted).apply { gravity = Gravity.CENTER })
        log("تم تشغيل مساحة العمل")
        refreshAll()
    }

    private fun refreshAll() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val n = cm.activeNetwork
        val c = if (n != null) cm.getNetworkCapabilities(n) else null
        networkText.text = when {
            c == null -> "غير متصل"
            c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi‑Fi"
            c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "بيانات الهاتف"
            c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "متصل"
        }
        val list = loadProfiles()
        countText.text = list.size.toString()
        renderProfiles(list)
        renderLogs()
    }

    private fun renderProfiles(items: List<ServerProfile>) {
        profileList.removeAllViews()
        if (items.isEmpty()) {
            val empty = card()
            empty.addView(text("لا توجد ملفات محفوظة حالياً", 15, white, true))
            empty.addView(text("اضغط «إضافة ملف اتصال» لتسجيل بيانات خادمك.", 12, muted))
            profileList.addView(empty)
            return
        }
        items.forEach { p ->
            val box = card()
            box.addView(text(p.name, 16, white, true))
            box.addView(text("${p.provider} • ${p.protocol}", 12, cyan))
            box.addView(text("${p.host}:${p.port}", 13, muted))
            val edit = button("تعديل الملف", raised)
            edit.setOnClickListener { profileDialog(p) }
            box.addView(edit)
            val del = button("حذف الملف", Color.rgb(76, 36, 53))
            del.setOnClickListener {
                AlertDialog.Builder(this).setTitle("حذف الملف")
                    .setMessage("حذف «${p.name}» من هذا الجهاز؟")
                    .setNegativeButton("إلغاء", null)
                    .setPositiveButton("حذف") { _, _ ->
                        saveProfiles(loadProfiles().filterNot { it.id == p.id })
                        log("تم حذف الملف: ${p.name}")
                        refreshAll()
                    }.show()
            }
            box.addView(del)
            profileList.addView(box)
            gapIn(profileList, 8)
        }
    }

    private fun profileDialog(old: ServerProfile?) {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(4), dp(18), dp(4))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        fun field(hint: String, value: String = "") = EditText(this).apply {
            this.hint = hint; setSingleLine(true); setText(value)
            setTextColor(white); setHintTextColor(muted); background = shape(raised, 10)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            textDirection = View.TEXT_DIRECTION_LTR
        }
        val name = field("اسم الملف", old?.name ?: "")
        val host = field("عنوان الخادم Host", old?.host ?: "")
        val port = field("المنفذ Port", old?.port?.toString() ?: "443")
        val provider = spinner(listOf("مخصص", "Djezzy", "Mobilis", "Ooredoo"), old?.provider ?: "مخصص")
        val protocol = spinner(listOf("WireGuard", "OpenVPN", "SSH", "V2Ray / Xray", "WebSocket"), old?.protocol ?: "WireGuard")
        listOf(name, host, port).forEach { e ->
            form.addView(e, LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(8) })
        }
        form.addView(text("المزود", 12, muted)); form.addView(provider)
        form.addView(text("نوع البروتوكول", 12, muted)); form.addView(protocol)
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (old == null) "إضافة ملف اتصال" else "تعديل ملف الاتصال")
            .setView(form).setNegativeButton("إلغاء", null).setPositiveButton("حفظ", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n = name.text.toString().trim()
                val h = host.text.toString().trim()
                val p = port.text.toString().trim().toIntOrNull()
                if (n.isBlank() || h.isBlank() || p == null || p !in 1..65535) {
                    Toast.makeText(this, "تحقق من الاسم والخادم والمنفذ (1–65535)", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val item = ServerProfile(old?.id ?: System.currentTimeMillis(), n,
                    provider.selectedItem.toString(), h, p, protocol.selectedItem.toString())
                val all = loadProfiles().toMutableList()
                val ix = all.indexOfFirst { it.id == item.id }
                if (ix >= 0) all[ix] = item else all.add(item)
                saveProfiles(all)
                log(if (old == null) "تمت إضافة الملف: $n" else "تم تعديل الملف: $n")
                dialog.dismiss()
                refreshAll()
            }
        }
        dialog.show()
    }

    private fun spinner(options: List<String>, selected: String): Spinner {
        return Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, options)
            setSelection(options.indexOf(selected).coerceAtLeast(0))
        }
    }

    private fun loadProfiles(): List<ServerProfile> = try {
        val a = JSONArray(prefs.getString("profiles", "[]"))
        (0 until a.length()).mapNotNull { i ->
            val o = a.optJSONObject(i) ?: return@mapNotNull null
            ServerProfile(o.optLong("id"), o.optString("name"), o.optString("provider"),
                o.optString("host"), o.optInt("port"), o.optString("protocol"))
        }
    } catch (_: Exception) { emptyList() }

    private fun saveProfiles(items: List<ServerProfile>) {
        val a = JSONArray()
        items.forEach { p -> a.put(JSONObject().apply {
            put("id", p.id); put("name", p.name); put("provider", p.provider)
            put("host", p.host); put("port", p.port); put("protocol", p.protocol)
        }) }
        prefs.edit().putString("profiles", a.toString()).apply()
    }

    private fun log(message: String) {
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val old = prefs.getString("logs", "") ?: ""
        val lines = (listOf("$stamp • $message") + old.lines().filter { it.isNotBlank() }).take(25)
        prefs.edit().putString("logs", lines.joinToString("\n")).apply()
        if (::logs.isInitialized) renderLogs()
    }

    private fun renderLogs() {
        logs.removeAllViews()
        val lines = (prefs.getString("logs", "") ?: "").lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) logs.addView(text("لا توجد أحداث بعد.", 12, muted))
        lines.forEach { line ->
            val t = text(line, 12, muted)
            t.setPadding(dp(10), dp(9), dp(10), dp(9))
            t.background = shape(cardColor, 9)
            logs.addView(t, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(5) })
        }
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        background = shape(cardColor, 17)
    }

    private fun text(s: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = s; textSize = size.toFloat(); setTextColor(color)
        if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        textDirection = View.TEXT_DIRECTION_FIRST_STRONG
    }

    private fun button(s: String, color: Int) = Button(this).apply {
        text = s; textSize = 13f; setTextColor(white); isAllCaps = false
        background = shape(color, 12); minHeight = dp(46); minimumHeight = dp(46)
        stateListAnimator = null
        layoutParams = LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(5) }
    }

    private fun shape(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }

    private fun gap(h: Int) { root.addView(View(this), LinearLayout.LayoutParams(1, dp(h))) }
    private fun gapIn(parent: LinearLayout, h: Int) { parent.addView(View(this), LinearLayout.LayoutParams(1, dp(h))) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
