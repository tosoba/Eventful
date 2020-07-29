package com.eventful.core.android.util.ext

import android.os.Binder
import android.os.Bundle
import androidx.core.app.BundleCompat

fun Bundle.putAny(key: String, value: Any) {
    when (value) {
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Short -> putShort(key, value)
        is Long -> putLong(key, value)
        is Byte -> putByte(key, value)
        is ByteArray -> putByteArray(key, value)
        is Char -> putChar(key, value)
        is CharArray -> putCharArray(key, value)
        is CharSequence -> putCharSequence(key, value)
        is Float -> putFloat(key, value)
        is Bundle -> putBundle(key, value)
        is Binder -> BundleCompat.putBinder(this, key, value)
        is android.os.Parcelable -> putParcelable(key, value)
        is java.io.Serializable -> putSerializable(key, value)
        else -> throw IllegalStateException("Type ${value.javaClass.canonicalName} for key: $key is not supported")
    }
}
