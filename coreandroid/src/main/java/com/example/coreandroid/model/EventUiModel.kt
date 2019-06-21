package com.example.coreandroid.model

import android.location.Location
import android.os.Parcelable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import com.example.coreandroid.util.ObservableStringFieldParceler
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.WriteWith

@Parcelize
data class EventUiModel(
    val category: String,
    val country: String,
    val description: String,
    val duration: Int,
    val end: String,
    val firstSeen: String,
    val id: String,
    val labels: List<String>,
    val localRank: Int,
    val location: List<Double>,
    val rank: Int,
    val relevance: Double,
    val scope: String,
    val start: String,
    val state: String,
    val timezone: String?,
    val title: String,
    val updated: String,
    val address: @WriteWith<ObservableStringFieldParceler>() ObservableField<String> = ObservableField(),
    val photoUrls: ObservableArrayList<String> = ObservableArrayList()
) : Parcelable {
    val latLng: LatLng? get() = if (location.size == 2) LatLng(location[1], location[0]) else null

    val androidLocation: Location?
        get() = if (location.size == 2) Location("").apply {
            latitude = location[1]
            longitude = location[0]
        } else null

    @IgnoredOnParcel
    var onPhotoUrlsAdded: (() -> Unit)? = null

    init {
        photoUrls.addOnListChangedCallback(object :
            ObservableList.OnListChangedCallback<ObservableArrayList<String>>() {
            override fun onChanged(sender: ObservableArrayList<String>?) {
            }

            override fun onItemRangeRemoved(sender: ObservableArrayList<String>?, positionStart: Int, itemCount: Int) {
            }

            override fun onItemRangeMoved(
                sender: ObservableArrayList<String>?,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {
            }

            override fun onItemRangeInserted(sender: ObservableArrayList<String>?, positionStart: Int, itemCount: Int) {
                onPhotoUrlsAdded?.invoke()
            }

            override fun onItemRangeChanged(sender: ObservableArrayList<String>?, positionStart: Int, itemCount: Int) {
            }
        })
    }
}