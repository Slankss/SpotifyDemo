@file:OptIn(ExperimentalMaterial3Api::class)

package com.okankkl.spotifydemo.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okankkl.spotifydemo.R
import com.okankkl.spotifydemo.model.Music
import com.okankkl.spotifydemo.presentation.components.MarqueeText
import kotlinx.coroutines.launch

@Composable
fun MusicQueueSheet(
    sheetState: SheetState,
    musicList: List<Music>,
    currentMusic: Music?,
    onSelect: (Music) -> Unit
) {
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
            }

        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Text(
                    modifier = Modifier.padding(bottom = 10.dp),
                    text = "Queue",
                    fontWeight = FontWeight.W500,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(musicList){ music ->
                MusicRow(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .combinedClickable(
                            onClick = {
                                onSelect(music)
                            },
                            onLongClick = {
                            }
                        ),
                    music = music,
                    isPlaying = music.id == currentMusic?.id
                )
            }
        }
    }
}

@Composable
private fun MusicRow(
    modifier: Modifier = Modifier,
    music: Music,
    isPlaying: Boolean
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .size(50.dp),
            painter = painterResource(music.artistImagePath),
            contentDescription = null,
            contentScale = ContentScale.FillHeight
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row{
                MarqueeText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = music.title,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400,
                        color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            Text(
                text = music.artist,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_sort),
            contentDescription = null
        )
    }
}