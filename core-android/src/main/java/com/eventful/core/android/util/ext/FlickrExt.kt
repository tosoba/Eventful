package com.eventful.core.android.util.ext

import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.photos.Photo
import com.flickr4java.flickr.photos.PhotoList
import com.flickr4java.flickr.photos.SearchParameters
import com.google.android.gms.maps.model.LatLng

fun Flickr.loadPhotosUrlsForLocation(
    latLng: LatLng,
    searchText: String,
    numberOfPhotos: Int,
    size: PhotoSize
): List<String> {
    fun searchParametersWith(accuracy: Int, searchText: String? = null) = SearchParameters().apply {
        latitude = latLng.latitude.toString()
        longitude = latLng.longitude.toString()
        this.accuracy = accuracy
        searchText?.let { text = it }
    }

    fun getPhotosUrlsUsing(params: SearchParameters) = photosInterface.search(params, numberOfPhotos, 0)
        .imagesUrls(size)

    val photosStreetText = getPhotosUrlsUsing(searchParametersWith(Flickr.ACCURACY_STREET, searchText))
    if (photosStreetText.isNotEmpty()) return photosStreetText

    val photosCityText = getPhotosUrlsUsing(searchParametersWith(Flickr.ACCURACY_CITY, searchText))
    if (photosCityText.isNotEmpty()) return photosCityText

    val photosStreet = getPhotosUrlsUsing(searchParametersWith(Flickr.ACCURACY_STREET))
    if (photosStreet.isNotEmpty()) return photosStreet

    return getPhotosUrlsUsing(searchParametersWith(Flickr.ACCURACY_CITY))
}

fun Photo.imageUrl(size: PhotoSize): String =
    "https://farm$farm.staticflickr.com/$server/${id}_${secret}_${size.code}.jpg"

private fun PhotoList<Photo>.imagesUrls(size: PhotoSize): List<String> = map { it.imageUrl(size) }

enum class PhotoSize(val code: String) {
    SQUARE_75_75("s"),
    SQUARE_150_150("q"),
    THUMBNAIL_100("t"),
    SMALL_240("m"),
    SMALL_320("n"),
    MEDIUM_500("-"),
    MEDIUM_640("z"),
    MEDIUM_800("c"),
    LARGE_1024("b"),
    LARGE_1600("h"),
    LARGE_2048("k")
}
