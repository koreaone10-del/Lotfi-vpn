package com.lotfi.vpn.ui

import com.lotfi.vpn.data.ConnectionState

object StatusFormatter {

    fun label(state: ConnectionState): String = when (state) {
        ConnectionState.DISCONNECTED -> "غير متصل"
        ConnectionState.CONNECTING -> "جارٍ الاتصال..."
        ConnectionState.CONNECTED -> "متصل"
        ConnectionState.ERROR -> "حدث خطأ"
    }
}
