@file:OptIn(ExperimentalMaterial3Api::class)

package com.okankkl.spotifydemo.presentation

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.okankkl.spotifydemo.R
import com.okankkl.spotifydemo.presentation.components.MediaPlayerControl
import com.okankkl.spotifydemo.presentation.components.MusicTimeProgress
import com.okankkl.spotifydemo.presentation.components.MusicTitleRow
import com.okankkl.spotifydemo.presentation.ui.theme.BackgroundBrush
import com.okankkl.spotifydemo.presentation.ui.theme.SpotifyDemoTheme
import com.okankkl.spotifydemo.receiver.MusicBroadcastReceiver
import com.okankkl.spotifydemo.service.MusicService
import com.okankkl.spotifydemo.service.MusicService.Companion.ACTION_CURRENT_MUSIC
import com.okankkl.spotifydemo.service.MusicService.Companion.ACTION_NEXT
import com.okankkl.spotifydemo.service.MusicService.Companion.ACTION_PAUSE
import com.okankkl.spotifydemo.service.MusicService.Companion.ACTION_PLAY
import com.okankkl.spotifydemo.service.MusicService.Companion.ACTION_PREV
import kotlinx.coroutines.launch

@Composable
fun MusicScreen(
    modifier: Modifier = Modifier,
    musicScreenViewModel: MusicScreenViewModel = viewModel(),
) {
    val viewState by musicScreenViewModel.viewState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val context = LocalContext.current

    var musicService by remember { mutableStateOf<MusicService?>(null) }
    var serviceIsBound by remember { mutableStateOf(false) }
    val serviceConnection = remember {
        object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                musicService = (service as MusicService.MusicBinder).getService()
                musicService?.apply {
                    musicScreenViewModel.setMusic(getCurrentMusic())
                    musicScreenViewModel.setPlaying(isPlaying())
                    musicScreenViewModel.setDuration(duration())
                    musicScreenViewModel.observeProgress(getProgressChannel())
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                musicService = null
            }
        }
    }

    val musicReceiver = remember { MusicBroadcastReceiver(
        togglePlay = { musicScreenViewModel.setPlaying() },
        onChangeCurrentMusic = {
            musicScreenViewModel.setMusic(it)
            musicService?.apply {
                musicScreenViewModel.setDuration(duration())
            }
        }
    ) }

    // When resume the app get current music from the service
    LifecycleResumeEffect(true) {
        musicService?.apply {
            musicScreenViewModel.setMusic(getCurrentMusic())
            musicScreenViewModel.setPlaying(isPlaying())
        }
        onPauseOrDispose {  }
    }

    // When start the app, register the receiver
    LifecycleStartEffect(true) {
        // Register Receiver
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREV)
            addAction(ACTION_CURRENT_MUSIC)
        }
        ContextCompat.registerReceiver(
            context,
            musicReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )


        // Start Service
        if (!MusicService.isRunning) {
            context.startForegroundService(Intent(context, MusicService::class.java))
        }
        // Bind Service
        if (!serviceIsBound){
            context.bindService(Intent(context, MusicService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
            serviceIsBound = true
        }

        onStopOrDispose { context.unregisterReceiver(musicReceiver) }
    }

    // When Destroy the app, stop the service
    DisposableEffect(Unit) {
        onDispose {
            if (serviceIsBound) {
                context.unbindService(serviceConnection)
                serviceIsBound = false
            }
            musicService?.stopForeground(Service.STOP_FOREGROUND_REMOVE)
            musicService?.stopSelf()
        }
    }

    // When create the app, observe the channel
    LaunchedEffect(Lifecycle.State.CREATED) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
            musicScreenViewModel.channelFlow.collect { event ->
                when (event){
                    is MusicScreenViewEvent.RestartMusic -> musicService?.restart()
                    is MusicScreenViewEvent.GetMusicList -> {
                        musicService?.apply {
                            setMusicList(event.musicList)
                            setMusic(viewState.currentMusic)
                        }
                    }
                }
            }
        }
    }

    // Music queue bottom sheet visibility
    if (bottomSheetState.isVisible){
        MusicQueueSheet(
            sheetState = bottomSheetState,
            musicList = musicService?.getMusicList().orEmpty(),
            currentMusic = viewState.currentMusic,
            onSelect = { music ->
                if (viewState.currentMusic != music) musicService?.playNewMusic(music)
                scope.launch { bottomSheetState.hide() }
            }
        )
    }

    // Screen Ui
    MusicScreenUI(
        modifier = modifier,
        viewState = viewState,
        viewModel = musicScreenViewModel,
        musicService = musicService,
        bottomSheetState = bottomSheetState
    )
}

@Composable
private fun MusicScreenUI(
    modifier: Modifier = Modifier,
    viewState: MusicScreenViewState,
    viewModel: MusicScreenViewModel,
    musicService: MusicService? = null,
    bottomSheetState: SheetState,
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBrush),
    ) {
        IconButton(
            modifier = Modifier
                .padding(top = 10.dp, end = 10.dp)
                .align(Alignment.End),
            onClick = {
                scope.launch { bottomSheetState.show() }
            }
        ){
            Icon(
                painter = painterResource(R.drawable.ic_queue),
                contentDescription = null
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
        ){
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ){
                AnimatedContent(
                    modifier = Modifier
                        .fillMaxWidth(),
                    targetState = viewState.currentMusic,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally{ it } togetherWith
                                fadeOut() + slideOutHorizontally{ -it }
                    }
                ) { music ->
                    music?.apply {
                        Image(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            painter = painterResource(imagePath),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }

            viewState.currentMusic?.let { currentMusic ->
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 75.dp),
                    verticalArrangement = Arrangement.spacedBy(25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MusicTitleRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        title = currentMusic.title,
                        artist = currentMusic.artist,
                        artistImagePath = currentMusic.artistImagePath
                    )

                    MusicTimeProgress(
                        modifier = Modifier.fillMaxWidth(),
                        musicTime = viewState.musicPlayerOptions.currentSecond ?: 0f,
                        onTap = { position ->
                            musicService?.stopProgress()
                            musicService?.seek(position)
                        },
                        valueRange = 0f..(viewState.musicPlayerOptions.duration?.toFloat()
                            ?.takeIf { it > 0.0f } ?: 1f),
                        onDragStart = {
                            musicService?.stopProgress()
                        },
                        onDragEnd = {
                            musicService?.seek(position = it)
                        }
                    )

                    MediaPlayerControl(
                        modifier = Modifier.fillMaxWidth(),
                        pressPlayPause = {
                            viewModel.setPlaying()
                            if (viewState.musicPlayerOptions.isPlaying){
                                musicService?.pause()
                            } else {
                                musicService?.play()
                            }
                        },
                        pressShuffle = {
                            musicService?.setShuffle()
                            viewModel.toggleShuffle()
                        },
                        musicPlayerOptions = viewState.musicPlayerOptions,
                        pressNext = {
                            musicService?.skip()
                        },
                        pressPrev = {
                            musicService?.skip(true)
                        },
                        pressRepeat = {
                            musicService?.setRepeat()
                            viewModel.toggleRepeat()
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MusicScreenPreview() {
    SpotifyDemoTheme {
        MusicScreen()
    }
}