package de.htw_berlin.qrdenker

import android.graphics.Bitmap
import android.net.Uri


sealed class RecordConstructorRecyclerViewItem {

    data class Heading(
        val heading : String
    )   : RecordConstructorRecyclerViewItem()

    data class Image(
        var imageBmp : Bitmap?,
        val imageName: String = "",
        var imageUrl: String = "",
        var uri: Uri = Uri.EMPTY
    )   : RecordConstructorRecyclerViewItem()

    data class Text(
        val text : String
    )   : RecordConstructorRecyclerViewItem()
}