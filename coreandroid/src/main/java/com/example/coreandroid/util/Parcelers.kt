package com.example.coreandroid.util

import android.os.Parcel
import androidx.databinding.ObservableField
import kotlinx.android.parcel.Parceler

object ObservableStringFieldParceler : Parceler<ObservableField<String>> {
    override fun ObservableField<String>.write(parcel: Parcel, flags: Int) {
        parcel.writeString(get())
    }

    override fun create(parcel: Parcel) = ObservableField(parcel.readString())
}