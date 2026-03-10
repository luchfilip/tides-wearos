package dev.tidesapp.wearos.player.service

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dev.tidesapp.wearos.player.data.PlayerRepositoryImpl
import dev.tidesapp.wearos.player.playback.MusicServiceController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WearMusicServiceEntryPoint {
    fun playerRepository(): PlayerRepositoryImpl
}

class WearMusicService : MediaSessionService() {

    private lateinit var playerRepository: PlayerRepositoryImpl
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private lateinit var serviceController: MusicServiceController
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            WearMusicServiceEntryPoint::class.java,
        )
        playerRepository = entryPoint.playerRepository()

        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        player = exoPlayer
        playerRepository.setPlayer(exoPlayer)

        mediaSession = MediaSession.Builder(this, exoPlayer).build()

        serviceController = MusicServiceController(serviceScope)
        serviceController.setStopCallback { stopSelf() }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        serviceController.onTaskRemoved()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceController.onDestroy()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        playerRepository.clearPlayer()
        player = null
        serviceScope.cancel()
        super.onDestroy()
    }
}
