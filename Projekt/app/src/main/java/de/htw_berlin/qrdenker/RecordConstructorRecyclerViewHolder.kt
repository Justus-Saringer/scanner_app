package de.htw_berlin.qrdenker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import de.htw_berlin.qrdenker.databinding.ListItemRecordConstructorHeadingBinding
import de.htw_berlin.qrdenker.databinding.ListItemRecordConstructorImageBinding
import de.htw_berlin.qrdenker.databinding.ListItemRecordConstructorTextBinding

sealed class RecordConstructorRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    class HeadingViewHolder(private val binding: ListItemRecordConstructorHeadingBinding, listener: RecordConstructorRecyclerViewAdapter.onItemClickListener) : RecordConstructorRecyclerViewHolder(binding){
        fun bind(heading: RecordConstructorRecyclerViewItem.Heading){
            binding.sectionHeading.text = heading.heading
        }
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    class ImageViewHolder(private val binding: ListItemRecordConstructorImageBinding,
                          listener: RecordConstructorRecyclerViewAdapter.onItemClickListener) : RecordConstructorRecyclerViewHolder(binding){
        fun bind(image: RecordConstructorRecyclerViewItem.Image){
            binding.sectionImage.setImageBitmap(image.imageBmp)
        }
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    class TextViewHolder(private val binding: ListItemRecordConstructorTextBinding,
                         listener: RecordConstructorRecyclerViewAdapter.onItemClickListener) : RecordConstructorRecyclerViewHolder(binding){
        fun bind(text: RecordConstructorRecyclerViewItem.Text){
            binding.sectionText.text = text.text
        }
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}