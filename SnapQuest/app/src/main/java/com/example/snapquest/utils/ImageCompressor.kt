package com.example.snapquest.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {
    fun compressImage(file: File, maxSizeKB: Int = 500): File {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.path, options)

        var scale = 1
        while ((options.outWidth * options.outHeight) / (scale * scale) > 2000 * 2000) {
            scale *= 2
        }

        val bitmapOptions = BitmapFactory.Options().apply {
            inSampleSize = scale
        }
        var bitmap = BitmapFactory.decodeFile(file.path, bitmapOptions)
        bitmap = rotateImageIfRequired(bitmap, file.path)

        val outputStream = ByteArrayOutputStream()
        var quality = 90
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        while (outputStream.toByteArray().size / 1024 > maxSizeKB && quality > 10) {
            outputStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        val compressedFile = File.createTempFile("compressed_", ".jpg", file.parentFile)
        FileOutputStream(compressedFile).use { fos ->
            fos.write(outputStream.toByteArray())
            fos.flush()
        }

        return compressedFile
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, path: String): Bitmap {
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }
}