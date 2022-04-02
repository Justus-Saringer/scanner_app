package de.htw_berlin.qrdenker

import android.content.Context
import java.io.File

object Common {
    fun getAppPath(context:Context):String{
/*        val dir = File(android.os.Environment.getExternalStorageDirectory().toString()
        +File.separator
        +context.resources.getString(R.string.app_name)
        +File.separator)*/
        val dir = File(context.getExternalFilesDir(null).toString()
        +File.separator
        +context.resources.getString(R.string.app_name)
        +File.separator)
        if (!dir.exists())
            dir.mkdir()
        return dir.path+File.separator
    }
}