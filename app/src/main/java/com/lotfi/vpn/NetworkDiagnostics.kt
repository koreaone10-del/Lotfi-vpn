package com.lotfi.vpn

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

object NetworkDiagnostics {
    suspend fun test(host:String,port:Int):String = withContext(Dispatchers.IO) {
        if(host.isBlank()) return@withContext "يرجى إدخال اسم نطاق أو عنوان IP."
        if(port !in 1..65535) return@withContext "رقم المنفذ غير صالح."
        try {
            val t=System.nanoTime()
            val addresses=InetAddress.getAllByName(host)
            val dns=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-t)
            if(addresses.isEmpty()) return@withContext "لم يتم العثور على عنوان IP."
            val list=addresses.take(4).joinToString("\n"){"• ${it.hostAddress}"}
            val socket=Socket()
            try {
                val tcpStart=System.nanoTime()
                socket.connect(InetSocketAddress(host,port),4500)
                val tcp=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-tcpStart)
                "نجح DNS. زمن DNS: ${dns} ms\nالعناوين:\n$list\n\nTCP/$port: متاح\nزمن الاتصال: ${tcp} ms"
            } finally { socket.close() }
        } catch(e:Exception) {
            "فشل الاختبار: ${e.javaClass.simpleName}\n${e.message ?: "لا توجد تفاصيل"}"
        }
    }
}
