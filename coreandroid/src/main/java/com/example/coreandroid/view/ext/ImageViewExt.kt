package com.example.coreandroid.view.ext

import android.graphics.Bitmap
import android.widget.ImageView
import com.example.coreandroid.util.ext.bitmap

val ImageView.dominantColor: Int
    get() {
        val newBitmap = Bitmap.createScaledBitmap(drawable.bitmap, 1, 1, true)
        val color = newBitmap.getPixel(0, 0)
        newBitmap.recycle()
        return color
    }
