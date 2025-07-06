package com.okankkl.spotifydemo.data

import com.okankkl.spotifydemo.R
import com.okankkl.spotifydemo.model.Music

class MusicData {
    companion object {
        val musicData = listOf(
            Music(
                id = 1,
                title = "Astronaut in the ocean",
                artist = "Masked Wolf",
                artistImagePath = R.drawable.image_masked_wolf,
                imagePath = R.drawable.image_astronaut_in_the_ocean,
                musicPath = R.raw.music_astronot_in_the_ocean
            ),
            Music(
                id = 2,
                title = "NSYNC - Bye Bye Bye",
                artist = "NSYNC",
                artistImagePath = R.drawable.image_nsync,
                imagePath = R.drawable.image_bye_bye,
                musicPath = R.raw.music_bye_bye
            ),
            Music(
                id = 3,
                title = "Stroma - Papaoutai",
                artist = "Stromae",
                artistImagePath = R.drawable.image_stromae,
                imagePath = R.drawable.image_papaoutaio,
                musicPath = R.raw.music_stromea_papaoutai
            ),
            Music(
                id = 4,
                title = "Blinding Lights",
                artist = "The Weeknd",
                artistImagePath = R.drawable.image_the_weeknd,
                imagePath = R.drawable.image_blinding_lights,
                musicPath = R.raw.music_blinding_lights
            ),
            Music(
                id = 5,
                title = "Happy",
                artist = "Pharrell Williams",
                artistImagePath = R.drawable.image_pharrell_williams,
                imagePath = R.drawable.image_happy,
                musicPath = R.raw.music_happy
            ),
            Music(
                id = 6,
                title = "Levitating",
                artist = "Dua Lipa",
                artistImagePath = R.drawable.image_dua_lipa,
                imagePath = R.drawable.image_levitating,
                musicPath = R.raw.music_levitating
            ),
            Music(
                id = 7,
                title = "Dance Monkey",
                artist = "Tones and I",
                artistImagePath = R.drawable.image_tones_and_i,
                imagePath = R.drawable.image_dance_monkey,
                musicPath = R.raw.music_dance_monkey
            ),
            Music(
                id = 8,
                title = "Enemy (from the series Arcane League of Legends)",
                artist = "Imagine Dragons",
                artistImagePath = R.drawable.image_imagine_dragons,
                imagePath = R.drawable.image_enemy,
                musicPath = R.raw.music_enemy
            ),
            Music(
                id = 9,
                title = "Counting Stars",
                artist = "One Republic",
                artistImagePath = R.drawable.image_one_republic,
                imagePath = R.drawable.image_counting_stars,
                musicPath = R.raw.music_counting_starts
            )
        )
    }
}