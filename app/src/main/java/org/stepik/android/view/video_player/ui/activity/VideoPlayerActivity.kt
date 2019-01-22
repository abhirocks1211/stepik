package org.stepik.android.view.video_player.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_player.*
import org.stepic.droid.R
import org.stepik.android.model.Video
import org.stepik.android.view.video_player.ui.service.VideoPlayerForegroundService

class VideoPlayerActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_EXTERNAL_VIDEO = "external_video"
        private const val EXTRA_CACHED_VIDEO = "cached_video"

        fun createIntent(context: Context, externalVideo: Video?, cachedVideo: Video?): Intent =
            Intent(context, VideoPlayerActivity::class.java)
                .putExtra(EXTRA_EXTERNAL_VIDEO, externalVideo)
                .putExtra(EXTRA_CACHED_VIDEO, cachedVideo)
    }

    private var exoPlayer: ExoPlayer? = null

    private val videoServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is VideoPlayerForegroundService.VideoPlayerBinder) {
                exoPlayer = service.getPlayer()
                attachPlayer()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            exoPlayer = null
        }
    }

    private val serviceIntent by lazy { Intent(this, VideoPlayerForegroundService::class.java).putExtras(intent) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        Util.startForegroundService(this, serviceIntent)
    }

    override fun onStart() {
        super.onStart()
        bindService(serviceIntent, videoServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun attachPlayer() {
        playerView.player = exoPlayer
        exoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_IDLE) {
                    finish()
                }
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                error?.printStackTrace()
            }
        })
    }

    override fun onStop() {
        unbindService(videoServiceConnection)
        playerView.player = null

        if (isFinishing) {
            stopService(serviceIntent)
        }

        super.onStop()
    }
}