package de.htw_berlin.qrdenker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.htw_berlin.qrdenker.databinding.ListItemRecordConstructorHeadingBinding
import de.htw_berlin.qrdenker.databinding.ListItemRecordConstructorImageBinding
import de.htw_berlin.qrdenker.databinding.ListItemRecordConstructorTextBinding
import java.lang.IllegalArgumentException

class RecordConstructorRecyclerViewAdapter: RecyclerView.Adapter<RecordConstructorRecyclerViewHolder>() {

    private lateinit var _listener: onItemClickListener

    var items = mutableListOf<RecordConstructorRecyclerViewItem>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    interface onItemClickListener{

        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener)
    {
        _listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecordConstructorRecyclerViewHolder {
        return when(viewType){
            R.layout.list_item_record_constructor_heading -> RecordConstructorRecyclerViewHolder.HeadingViewHolder(
                ListItemRecordConstructorHeadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false), _listener
            )
            R.layout.list_item_record_constructor_image -> RecordConstructorRecyclerViewHolder.ImageViewHolder(
                ListItemRecordConstructorImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), _listener
            )
            R.layout.list_item_record_constructor_text -> RecordConstructorRecyclerViewHolder.TextViewHolder(
                ListItemRecordConstructorTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), _listener
            )
            else -> throw  IllegalArgumentException("Invalid ViewType provided")
        }
    }

    override fun onBindViewHolder(holder: RecordConstructorRecyclerViewHolder, position: Int) {
        when(holder){
            is RecordConstructorRecyclerViewHolder.HeadingViewHolder -> holder.bind(items[position] as RecordConstructorRecyclerViewItem.Heading)
            is RecordConstructorRecyclerViewHolder.ImageViewHolder -> holder.bind(items[position] as RecordConstructorRecyclerViewItem.Image)
            is RecordConstructorRecyclerViewHolder.TextViewHolder -> holder.bind(items[position] as RecordConstructorRecyclerViewItem.Text)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(items[position]){
            is RecordConstructorRecyclerViewItem.Heading -> R.layout.list_item_record_constructor_heading
            is RecordConstructorRecyclerViewItem.Image -> R.layout.list_item_record_constructor_image
            is RecordConstructorRecyclerViewItem.Text -> R.layout.list_item_record_constructor_text
        }
    }

    fun deleteItem(pos : Int){
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }
}