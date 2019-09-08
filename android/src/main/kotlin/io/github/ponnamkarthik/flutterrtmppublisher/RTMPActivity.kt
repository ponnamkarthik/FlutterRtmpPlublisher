package io.github.ponnamkarthik.flutterrtmppublisher

//import android.opengl.GLSurfaceView
//import android.os.Bundle
//import android.os.Handler
//import android.util.Log
//import android.view.Gravity
//import android.view.View
//import android.widget.*
//import android.support.v7.app.AppCompatActivity
//import com.takusemba.rtmppublisher.Publisher
//import com.takusemba.rtmppublisher.PublisherListener
//import java.util.*
//
//
//class RTMPActivity: AppCompatActivity(), PublisherListener {
//
//    private lateinit var publisher: Publisher
//    private lateinit var glView: GLSurfaceView
//    private lateinit var container: RelativeLayout
//    private lateinit var publishButton: Button
//    private lateinit var cameraButton: ImageView
//    private lateinit var label: TextView
//
//    private var url = ""
//    private val handler = Handler()
//    private var thread: Thread? = null
//    private var isCounting = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.main)
//        glView = findViewById(R.id.surface_view)
//        container = findViewById(R.id.container)
//        publishButton = findViewById(R.id.toggle_publish)
//        cameraButton = findViewById(R.id.toggle_camera)
//        label = findViewById(R.id.live_label)
//
//        url = intent.getStringExtra("url")
//
//        if (url.isBlank()) {
//            Toast.makeText(this, R.string.error_empty_url, Toast.LENGTH_SHORT)
//                    .apply { setGravity(Gravity.CENTER, 0, 0) }
//                    .run { show() }
//        } else {
//            publisher = Publisher.Builder(this)
//                    .setGlView(glView)
//                    .setUrl(url)
//                    .setSize(Publisher.Builder.DEFAULT_WIDTH, Publisher.Builder.DEFAULT_HEIGHT)
//                    .setAudioBitrate(Publisher.Builder.DEFAULT_AUDIO_BITRATE)
//                    .setVideoBitrate(Publisher.Builder.DEFAULT_VIDEO_BITRATE)
//                    .setCameraMode(Publisher.Builder.DEFAULT_MODE)
//                    .setListener(this)
//                    .build()
//
//            publishButton.setOnClickListener {
//                if (publisher.isPublishing) {
//                    publisher.stopPublishing()
//                } else {
//                    publisher.startPublishing()
//                }
//            }
//
//            cameraButton.setOnClickListener {
//                publisher.switchCamera()
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (url.isNotBlank()) {
//            updateControls()
//        }
//    }
//
//    override fun onStarted() {
//        Toast.makeText(this, R.string.started_publishing, Toast.LENGTH_SHORT)
//                .apply { setGravity(Gravity.CENTER, 0, 0) }
//                .run { show() }
//        updateControls()
//        startCounting()
//    }
//
//    override fun onStopped() {
//        Toast.makeText(this, R.string.stopped_publishing, Toast.LENGTH_SHORT)
//                .apply { setGravity(Gravity.CENTER, 0, 0) }
//                .run { show() }
//        updateControls()
//        stopCounting()
//    }
//
//    override fun onDisconnected() {
//        Toast.makeText(this, R.string.disconnected_publishing, Toast.LENGTH_SHORT)
//                .apply { setGravity(Gravity.CENTER, 0, 0) }
//                .run { show() }
//        updateControls()
//        stopCounting()
//    }
//
//    override fun onFailedToConnect() {
//        Toast.makeText(this, R.string.failed_publishing, Toast.LENGTH_SHORT)
//                .apply { setGravity(Gravity.CENTER, 0, 0) }
//                .run { show() }
//        updateControls()
//        stopCounting()
//    }
//
//    private fun updateControls() {
//        publishButton.text = getString(if (publisher.isPublishing) R.string.stop_publishing else R.string.start_publishing)
//    }
//
//    private fun startCounting() {
//        isCounting = true
//        label.text = getString(R.string.publishing_label, 0L.format(), 0L.format())
//        label.visibility = View.VISIBLE
//        val startedAt = System.currentTimeMillis()
//        var updatedAt = System.currentTimeMillis()
//        thread = Thread {
//            while (isCounting) {
//                if (System.currentTimeMillis() - updatedAt > 1000) {
//                    updatedAt = System.currentTimeMillis()
//                    handler.post {
//                        val diff = System.currentTimeMillis() - startedAt
//                        val second = diff / 1000 % 60
//                        val min = diff / 1000 / 60
//                        label.text = getString(R.string.publishing_label, min.format(), second.format())
//                    }
//                }
//            }
//        }
//        thread?.start()
//    }
//
//    private fun stopCounting() {
//        isCounting = false
//        label.text = ""
//        label.visibility = View.GONE
//        thread?.interrupt()
//    }
//
//    private fun Long.format(): String {
//        return String.format("%02d", this)
//    }
//}


//yansem
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


//import android.os.Build
//import android.os.Bundle
//import android.os.Environment
//import android.support.v7.app.AppCompatActivity
//import android.view.SurfaceHolder
//import android.view.SurfaceView
//import android.view.View
//import android.view.WindowManager
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import com.pedro.encoder.input.video.CameraOpenException
//import com.pedro.rtplibrary.rtmp.RtmpCamera1
//import java.io.File
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import net.ossrs.rtmp.ConnectCheckerRtmp
//
///**
// * More documentation see:
// * [com.pedro.rtplibrary.base.Camera1Base]
// * [com.pedro.rtplibrary.rtmp.RtmpCamera1]
// */
//class RTMPActivity : AppCompatActivity(), ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {
//
//    private var rtmpCamera1: RtmpCamera1? = null
//    private var button: Button? = null
//    private var bRecord: Button? = null
//    private var etUrl: EditText? = null
//
//    private var currentDateAndTime = ""
//    private val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/rtmp-rtsp-stream-client-java")
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        setContentView(R.layout.activity_example)
//        val surfaceView: SurfaceView = findViewById(R.id.surfaceView)
//        button = findViewById(R.id.b_start_stop)
//        button!!.setOnClickListener(this)
//        bRecord = findViewById(R.id.b_record)
//        bRecord!!.setOnClickListener(this)
//        val switchCamera: Button = findViewById(R.id.switch_camera)
//        switchCamera.setOnClickListener(this)
//        etUrl = findViewById(R.id.et_rtp_url)
//        etUrl!!.setHint(R.string.hint_rtmp)
//        rtmpCamera1 = RtmpCamera1(surfaceView, this)
//        surfaceView.getHolder().addCallback(this)
//
//        etUrl?.setText(intent.getStringExtra("url"))
//    }
//
//    override fun onConnectionSuccessRtmp() {
//        runOnUiThread { Toast.makeText(this@RTMPActivity, "Connection success", Toast.LENGTH_SHORT).show() }
//    }
//
//    override fun onConnectionFailedRtmp(reason: String) {
//        runOnUiThread {
//            Toast.makeText(this@RTMPActivity, "Connection failed. $reason", Toast.LENGTH_SHORT)
//                    .show()
//            rtmpCamera1!!.stopStream()
//            button!!.setText(R.string.start_button)
//        }
//    }
//
//    override fun onDisconnectRtmp() {
//        runOnUiThread { Toast.makeText(this@RTMPActivity, "Disconnected", Toast.LENGTH_SHORT).show() }
//    }
//
//    override fun onAuthErrorRtmp() {
//        runOnUiThread { Toast.makeText(this@RTMPActivity, "Auth error", Toast.LENGTH_SHORT).show() }
//    }
//
//    override fun onAuthSuccessRtmp() {
//        runOnUiThread { Toast.makeText(this@RTMPActivity, "Auth success", Toast.LENGTH_SHORT).show() }
//    }
//
//    override fun onClick(view: View) {
//        when (view.id) {
//            R.id.b_start_stop -> if (!rtmpCamera1!!.isStreaming) {
//                if (rtmpCamera1!!.isRecording || rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
//                    button!!.setText(R.string.stop_button)
//                    rtmpCamera1!!.startStream(etUrl!!.text.toString())
//                } else {
//                    Toast.makeText(this, "Error preparing stream, This device cant do it",
//                            Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                button!!.setText(R.string.start_button)
//                rtmpCamera1!!.stopStream()
//            }
//            R.id.switch_camera -> try {
//                rtmpCamera1!!.switchCamera()
//            } catch (e: CameraOpenException) {
//                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
//            }
//
//            R.id.b_record -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                if (!rtmpCamera1!!.isRecording) {
//                    try {
//                        if (!folder.exists()) {
//                            folder.mkdir()
//                        }
//                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
//                        currentDateAndTime = sdf.format(Date())
//                        if (!rtmpCamera1!!.isStreaming) {
//                            if (rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
//                                rtmpCamera1!!.startRecord(
//                                        folder.absolutePath + "/" + currentDateAndTime + ".mp4")
//                                bRecord!!.setText(R.string.stop_record)
//                                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
//                            } else {
//                                Toast.makeText(this, "Error preparing stream, This device cant do it",
//                                        Toast.LENGTH_SHORT).show()
//                            }
//                        } else {
//                            rtmpCamera1!!.startRecord(
//                                    folder.absolutePath + "/" + currentDateAndTime + ".mp4")
//                            bRecord!!.setText(R.string.stop_record)
//                            Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
//                        }
//                    } catch (e: IOException) {
//                        rtmpCamera1!!.stopRecord()
//                        bRecord!!.setText(R.string.start_record)
//                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
//                    }
//
//                } else {
//                    rtmpCamera1!!.stopRecord()
//                    bRecord!!.setText(R.string.start_record)
//                    Toast.makeText(this,
//                            "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
//                            Toast.LENGTH_SHORT).show()
//                    currentDateAndTime = ""
//                }
//            } else {
//                Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
//                        Toast.LENGTH_SHORT).show()
//            }
//            else -> {
//            }
//        }
//    }
//
//    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
//
//    }
//
//    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
//        rtmpCamera1!!.startPreview()
//    }
//
//    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1!!.isRecording) {
//            rtmpCamera1!!.stopRecord()
//            bRecord!!.setText(R.string.start_record)
//            Toast.makeText(this,
//                    "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
//                    Toast.LENGTH_SHORT).show()
//            currentDateAndTime = ""
//        }
//        if (rtmpCamera1!!.isStreaming) {
//            rtmpCamera1!!.stopStream()
//            button!!.text = resources.getString(R.string.start_button)
//        }
//        rtmpCamera1!!.stopPreview()
//    }
//}