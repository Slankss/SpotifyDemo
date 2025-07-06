package com.okankkl.spotifydemo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okankkl.spotifydemo.data.MusicData
import com.okankkl.spotifydemo.model.Music
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicScreenViewModel: ViewModel() {

    private var _viewState = MutableStateFlow(MusicScreenViewState())
    var viewState = _viewState.asStateFlow()

    private val channel = Channel<MusicScreenViewEvent>(Channel.BUFFERED)
    val channelFlow = channel.receiveAsFlow()

    init {
        getMusicList()
    }

    fun observeProgress(channel: ReceiveChannel<Float>) {
        viewModelScope.launch {
            channel.consumeEach { position ->
                _viewState.update { viewState ->
                    viewState.copy(
                        musicPlayerOptions = viewState.musicPlayerOptions.copy(
                            currentSecond = position
                        )
                    )
                }
            }
        }
    }

    fun getMusicList() {
        viewModelScope.launch {
            val musicList = MusicData.musicData
            _viewState.update { it.copy(currentMusic = musicList.first()) }
            channel.send(MusicScreenViewEvent.GetMusicList(musicList))
        }
    }

    fun setMusic(music: Music?){
        music?.let {
            _viewState.update { it.copy( currentMusic = music) }
        }
    }

    fun toggleShuffle(){
        _viewState.update { viewState ->
            viewState.copy(
                musicPlayerOptions = viewState.musicPlayerOptions.copy(
                    shuffle = !(viewState.musicPlayerOptions.shuffle)
                )
            )
        }
    }

    fun setPlaying(isPlaying: Boolean? = null){
        _viewState.update { viewState ->
            viewState.copy(
                musicPlayerOptions = viewState.musicPlayerOptions.copy(
                    isPlaying = isPlaying ?: !viewState.musicPlayerOptions.isPlaying
                )
            )
        }
    }

    fun toggleRepeat(){
        _viewState.update { viewState ->
            viewState.copy(
                musicPlayerOptions = viewState.musicPlayerOptions.copy(
                    repeat = !(viewState.musicPlayerOptions.repeat)
                )
            )
        }
    }

    fun setDuration(duration: Int){
        _viewState.update { viewState ->
            viewState.copy(
                musicPlayerOptions = viewState.musicPlayerOptions.copy(
                    duration = duration,
                    currentSecond = 0f
                )
            )
        }
    }
}

data class MusicScreenViewState(
    val currentMusic: Music? = null,
    val musicPlayerOptions: MusicPlayerOptions = MusicPlayerOptions()
)

sealed class MusicScreenViewEvent {
    data object RestartMusic: MusicScreenViewEvent()
    data class GetMusicList(val musicList: List<Music>): MusicScreenViewEvent()
}

data class MusicPlayerOptions(
    var shuffle: Boolean = false,
    var isPlaying: Boolean = false,
    var currentSecond: Float? = null,
    var duration: Int? = null,
    val repeat: Boolean = false
)