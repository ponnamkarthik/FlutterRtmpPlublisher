package io.github.ponnamkarthik.flutterrtmppublisher

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.github.faucamp.simplertmp.RtmpHandler
import com.seu.magicfilter.utils.MagicFilterType
import net.ossrs.yasea.SrsCameraView
import net.ossrs.yasea.SrsEncodeHandler
import net.ossrs.yasea.SrsPublisher
import net.ossrs.yasea.SrsRecordHandler
import java.io.IOException
import java.net.SocketException
import java.util.Random

class RTMPActivity:AppCompatActivity(), RtmpHandler.RtmpListener, SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener {
    private lateinit var btnPublish: Button
    private lateinit var btnSwitchCamera:ImageView
    private lateinit var btnBack:ImageView
//    private lateinit var btnPause:Button
    private lateinit var sp:SharedPreferences
    private var rtmpUrl = "rtmp://live.mux.com/app/22083600-1066-72a9-adf9-704aaf1c42b8"
    private lateinit var mPublisher:SrsPublisher


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        // response screen rotation event
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)
        // restore data.


        sp = getSharedPreferences("Yasea", MODE_PRIVATE)

        rtmpUrl = intent.getStringExtra("url")

        btnPublish = findViewById<Button>(R.id.publish)
        btnSwitchCamera = findViewById<ImageView>(R.id.swCam)
        btnBack = findViewById<ImageView>(R.id.backButton)
//        btnPause = findViewById(R.id.pause) as Button
//        btnPause.isEnabled = false
        mPublisher = SrsPublisher(findViewById<SrsCameraView>(R.id.glsurfaceview_camera))
        mPublisher.setEncodeHandler(SrsEncodeHandler(this))
        mPublisher.setRtmpHandler(RtmpHandler(this))
        mPublisher.setRecordHandler(SrsRecordHandler(this))
        mPublisher.setPreviewResolution(640, 360)
        mPublisher.setOutputResolution(360, 640)
        mPublisher.setVideoHDMode()
        mPublisher.startCamera()
        btnPublish.setOnClickListener {
            if (btnPublish.text.toString().contains("Start")) {
                val editor = sp.edit()
                editor.putString("rtmpUrl", rtmpUrl)
                editor.apply()
                mPublisher.startPublish(rtmpUrl)
                mPublisher.startCamera()
//                if (btnSwitchEncoder.text.toString().contentEquals("soft encoder")) {
//                    Toast.makeText(applicationContext, "Use hard encoder", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(applicationContext, "Use soft encoder", Toast.LENGTH_SHORT).show()
//                }
                btnPublish.text = getString(R.string.stop_publishing)

//                btnPause.isEnabled = true
            } else if (btnPublish.text.toString().contains("Stop")) {
                mPublisher.stopPublish()
                mPublisher.stopRecord()
                btnPublish.text = getString(R.string.start_publishing)

//                btnPause.isEnabled = false
            }
        }
