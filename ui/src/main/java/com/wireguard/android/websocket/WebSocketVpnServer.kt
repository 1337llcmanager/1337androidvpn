package com.wireguard.android.websocket

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class WebSocketVpnServer : Service() {
    private var wsServer: VpnWebSocketServer? = null
    private val binder = LocalBinder()
    private val port = 8888
    
    inner class LocalBinder : Binder() {
        fun getService(): WebSocketVpnServer = this@WebSocketVpnServer
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startWebSocketServer()
        return START_STICKY
    }
    
    private fun startWebSocketServer() {
        try {
            wsServer = VpnWebSocketServer(InetSocketAddress("0.0.0.0", port))
            wsServer?.start()
            Log.d(TAG, "WebSocket server started on port $port")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start WebSocket server", e)
        }
    }
    
    override fun onDestroy() {
        wsServer?.stop()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent): IBinder = binder
    
    private inner class VpnWebSocketServer(addr: InetSocketAddress) : WebSocketServer(addr) {
        private val connectedClients = mutableSetOf<WebSocket>()
        
        override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
            if (conn != null) {
                connectedClients.add(conn)
                Log.d(TAG, "Client connected: ${conn.remoteSocketAddress}")
                conn.send("Connected to VPN WebSocket Server")
            }
        }
        
        override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
            if (conn != null) {
                connectedClients.remove(conn)
                Log.d(TAG, "Client disconnected: ${conn.remoteSocketAddress}")
            }
        }
        
        override fun onMessage(conn: WebSocket?, message: String?) {
            if (conn != null && message != null) {
                Log.d(TAG, "Message from ${conn.remoteSocketAddress}: $message")
                handleVpnCommand(conn, message)
            }
        }
        
        override fun onError(conn: WebSocket?, ex: Exception?) {
            Log.e(TAG, "WebSocket error", ex)
        }
        
        override fun onStart() {
            Log.d(TAG, "WebSocket server started")
        }
        
        private fun handleVpnCommand(conn: WebSocket, message: String) {
            // Parse commands and route traffic through VPN
            try {
                val response = when {
                    message.contains("connect") -> "Connecting to VPN..."
                    message.contains("status") -> "VPN Status: Active"
                    else -> "Command received: $message"
                }
                conn.send(response)
                broadcastToClients(message)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling command", e)
                conn.send("Error: ${e.message}")
            }
        }
        
        private fun broadcastToClients(message: String) {
            connectedClients.forEach { client ->
                client.send(message)
            }
        }
    }
    
    companion object {
        private const val TAG = "WebSocketVpnServer"
    }
}
