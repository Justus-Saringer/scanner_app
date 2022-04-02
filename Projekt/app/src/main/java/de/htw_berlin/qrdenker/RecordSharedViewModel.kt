package de.htw_berlin.qrdenker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordSharedViewModel: ViewModel() {

    private var _recordItems = MutableLiveData(mutableListOf<RecordConstructorRecyclerViewItem>())
    internal val recordItems : LiveData<MutableList<RecordConstructorRecyclerViewItem>> = _recordItems

    internal var imageUri: Uri? = null
    internal var title: String = ""
    internal var id: String = ""

    internal val recordConstructorRecyclerViewAdapter = RecordConstructorRecyclerViewAdapter()
    internal lateinit var currentEditorItem: RecordConstructorRecyclerViewItem
    internal var currentEditorItemPosition: Int = recordConstructorRecyclerViewAdapter.itemCount
    internal var isNewItem = false
    internal lateinit var defaultBitmap: Bitmap


    fun openEditor(currentPosition: Int, itemTypeId : Int = 0){
        if (itemTypeId != 0){
            currentEditorItemPosition = recordConstructorRecyclerViewAdapter.itemCount
            isNewItem = true
            if (itemTypeId == 1) currentEditorItem = RecordConstructorRecyclerViewItem.Heading("")
            if (itemTypeId == 2) currentEditorItem = RecordConstructorRecyclerViewItem.Image(defaultBitmap!!)
            if (itemTypeId == 3) currentEditorItem = RecordConstructorRecyclerViewItem.Text("")
        }
        else{
                currentEditorItem = recordConstructorRecyclerViewAdapter.items[currentPosition]
                currentEditorItemPosition = currentPosition
                isNewItem = false
            }
        }


    fun addItem(){
        recordItems.value!!.add(currentEditorItem)
        recordConstructorRecyclerViewAdapter.items = recordItems.value!!
        recordConstructorRecyclerViewAdapter.notifyItemChanged(currentEditorItemPosition)
    }
    fun replaceItem()
    {
        recordItems.value!!.set(currentEditorItemPosition, currentEditorItem)
        recordConstructorRecyclerViewAdapter.notifyItemChanged(currentEditorItemPosition)
    }

    fun updateRecyclerView(items: MutableList<RecordConstructorRecyclerViewItem>){
        _recordItems.value = items
        recordConstructorRecyclerViewAdapter.items = recordItems.value!!
        recordConstructorRecyclerViewAdapter.notifyDataSetChanged()
    }

    fun clearViewModel(){
        _recordItems.value!!.clear()
        imageUri = null
        title = ""
        id =""
        recordConstructorRecyclerViewAdapter.items = _recordItems.value!!
        recordConstructorRecyclerViewAdapter.notifyDataSetChanged()

    }

}