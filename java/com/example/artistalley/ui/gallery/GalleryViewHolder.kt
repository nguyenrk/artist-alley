package com.example.artistalley.ui.gallery

import android.content.Context
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.Constants
import com.example.artistalley.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.profile_card_row.view.*

class GalleryViewHolder : RecyclerView.ViewHolder {
    lateinit var context: Context
    val profileName: TextView = itemView.profile_name
    val profilePicture: ImageView = itemView.imageView
    constructor(itemView: View, adapter: GalleryAdapter, context: Context?): super(itemView){
        this.context = context!!

        itemView.setOnClickListener{
            //adapter.showImage(adapterPosition)
            adapter.showProfile(adapterPosition)
        }
        itemView.setOnLongClickListener{
            adapter.showAddDialog(adapterPosition)
            true
        }
    }

    fun bind(profile: Profile){
        if(profile.name == "")
            profileName.text = profile.location
        else
            profileName.text = profile.name + ": " + profile.location
        if(URLUtil.isValidUrl(profile.resourceID))
            Picasso.get().load(profile.resourceID).into(profilePicture)
        else
            profilePicture.setImageResource(R.drawable.ax_logo_new)
            //Picasso.get().load("https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Anime_Expo_logo.svg/1200px-Anime_Expo_logo.svg.png").into(profilePicture)

//        Log.d(Constants.TAG, "profile drawable: "+ URLUtil.isValidUrl(profile.resourceID))
//        location.text = profile.location
    }



}