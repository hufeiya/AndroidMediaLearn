package cn.neetu.projection_module.push

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cn.neetu.projection_module.R
import cn.neetu.projection_module.databinding.ActivityProjectPushBinding

class ProjectionPushActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityProjectPushBinding
    private var isActive = false
    private var mediaProjectionManager: MediaProjectionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityProjectPushBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        checkPermission()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        startActivityForResult(mediaProjectionManager?.createScreenCaptureIntent(), 88)
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
                ), 1
            )
            false
        } else {
            true
        }
    }

    fun switchPushState(view: View) {
        if (!checkPermission()) return
        val button = view as Button
        /*socketPusher?.apply {
            if (isActive) {
                close()
                button.setText(R.string.stop_push_stream)
            } else {
                start()
                button.setText(R.string.start_push_steam)
            }
            isActive = !isActive
        }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 88) {
            val serviceIntent = Intent(this, ProjectionService::class.java)
            serviceIntent.putExtra("code", resultCode)
            serviceIntent.putExtra("data", data)
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }
}