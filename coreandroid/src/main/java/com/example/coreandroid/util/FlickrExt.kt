package com.example.coreandroid.util

import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.photos.GeoData
import com.flickr4java.flickr.photos.Photo
import com.google.android.gms.maps.model.LatLng

fun Flickr.loadPhotosUrlsForLocation(
    latLng: LatLng, numberOfPhotos: Int, size: PhotoSize
): List<String> = geoInterface.photosForLocation(
    GeoData(latLng.longitude.toString(), latLng.latitude.toString(), "11"),
    emptySet(), numberOfPhotos, 0
).map { it.imageUrl(size) }

fun Photo.imageUrl(size: PhotoSize): String =
    "https://farm$farm.staticflickr.com/$server/${id}_${secret}_${size.code}.jpg"

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
