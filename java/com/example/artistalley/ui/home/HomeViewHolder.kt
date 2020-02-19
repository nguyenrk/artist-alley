package com.example.artistalley.ui.home

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.Constants
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.thumbnail_item.view.*
class HomeViewHolder (itemView: View, val context: Context, adapter: HomeAdapter):RecyclerView.ViewHolder(itemView){
    private val imageView: ImageView = itemView.thumbnail

    init {
        itemView.setOnClickListener{
            adapter.onThumbnailSelected(adapterPosition)
        }
        itemView.setOnLongClickListener{
            adapter.deleteTest(adapterPosition)
            true
        }
    }

    fun bind(thumbnail: Thumbnail) {
//        Log.d(Constants.TAG, "URL: ${thumbnail.url}")
        Picasso.get()
            .load(thumbnail.url)
            .into(imageView)
    }
}