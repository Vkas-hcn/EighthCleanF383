package com.eighth.day.lunar

import android.net.Uri


data class PhotoItem(
    val id: Long,
    val uri: Uri,
    val path: String,
    val size: Long,
    val dateAdded: Long,
    var isSelected: Boolean = false
)

data class PhotoGroup(
    val date: String,
    val photos: MutableList<PhotoItem>,
    var isAllSelected: Boolean = false
)

