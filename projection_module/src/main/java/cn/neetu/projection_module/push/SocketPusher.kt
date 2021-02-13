package cn.neetu.projection_module.push

import android.media.projection.MediaProjection
import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

class SocketPusher(private val port: Int, private val mediaProjection: MediaProjection) {
    private var mWebSocket: WebSocket? = null
    private val mCodec = ProjectionCodec(mediaProjection)

    init {
        mCodec.callback = {
            Log.d(TAG, "sending frame...")
            mWebSocket?.send(it)
        }
    }
    fun start() {
        mWebSocketServer.start()
        mCodec.start()
    }

    fun close() {
        try {
            mCodec.stop()
            mWebSocket?.close()
            mWebSocketServer.stop()
        } catch (t: Exception) {
            t.printStackTrace()
        }

    }

    private val mWebSocketServer = object : WebSocketServer(InetSocketAddress(port)) {
        override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
            mWebSocket = conn
        }

        override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
            Log.i(TAG, "Socket closed $reason")
            mWebSocket = null
        }

        override fun onMessage(conn: WebSocket?, message: String?) {

        }

        override fun onStart() {

        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            Log.i(TAG, "Socket error ${ex?.message}")
            mWebSocket = null
        }

    }

    companion object {
        private const val TAG = "SocketPusher"
    }
}