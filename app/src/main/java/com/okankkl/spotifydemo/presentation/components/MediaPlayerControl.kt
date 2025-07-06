package com.okankkl.spotifydemo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.okankkl.spotifydemo.R
import com.okankkl.spotifydemo.presentation.MusicPlayerOptions
import com.okankkl.spotifydemo.presentation.ui.theme.LocalIconSize
import com.okankkl.spotifydemo.presentation.ui.theme.LocalPaddingSize

@Composable
fun MediaPlayerControl(
    modifier: Modifier = Modifier,
    musicPlayerOptions: MusicPlayerOptions? = null,
    pressShuffle: () -> Unit = {},
    pressNext: () -> Unit = {},
    pressPrev: () -> Unit = {},
    pressPlayPause: () -> Unit = {},
    pressRepeat: () -> Unit = {}
) {
    val localIconSize = LocalIconSize.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        ControllerButton(
            painter = painterResource(R.drawable.ic_shuffle),
            onClick = pressShuffle,
            iconSize = localIconSize.small,
            tint = if (musicPlayerOptions?.shuffle == true){
                MaterialTheme.colorScheme.primary
            } else MaterialTheme.colorScheme.onBackground
        )

        ControllerButton(
            painter = painterResource(R.drawable.ic_skip_prev),
            onClick = pressPrev,
            iconSize = localIconSize.extraLarge
        )

        ControllerButton(
            backgroundColor = Color.White,
            painter = painterResource(if (musicPlayerOptions?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play),
            onClick = pressPlayPause,
            iconSize = localIconSize.large,
            tint = Color.Black
        )

        ControllerButton(
            painter = painterResource(R.drawable.ic_skip_next),
            onClick = pressNext,
            iconSize = localIconSize.extraLarge
        )

        ControllerButton(
            painter = painterResource(R.drawable.ic_repeat),
            onClick = pressRepeat,
            iconSize = localIconSize.small,
            tint = if (musicPlayerOptions?.repeat == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ControllerButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    painter: Painter,
    backgroundColor: Color = Color.Transparent,
    iconSize: Dp = LocalIconSize.current.default,
    tint: Color = Color.White,
) {
    val localPaddingSize = LocalPaddingSize.current
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(role = Role.Button){
                onClick()
            },
        contentAlignment = Alignment.Center,
    ){
        Icon(
            modifier = Modifier
                .padding(localPaddingSize.small)
                .size(iconSize),
            painter  = painter,
            contentDescription = null,
            tint = tint
        )
    }
}