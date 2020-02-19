package com.example.artistalley.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.R
import com.example.artistalley.ui.home.businessCard.BusinessCard
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_thumbnail_detail.view.*

private const val ARG_THUMBNAIL = "ARG_THUMBNAIL"

class DetailFragment : Fragment() {
    private var thumbnail: Thumbnail? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            thumbnail = it.getParcelable(ARG_THUMBNAIL)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(thumbnail: Thumbnail) =
            DetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_THUMBNAIL, thumbnail)
                }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_thumbnail_detail, container, false)
        Picasso.get()
            .load(thumbnail!!.url)
            .into(view.detail_image_view)
        return view
    }







}
