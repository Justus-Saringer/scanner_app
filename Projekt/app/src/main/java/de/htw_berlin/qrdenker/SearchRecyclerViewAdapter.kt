package de.htw_berlin.qrdenker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.htw_berlin.qrdenker.firebase.model.DocumentModel


class SearchRecyclerViewAdapter() :
    RecyclerView.Adapter<SearchRecyclerViewAdapter.SearchRecyclerViewHolder>() {

    private lateinit var itemClickListener : onItemClickListener

    interface onItemClickListener
    {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener : onItemClickListener)
    {
        itemClickListener = listener
    }

    var items = mutableListOf<DocumentModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_search_result, parent, false)
        return SearchRecyclerViewHolder(itemView, itemClickListener)
    }

    override fun onBindViewHolder(holder: SearchRecyclerViewHolder, position: Int) {
        val currentItem = items[position]
        holder.resultHeading.text = currentItem.title
        holder.resultText.text = currentItem.id
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun deleteItem(pos : Int){
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    class SearchRecyclerViewHolder(itemView : View, listener : onItemClickListener) : RecyclerView.ViewHolder(itemView)
    {
        val resultHeading : TextView = itemView.findViewById(R.id.search_result_heading)
        val resultText : TextView = itemView.findViewById(R.id.search_result_text)

        init
        {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}
