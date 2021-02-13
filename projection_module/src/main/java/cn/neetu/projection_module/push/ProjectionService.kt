package cn.neetu.projection_module.push

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.Parcelable


class ProjectionService: Service() {

    private var mResultCode = 0
    private var mResultData: Intent? = null
    private var projectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()
        mResultCode = intent.getIntExtra("code", -1)
        mResultData = intent.getParcelableExtra<Parcelable>("data") as? Intent
        this.projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        mMediaProjection = projectionManager?.getMediaProjection(mResultCode, mResultData!!)
        val socketPusher = SocketPusher(12001, mMediaProjection!!)
        socketPusher.start()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        val builder: Notification.Builder =
            Notification.Builder(this.applicationContext) //获取一个Notification构造器
        val nfIntent = Intent(this, ProjectionPushActivity::class.java) //点击后跳转的界面，可以设置跳转数据
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.mipmap.sym_def_app_icon
                )
            ) // set big icon on notification list
            //.setContentTitle("SMI InstantView")
            .setSmallIcon(R.mipmap.sym_def_app_icon) // set small icon in status bar
            .setContentText("is running......")
            .setWhen(System.currentTimeMillis())

        /*Android 8.0 adaptation*/
        //normal notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id")
        }
        //foreground notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "notification_id",
                "notification_name",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification: Notification = builder.build()
        notification.defaults = Notification.DEFAULT_SOUND
        startForeground(110, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}