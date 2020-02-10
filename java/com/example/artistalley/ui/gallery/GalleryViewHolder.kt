package com.example.artistalley.ui.gallery

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.profile_card_row.view.*

class GalleryViewHolder : RecyclerView.ViewHolder {
    lateinit var context: Context
    val profileName: TextView = itemView.profile_name
    val location: TextView = itemView.profile_location
    val profilePicture: ImageView = itemView.imageView
    constructor(itemView: View, adapter: GalleryAdapter, context: Context?): super(itemView){
        this.context = context!!

        itemView.setOnClickListener{
            //adapter.showImage(adapterPosition)
        }
        itemView.setOnLongClickListener{
            //adapter.showAddDialog(adapterPosition)
            true
        }
    }

    fun bind(profile: Profile){
        profileName.text = profile.name
        location.text = profile.location
    }



}