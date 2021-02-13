package cn.neetu.androidmedialearn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.neetu.androidmedialearn.databinding.ActivityMainBinding
import cn.neetu.projection_module.push.ProjectionPushActivity

class MainActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }

    fun jumpToProjectionServer(view: View) {
        startActivity(Intent(this, ProjectionPushActivity::class.java))
    }

    fun jumpToProjectionClient(view: View) {

    }
}