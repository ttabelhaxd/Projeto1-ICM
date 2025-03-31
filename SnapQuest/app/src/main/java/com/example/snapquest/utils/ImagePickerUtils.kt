package com.example.snapquest.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImagePickerUtils {
    @Composable
    fun rememberImagePicker(
        onImageSelected: (String) -> Unit,
        onError: (String) -> Unit
    ): ImagePicker {
        val context = LocalContext.current
        var tempPhotoPath by remember { mutableStateOf<String?>(null) }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    val file = createImageFile(context)
                    FileOutputStream(file).use { out ->
                        bitmap?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    tempPhotoPath = file.absolutePath
                    onImageSelected(file.absolutePath)
                } catch (e: Exception) {
                    onError("Error loading photo: ${e.message}")
                }
            }
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && tempPhotoPath != null) {
                onImageSelected(tempPhotoPath!!)
            }
        }

        return remember {
            ImagePicker(
                launchCamera = { file ->
                    tempPhotoPath = file.absolutePath
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    cameraLauncher.launch(uri)
                },
                launchGallery = {
                    galleryLauncher.launch("image/*")
                },
                createImageFile = { createImageFile(context) }
            )
        }
    }

    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            parentFile?.mkdirs()
        }
    }
}

data class ImagePicker(
    val launchCamera: (File) -> Unit,
    val launchGallery: () -> Unit,
    val createImageFile: () -> File
)