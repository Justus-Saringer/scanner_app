package de.htw_berlin.qrdenker

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class PrintViewModel: ViewModel() {
    val qrCodeList = mutableListOf<Pair<Bitmap, String>>()

    fun generateQRCodes(idList: List<Pair<String, String>>){
        val writer = QRCodeWriter()
        for (pair in idList){
            try {
                val bitMatrix = writer.encode(pair.first, BarcodeFormat.QR_CODE,512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width){
                    for (y in 0 until height){
                        bmp.setPixel(x,y, if(bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                    }
                }
                qrCodeList.add(Pair(bmp, pair.second))
            }
            catch (e: WriterException){
                e.printStackTrace()
            }
        }
    }

}