//        btnPause.setOnClickListener {
//            if (btnPause.text.toString().equals("Pause")) {
//                mPublisher.pausePublish()
//                btnPause.text = "resume"
//            } else {
//                mPublisher.resumePublish()
//                btnPause.text = "Pause"
//            }
//        }
        btnSwitchCamera.setOnClickListener {
            mPublisher.switchCameraFace((mPublisher.cameraId + 1) % Camera.getNumberOfCameras())
        }

        btnBack.setOnClickListener {
            var msg = "";
            if(btnPublish.text.toString().contains("Stop")) {
                msg = "Do you want to stop streaming and go back?"
            } else {
                msg = "Do you want to go back?"
            }
            val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage(msg)
                    .setPositiveButton("GO Back") { dialog, which ->
                        dialog.dismiss()
                        mPublisher.stopPublish()
                        this.onBackPressed()
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.cancel()
                    }
                    .create()
            alertDialog.show()
        }
    }

    override fun onResume() {
        super.onResume()
        val btn = findViewById<Button>(R.id.publish)
        btn.isEnabled = true
        mPublisher.resumeRecord()
    }
    override  fun onPause() {
        super.onPause()
        mPublisher.pauseRecord()
    }
    override  fun onDestroy() {
        super.onDestroy()
        mPublisher.stopPublish()
        mPublisher.stopRecord()
    }
    override fun onConfigurationChanged(newConfig:Configuration) {
        super.onConfigurationChanged(newConfig)
        mPublisher.stopEncode()
        mPublisher.stopRecord()
        mPublisher.setScreenOrientation(newConfig.orientation)
        if (btnPublish.text.toString().contentEquals("stop"))
        {
            mPublisher.startEncode()
        }
        mPublisher.startCamera()
    }
    private fun handleException(e:Exception) {
        try
        {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            mPublisher.stopPublish()
            mPublisher.stopRecord()
            btnPublish.text = getString(R.string.start_publishing)
        }
        catch (e1:Exception) {
            //
        }
    }
    // Implementation of SrsRtmpListener.
    override fun onRtmpConnecting(msg:String) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
    }
    override fun onRtmpConnected(msg:String) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
    }
    override fun onRtmpVideoStreaming() {}
    override fun onRtmpAudioStreaming() {}
    override fun onRtmpStopped() {
        Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show()
    }
    override fun onRtmpDisconnected() {
        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show()
    }
    override fun onRtmpVideoFpsChanged(fps:Double) {
        Log.i(TAG, String.format("Output Fps: %f", fps))
    }
    override fun onRtmpVideoBitrateChanged(bitrate:Double) {
        val rate = bitrate.toInt()
        if (rate / 1000 > 0)
        {
            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000))
        }
        else
        {
            Log.i(TAG, String.format("Video bitrate: %d bps", rate))
        }
    }
    override fun onRtmpAudioBitrateChanged(bitrate:Double) {
        val rate = bitrate.toInt()
        if (rate / 1000 > 0)
        {
            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000))
        }
        else
        {
            Log.i(TAG, String.format("Audio bitrate: %d bps", rate))
        }
    }
    override fun onRtmpSocketException(e:SocketException) {
        handleException(e)
    }
    override fun onRtmpIOException(e:IOException) {
        handleException(e)
    }
    override fun onRtmpIllegalArgumentException(e:IllegalArgumentException) {
        handleException(e)
    }
    override fun onRtmpIllegalStateException(e:IllegalStateException) {
        handleException(e)
    }
    // Implementation of SrsRecordHandler.
    override fun onRecordPause() {
        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show()
    }
    override fun onRecordResume() {
        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show()
    }
    override fun onRecordStarted(msg:String) {
        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show()
    }
    override fun onRecordFinished(msg:String) {
        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show()
    }
    override fun onRecordIOException(e:IOException) {
        handleException(e)
    }
    override fun onRecordIllegalArgumentException(e:IllegalArgumentException) {
        handleException(e)
    }
    // Implementation of SrsEncodeHandler.
    override fun onNetworkWeak() {
        Toast.makeText(getApplicationContext(), "Network weak", Toast.LENGTH_SHORT).show()
    }
    override fun onNetworkResume() {
        Toast.makeText(getApplicationContext(), "Network resume", Toast.LENGTH_SHORT).show()
    }
    override fun onEncodeIllegalArgumentException(e:IllegalArgumentException) {
        handleException(e)
    }
    companion object {
        private val TAG = "Yasea"
        private fun getRandomAlphaString(length:Int):String {
            val base = "abcdefghijklmnopqrstuvwxyz"
            val random = Random()
            val sb = StringBuilder()
            for (i in 0 until length)
            {
                val number = random.nextInt(base.length)
                sb.append(base.get(number))
            }
            return sb.toString()
        }
        private fun getRandomAlphaDigitString(length:Int):String {
            val base = "abcdefghijklmnopqrstuvwxyz0123456789"
            val random = Random()
            val sb = StringBuilder()
            for (i in 0 until length)
            {
                val number = random.nextInt(base.length)
                sb.append(base.get(number))
            }
            return sb.toString()
        }
    }
}