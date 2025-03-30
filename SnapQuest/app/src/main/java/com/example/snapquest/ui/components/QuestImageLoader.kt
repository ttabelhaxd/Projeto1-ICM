package com.example.snapquest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.res.painterResource
import com.example.snapquest.R

@Composable
fun QuestImageLoader(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "Quest image"
) {
    val painter = if (!imageUrl.isNullOrEmpty()) {
        rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build()
        )
    } else {
        painterResource(R.drawable.placeholder_quest)
    }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentScale = ContentScale.Crop
    )
}