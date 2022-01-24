package edu.uw.saksham8.photogram

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PhotoDetail (
    val url: String = "",
    val title: String = "",
    val uid: String = "",
    val likes: Map<String, Boolean> = mutableMapOf()
): Parcelable