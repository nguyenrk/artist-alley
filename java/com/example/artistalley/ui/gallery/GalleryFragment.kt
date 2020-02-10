package com.example.artistalley.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Gallery
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.R

class GalleryFragment : Fragment() {

    private lateinit var galleryViewHolder: GalleryViewHolder
    private var listener: OnProfileSelectedListener? = null
    private lateinit var adapter: GalleryAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        galleryViewHolder =
//            ViewModelProviders.of(this).get(GalleryViewHolder::class.java)
        val recyclerView = inflater.inflate(R.layout.fragment_gallery, container, false) as RecyclerView
        adapter = GalleryAdapter(context, listener)


        return recyclerView
    }
    interface OnProfileSelectedListener {
        fun onProfileSelected(profile:Profile)
    }




}