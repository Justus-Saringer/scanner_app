package de.htw_berlin.qrdenker

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import de.htw_berlin.qrdenker.firebase.model.DocumentModel

class SearchViewModel : ViewModel() {

    // parts of the recyclerview
    private lateinit var resultList : MutableList<DocumentModel>

    internal val searchRecyclerViewAdapter = SearchRecyclerViewAdapter()


    /*fun testItems()
    {
        resultList = mutableListOf(
            SearchRecyclerViewItem("Dies ist ein Titel", "Ganz viel Text"),
        SearchRecyclerViewItem("Ivan", "der Schreckliche")
        )
        searchRecyclerViewAdapter.items = resultList
        searchRecyclerViewAdapter.notifyDataSetChanged()
    }*/

    fun updateRecyclerView(items: MutableList<DocumentModel>){
        resultList = items
        searchRecyclerViewAdapter.items = resultList
        searchRecyclerViewAdapter.notifyDataSetChanged()
    }

    fun deleteItem(position: Int){
        resultList.removeAt(position)
        searchRecyclerViewAdapter.items = resultList
        searchRecyclerViewAdapter.notifyItemRemoved(position)
    }
}