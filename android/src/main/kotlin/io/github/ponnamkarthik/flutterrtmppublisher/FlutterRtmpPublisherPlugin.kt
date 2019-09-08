package io.github.ponnamkarthik.flutterrtmppublisher

import android.util.Log
import io.flutter.plugin.common.PluginRegistry.Registrar
import android.content.Intent
import android.app.Activity
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import java.util.logging.StreamHandler


class FlutterRtmpPublisherPlugin internal constructor(internal var activity: Activity) : MethodChannel.MethodCallHandler, EventChannel.StreamHandler {

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {

    if (call.method.equals("stream")) {
      val url = call.argument<String>("url")

      val playerIntent = Intent(activity, RTMPActivity::class.java)
      playerIntent.putExtra("url", url)


      activity.startActivityForResult(playerIntent, RTMP_STREAM_RESULT)

    } else {
      result.notImplemented()
    }
  }

  override fun onListen(o: Any, eventSink: EventChannel.EventSink) {
    events = eventSink
  }

  override fun onCancel(o: Any) {
    events = null
  }

  companion object {

    private val STREAM_CHANNEL_NAME = "rtmp_publisher_stream"
    private val RTMP_STREAM_RESULT = 66646
    internal var events: EventChannel.EventSink? = null
    /**
     * Plugin registration.
     */
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "rtmp_publisher")
      channel.setMethodCallHandler(FlutterRtmpPublisherPlugin(registrar.activity()))
      registrar.addActivityResultListener(object : PluginRegistry.ActivityResultListener {
        override fun onActivityResult(i: Int, i1: Int, intent: Intent?): Boolean {
          if (i == RTMP_STREAM_RESULT) {
            if (intent != null) {
              if (intent.getIntExtra("done", -1) == 0) {
                if (events != null) {
                  events!!.success("done")
                }
              }
            }
          }
          return false
        }
      })

      val eventChannel = EventChannel(registrar.messenger(), STREAM_CHANNEL_NAME)
      val youtubeWithEventChannel = FlutterRtmpPublisherPlugin(registrar.activity())
      eventChannel.setStreamHandler(youtubeWithEventChannel)
    }
  }
}