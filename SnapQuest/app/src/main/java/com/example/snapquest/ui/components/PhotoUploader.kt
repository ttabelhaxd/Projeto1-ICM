package com.example.snapquest.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import com.example.snapquest.R
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotoUploader(
    currentPhotoPath: String,
    onPhotoSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tempPhotoPath by remember { mutableStateOf(currentPhotoPath) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(tempPhotoPath) {
        if (tempPhotoPath.isNotEmpty()) {
            delay(100)
            try {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = false
                }
                val newBitmap = BitmapFactory.decodeFile(tempPhotoPath, options)
                bitmap = newBitmap
            } catch (e: Exception) {
                Log.e("PhotoUploader", "Error loading image: ${e.message}")
                bitmap = null
            }
        } else {
            bitmap = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val newBitmap = BitmapFactory.decodeStream(inputStream)
                val file = createImageFile(context)
                FileOutputStream(file).use { out ->
                    newBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                tempPhotoPath = file.absolutePath
                onPhotoSelected(tempPhotoPath)
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.fromFile(File(tempPhotoPath)))
                val newBitmap = BitmapFactory.decodeStream(inputStream)
                val file = createImageFile(context)
                FileOutputStream(file).use { out ->
                    newBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                tempPhotoPath = file.absolutePath
                onPhotoSelected(tempPhotoPath)
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
                modifier = Modifier
                    .size(150.dp)
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
                val file = createImageFile(context).apply {
                    createNewFile()
                }
                tempPhotoPath = file.absolutePath
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                cameraLauncher.launch(uri)
            }) {
                Text("Take Photo")
            }

            Button(onClick = {
                galleryLauncher.launch("image/*")
            }) {
                Text("Choose from Gallery")
            }
        }
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    ).apply {
        parentFile?.mkdirs()
    }
}