package com.example.runningapp.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

/**
 * Room에서 기본형 이외의 자료를 저장하기 위해서는
 * TypeConverter를 사용해야 한다.
 * https://developer.android.com/training/data-storage/room/referencing-data?hl=ko
 */
class Converters {

    // ByteArray -> Bitmap 변환
    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    // Bitmap -> ByteArray 변환
    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}