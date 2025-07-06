package com.okankkl.spotifydemo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.okankkl.spotifydemo.MainActivity
import com.okankkl.spotifydemo.R
import com.okankkl.spotifydemo.data.MusicData
import com.okankkl.spotifydemo.model.Music
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicService: Service() {

    companion object {
        var isRunning = false
        const val ACTION_PLAY = "com.okankkl.spotify.demo.action_play"
        const val ACTION_PAUSE = "com.okankkl.spotify.demo.action_pause"
        const val ACTION_NEXT = "com.okankkl.spotify.demo.action_next"
        const val ACTION_PREV = "com.okankkl.spotify.demo.action_prev"
        const val ACTION_CURRENT_MUSIC = "com.okankkl.spotify.demo.action_current_music"
        const val ACTION_OPEN_MUSIC_SCREEN = "com.okankkl.spotify.demo.action_open_music_screen"
        const val DATA_MUSIC = "music"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "music_channel"
    }

    private val binder = MusicBinder()
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }
    private var musicList: List<Music>? = null
    private var shuffledMusicList: List<Music>? = null
    private var currentMusic: Music? = null
    private var repeat = false
    private var progressJob: Job? = null
    private val progressChannel = Channel<Float>(Channel.CONFLATED)
    // Conflated takes only last value

    inner class MusicBinder: Binder(){
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        // Create notification channel
        createNotificationChannel()

        musicList = MusicData.musicData
        currentMusic = musicList?.first()

        mediaSession = MediaSessionCompat(this, "MusicService").apply {
//            setCallback( object: MediaSessionCompat.Callback() {
//                override fun onPlay() {
//                    Log.d("ICARDI","PLAY")
//                    play()
//                    sendBroadcast(Intent(ACTION_PLAY))
//                }
//
//                override fun onPause() {
//                    Log.d("ICARDI","PLAY")
//                    pause()
//                    sendBroadcast(Intent(ACTION_PAUSE))
//                }
//
//                override fun onSkipToNext() {
//                    skip()
//                }
//
//                override fun onSkipToPrevious() {
//                    skip(true)
//                }
//
//                override fun onSeekTo(pos: Long) {
//                    if (::mediaPlayer.isInitialized) {
//                        mediaPlayer.seekTo(pos.toInt())
//                    }
//                }
//            })
            isActive = true
        }
        mediaController = MediaControllerCompat(this, mediaSession.sessionToken)
        currentMusic?.let {
            mediaPlayer = MediaPlayer.create(this, it.musicPath)
            startForeground(NOTIFICATION_ID, buildNotification(it))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_PLAY -> {
                play()
                sendBroadcast(Intent(ACTION_PLAY))
            }
            ACTION_PAUSE -> {
                pause()
                sendBroadcast(Intent(ACTION_PLAY))
            }
            ACTION_NEXT -> skip()
            ACTION_PREV -> skip(true)
        }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    fun setMusicList(musicList: List<Music>){
        this.musicList = musicList
    }

    fun setMusic(music: Music?){
        this.currentMusic = music
    }

    fun getCurrentMusic(): Music? = currentMusic
    fun getMusicList(): List<Music> = musicList.orEmpty()

    fun startProgressUpdates() {
        mediaController.repeatMode
        if (progressJob?.isActive == true) return
        progressJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if(::mediaPlayer.isInitialized) {
                    val playing = try {
                        mediaPlayer.isPlaying
                    } catch(_: IllegalStateException) {
                        false
                    }
                    if (playing) {
                        progressChannel.send(progress())
                    }
                    try {
                        if (progress().toInt() == duration()){
                            if (repeat) restart()
                            else skip(isPlaying = true)
                        }
                    } catch (_: IllegalStateException) {}
                }
                delay(600)
            }
        }
    }

    fun getProgressChannel(): ReceiveChannel<Float> = progressChannel

    fun stopProgress(){
        progressJob?.cancel()
    }

    fun playNewMusic(music: Music? = null, isPlaying: Boolean? = null){
        if (music != null){
            currentMusic = music
        }
        currentMusic?.let { music ->
            val isPlaying = isPlaying ?: mediaPlayer.isPlaying

            mediaPlayer.release()
            mediaPlayer = MediaPlayer.create(this, music.musicPath)

            if (isPlaying) {
                play()
            }

            showNotification()

            val intent = Intent(ACTION_CURRENT_MUSIC).apply {
                putExtra(DATA_MUSIC,music)
            }
            sendBroadcast(intent)
        }
    }

    fun play() {
        if ( (::mediaPlayer.isInitialized) && !mediaPlayer.isPlaying) {
            startProgressUpdates()
            mediaPlayer.start()
            showNotification()
        }
    }

    fun pause() {
        if ( (::mediaPlayer.isInitialized) && mediaPlayer.isPlaying) {
            stopProgress()
            mediaPlayer.pause()
            showNotification()
        }
    }

    fun restart(){
        if (::mediaPlayer.isInitialized){
            CoroutineScope(Dispatchers.Default).launch {
                progressChannel.send(0f)
            }
            mediaPlayer.seekTo(0)
            if (mediaPlayer.isPlaying) mediaPlayer.start()
            showNotification()
        }
    }

    fun seek(position: Float) {
        if (::mediaPlayer.isInitialized) {
            val progress = (position * duration()).toInt()
            mediaPlayer.seekTo((progress * 1000).toLong(), MediaPlayer.SEEK_CLOSEST_SYNC)
            CoroutineScope(Dispatchers.Default).launch {
                progressChannel.send(progress.toFloat())
            }
            startProgressUpdates()
        }
    }

    fun skip(prev: Boolean = false, isPlaying: Boolean? = null) {
        if ( repeat || (prev && progress() >= 10) ) {
            restart()
            return
        }
        val musicList = (shuffledMusicList ?: musicList)
        musicList?.let { musicList ->
            val currentIndex = musicList.indexOf(currentMusic)
            val newIndex = if (prev){
                if (currentIndex > 0) currentIndex - 1 else (musicList.lastIndex)
            } else {
                if (currentIndex < (musicList.size - 1)) currentIndex + 1 else 0
            }
            currentMusic = musicList[newIndex]

            playNewMusic(isPlaying = isPlaying)
        }
    }

    fun progress(): Float {
        return if (::mediaPlayer.isInitialized) {
            mediaPlayer.currentPosition / 1000f // ms to second
        } else {
            0f
        }
    }
    fun duration(): Int = mediaPlayer.duration / 1000
    fun isPlaying(): Boolean = mediaPlayer.isPlaying

    fun buildNotification(music: Music): Notification {
        // Create actions
        val playPauseAction = if (mediaPlayer.isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "Pause",
                getPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play, "Play",
                getPendingIntent(ACTION_PLAY)
            )
        }

        val prevAction = NotificationCompat.Action(
            R.drawable.ic_skip_prev,
            "Previous",
            getPendingIntent(ACTION_PREV)
        )

        val nextAction = NotificationCompat.Action(
            R.drawable.ic_skip_next,
            "Next",
            getPendingIntent(ACTION_NEXT)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(music.title)
            .setContentText(music.artist)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .setFullScreenIntent(getFullScreenIntent(), true) // Priority Level
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)

            .setAutoCancel(false)
            .setOngoing(isPlaying())
            .build()
    }

    fun showNotification() {
        val music = currentMusic ?: return
        startForeground(NOTIFICATION_ID, buildNotification(music))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Playback",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Music playback controls"
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getFullScreenIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            ACTION_OPEN_MUSIC_SCREEN.hashCode(),
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun setRepeat() {
        this.repeat = !(this.repeat)
    }

    fun setShuffle() {
        shuffledMusicList = if (shuffledMusicList == null) musicList?.shuffled() else null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
        musicList = null
        isRunning = false
    }
}