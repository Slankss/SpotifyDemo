package com.okankkl.spotifydemo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.okankkl.spotifydemo.model.Music
import com.okankkl.spotifydemo.service.MusicService

class MusicBroadcastReceiver(
    val togglePlay: () -> Unit = {},
    val onChangeCurrentMusic: (music: Music) -> Unit = {}
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            MusicService.Companion.ACTION_PLAY -> togglePlay()
            MusicService.Companion.ACTION_PAUSE -> togglePlay()
            MusicService.Companion.ACTION_CURRENT_MUSIC -> {
                @Suppress("DEPRECATION")
                val music = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(MusicService.Companion.DATA_MUSIC, Music::class.java)
                } else {
                    intent.getParcelableExtra<Music>(MusicService.Companion.DATA_MUSIC)
                }

                music?.let { onChangeCurrentMusic(music) }
            }
        }
    }
}