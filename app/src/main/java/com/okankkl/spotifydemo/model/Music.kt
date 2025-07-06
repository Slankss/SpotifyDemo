package com.okankkl.spotifydemo.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Music(
    var id: Int,
    var title: String,
    var artist: String,
    var imagePath: Int,
    var musicPath: Int,
    var artistImagePath: Int
): Parcelable