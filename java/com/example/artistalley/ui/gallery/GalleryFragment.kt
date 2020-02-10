package com.example.artistalley.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Gallery
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.Constants
import com.example.artistalley.MainActivity
import com.example.artistalley.R
import kotlinx.android.synthetic.main.fragment_home.*

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
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter.addSnapshotListener()
        (activity as MainActivity).fab.setOnClickListener {
            Log.d(Constants.TAG, "Button")
            adapter.showAddDialog()
        }
        return recyclerView
    }
    interface OnProfileSelectedListener {
        fun onProfileSelected(profile:Profile)
    }




}