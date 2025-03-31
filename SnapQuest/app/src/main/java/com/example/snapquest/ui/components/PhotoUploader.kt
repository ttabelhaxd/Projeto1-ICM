package com.example.snapquest.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.snapquest.R
import com.example.snapquest.utils.ImagePickerUtils
import kotlinx.coroutines.delay

@Composable
fun PhotoUploader(
    currentPhotoPath: String,
    onPhotoSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val imagePicker = ImagePickerUtils.rememberImagePicker(
        onImageSelected = { path ->
            onPhotoSelected(path)
        },
        onError = { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    )

    LaunchedEffect(currentPhotoPath) {
        if (currentPhotoPath.isNotEmpty()) {
            delay(100)
            try {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = false
                }
                bitmap = BitmapFactory.decodeFile(currentPhotoPath, options)
            } catch (e: Exception) {
                bitmap = null
            }
        } else {
            bitmap = null
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Selected photo",
                modifier = Modifier.size(150.dp)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.placeholder_select_photo),
                contentDescription = "Placeholder",
                modifier = Modifier.size(150.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val file = imagePicker.createImageFile()
                imagePicker.launchCamera(file)
            }) {
                Text("Take Photo")
            }

            Button(onClick = {
                imagePicker.launchGallery()
            }) {
                Text("Choose from Gallery")
            }
        }
    }
}