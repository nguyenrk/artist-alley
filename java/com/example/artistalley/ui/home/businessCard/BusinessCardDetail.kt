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

private const val ARG_BUSINESS = "ARG_BUSINESS"

class BusinessCardDetail : Fragment() {
    private var businessCard: BusinessCard? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            businessCard = it.getParcelable(ARG_BUSINESS)
        }
    }

    companion object {
        @JvmStatic

        fun newBusinessInstance(businessCard: BusinessCard) =
            BusinessCardDetail().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_BUSINESS, businessCard)
                }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_business_card_detail, container, false)
        Picasso.get()
            .load(businessCard!!.url)
            .into(view.detail_image_view)
        return view
    }







}
