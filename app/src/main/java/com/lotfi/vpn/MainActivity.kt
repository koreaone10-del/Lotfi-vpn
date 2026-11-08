package com.lotfi.vpn

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.lotfi.vpn.data.ConnectionState
import com.lotfi.vpn.ui.StatusFormatter

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusView = findViewById<TextView>(R.id.connectionStatus)
        statusView.text = StatusFormatter.label(ConnectionState.DISCONNECTED)
    }
}
