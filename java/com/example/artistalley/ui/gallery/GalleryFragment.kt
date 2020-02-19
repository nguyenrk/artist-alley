package com.example.artistalley.ui.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Gallery
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.Constants
import com.example.artistalley.MainActivity
import com.example.artistalley.R
import com.example.artistalley.ui.home.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*
import java.lang.RuntimeException
private const val ARG_UID = "UID"
private const val RC_CHOOSE_PICTURE = 1
private const val RC_CHOOSE_BUSINESS_CARD_PICTURE = 2
class GalleryFragment : Fragment() {
    private lateinit var galleryViewHolder: GalleryViewHolder
    private var listener: OnProfileSelectedListener? = null
    private lateinit var adapter: GalleryAdapter
    private lateinit var homeView: View
    private lateinit var rootView: RecyclerView
    private var uid: String? = null
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        galleryViewHolder =
//            ViewModelProviders.of(this).get(GalleryViewHolder::class.java)
        setHasOptionsMenu(true)
        homeView = inflater.inflate(R.layout.fragment_gallery,container,false)
        //val recyclerView = inflater.inflate(R.layout.fragment_gallery, container, false) as RecyclerView
        rootView = homeView.findViewById(R.id.recycler_view)
        adapter = GalleryAdapter(context, listener, uid!!)
        rootView.adapter = adapter
        rootView.layoutManager = LinearLayoutManager(context)


        adapter.addSnapshotListener()
        (activity as MainActivity).fab.setOnClickListener {
            adapter.showAddDialog()
        }
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                adapter.removeAt(viewHolder.adapterPosition)
            }
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val pos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                return true
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rootView)
        return homeView
    }


    override fun onAttach(context: Context){
        super.onAttach(context)

        if(context is OnProfileSelectedListener){
            listener = context
        }
        else{
            throw RuntimeException(context.toString() + "must implement OnLoginButtonPressedListener")
        }
    }
    interface OnProfileSelectedListener {
        fun onProfileSelected(profile:Profile)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        Log.d(Constants.TAG, "Not showing")
        menuInflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).fab.show()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(uid: String) =
            GalleryFragment().apply {
                arguments = Bundle().apply {

                    putString(ARG_UID, uid)
                }
            }
    }




}