package com.example.artistalley.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HeaderViewListAdapter
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.Constants
import com.example.artistalley.MainActivity
import com.example.artistalley.R
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_COLUMNS = "ARG_COLUMNS"
private const val RC_TAKE_PICTURE = 1
private const val RC_CHOOSE_PICTURE = 2
class HomeFragment : Fragment() {

    private lateinit var homeViewHolder: HomeViewHolder
    private var columns = 2
    private lateinit var adapter: HomeAdapter
    private var currentPhotoPath = ""
    private lateinit var rootView: RecyclerView
    private lateinit var homeView: View
    private var listener: OnThumbnailListener? = null
    lateinit var uid : String

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        arguments?.let {
            columns = it.getInt(ARG_COLUMNS)
        }
        currentPhotoPath = savedInstanceState?.getString(Constants.KEY_URL, "") ?: ""
        adapter = HomeAdapter(context!!, listener)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        rootView = inflater.inflate(R.layout.fragment_picture_grid, container, false) as RecyclerView
//        rootView.apply{
//            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//
//            adapter = adapter
//        }
        //homeView = inflater.inflate(R.layout.fragment_home, container, false)
        setAdapterWithColumns(columns)
        (activity as MainActivity).fab.setOnClickListener {
            Log.d(Constants.TAG, "Button")
            showPictureDialog()
        }
        return rootView
    }

    private fun showPictureDialog() {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose a photo source")
        builder.setMessage("Would you like to take a new picture?\nOr choose an existing one?")
        builder.setPositiveButton("Take Picture") { _, _ ->
            launchCameraIntent()
        }

        builder.setNegativeButton("Choose Picture") { _, _ ->
            launchChooseIntent()
        }
        builder.create().show()
    }
    private fun launchCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    // authority declared in manifest
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context!!,
                        "edu.rosehulman.catchandkit",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, RC_TAKE_PICTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
//            Log.d(Constants.TAG, "Here" + currentPhotoPath)
        }
    }

    private fun launchChooseIntent() {
        // https://developer.android.com/guide/topics/providers/document-provider
        val choosePictureIntent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        choosePictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
        choosePictureIntent.type = "image/*"
        if (choosePictureIntent.resolveActivity(context!!.packageManager) != null) {
            startActivityForResult(choosePictureIntent, RC_CHOOSE_PICTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_TAKE_PICTURE -> {
                    sendCameraPhotoToAdapter()
                }
                RC_CHOOSE_PICTURE -> {
                    sendGalleryPhotoToAdapter(data)
                }
            }
        }
    }

    private fun sendCameraPhotoToAdapter() {
        addPhotoToGallery()
        Log.d(Constants.TAG, "Sending to adapter this photo: $currentPhotoPath")
        adapter.add(currentPhotoPath)
    }
    private fun sendGalleryPhotoToAdapter(data: Intent?) {
        if (data != null && data.data != null) {
            val location = data.data!!.toString()
            adapter.add(location)
        }
    }

    // Works Not working on phone
    private fun addPhotoToGallery() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            activity!!.sendBroadcast(mediaScanIntent)
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnThumbnailListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnThumbnailListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constants.KEY_URL, currentPhotoPath)
    }

    fun setAdapterWithColumns(columns: Int = 3) {
        rootView.adapter = adapter
        rootView.layoutManager = GridLayoutManager(context, columns)
        rootView.setHasFixedSize(true)
    }
    companion object {
         @JvmStatic
         fun newInstance(columns: Int) =
             HomeFragment().apply {
                 arguments = Bundle().apply {
                     putInt(ARG_COLUMNS, columns)
                 }
             }
    }

    interface OnThumbnailListener {
        fun onThumbnailSelected(thumbnail: Thumbnail)
        fun onDeleteThumbnailSelected(thumbnail: Thumbnail, position: Int)
    }
}