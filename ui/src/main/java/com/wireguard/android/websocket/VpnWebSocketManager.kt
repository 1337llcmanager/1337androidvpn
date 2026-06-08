package com.wireguard.android.websocket

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class VpnWebSocketManager(private val context: Context) {
    private var webSocketService: WebSocketVpnServer? = null
    private var bound = false
    
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WebSocketVpnServer.LocalBinder
            webSocketService = binder.getService()
            bound = true
            Log.d(TAG, "WebSocket service connected")
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            webSocketService = null
            Log.d(TAG, "WebSocket service disconnected")
        }
    }
    
    fun startWebSocketServer() {
        if (!bound) {
            val intent = Intent(context, WebSocketVpnServer::class.java)
            context.startService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }
    
    fun stopWebSocketServer() {
        if (bound) {
            context.unbindService(connection)
            val intent = Intent(context, WebSocketVpnServer::class.java)
            context.stopService(intent)
            bound = false
        }
    }
    
    companion object {
        private const val TAG = "VpnWebSocketManager"
    }
}
