package com.example.artistalley.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.AdapterViewFlipper
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.graphics.toColor
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.Constants
import com.example.artistalley.MainActivity
import com.example.artistalley.R
import com.example.artistalley.ui.gallery.Profile
import com.example.artistalley.ui.home.businessCard.BusinessAdapter
import com.example.artistalley.ui.home.businessCard.BusinessCard
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_COLUMNS = "ARG_COLUMNS"
private const val RC_TAKE_PICTURE = 1
private const val RC_CHOOSE_PICTURE = 2
private const val RC_TAKE_BUSINESS_CARD_PICTURE = 3
private const val RC_CHOOSE_BUSINESS_CARD_PICTURE = 4
private const val ARG_PROFILE = "ARG_PROFILE"
private const val ARG_UID = "ARG_UID"
class HomeFragment : Fragment() {

    private lateinit var homeViewHolder: HomeViewHolder
    private var columns = 3
    private lateinit var adapter: HomeAdapter
    private lateinit var adapterBusiness: BusinessAdapter
    private var currentPhotoPath = ""
    private lateinit var rootView: RecyclerView
    private lateinit var homeView: View
    private lateinit var businessView: AdapterViewFlipper
    private var listener: OnThumbnailListener? = null
    private var uid : String? = "r"
    private var userUIDD: String? = "user"
    private var profileSel: Profile = Profile("Empty", "Empty", "https://blind.com/wp-content/uploads/2017/08/AX_Full-logo-1280x0-cropped.png")


//    init {
//        businessRef.addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
//
//            if (exception != null) {
//            }
//            processThumbnailDiffs(snapshot!!)
//        }
//    }


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        arguments?.let {
            columns = it.getInt(ARG_COLUMNS)
            //uid = it.getString(ARG_PROFILE)
        }

        currentPhotoPath = savedInstanceState?.getString(Constants.KEY_URL, "") ?: ""
        Log.d(Constants.TAG, userUIDD)
        adapter = HomeAdapter(context!!, listener, uid!!, userUIDD!!)
        adapterBusiness = BusinessAdapter(context!!, listener, uid!!, userUIDD!!)
    }

    override fun onStop() {
        super.onStop()
        fab.hide()
    }

    override fun onStart() {
        super.onStart()
        fab.show()
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        homeView = inflater.inflate(R.layout.fragment_home,container,false)
        val name: TextView = homeView.findViewById(R.id.profile_title)
        if(profileSel.name.length >= 30){
            val shortName = profileSel.name.substring(0,27) + "..."
            Log.d(Constants.TAG,shortName)
            name.text = shortName
        }else{
            name.text = profileSel.name
        }

        rootView = homeView.findViewById(R.id.recycler_view)
        businessView = homeView.findViewById(R.id.business_card)
        //if(businessView!= null){
        businessView.setInAnimation(this.context,R.animator.left_in)
        businessView.setOutAnimation(this.context, R.animator.right_out)
        businessView.adapter = adapterBusiness
        val fab2 = homeView.findViewById<View>(R.id.fab)
        //}
        //rootView = inflater.inflate(R.layout.fragment_picture_grid, container, false) as RecyclerView
//        rootView.apply{
//            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//
//            adapter = adapter
//        }
        //homeView = inflater.inflate(R.layout.fragment_home, container, false)
        setAdapterWithColumns(columns)
        fab2.setOnClickListener {
            Log.d(Constants.TAG, "Button")
            showPictureDialog()
        }
        return homeView
    }

    private fun showPictureDialog() {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose a photo source")
        builder.setMessage("Would you like to take a new picture?\nOr choose an existing one?")
        builder.setPositiveButton("Take Picture") { _, _ ->
            launchCameraIntent(0)
        }

        builder.setNegativeButton("Choose Picture") { _, _ ->
            launchChooseIntent(0)
        }
        builder.create().show()
    }
    private fun launchCameraIntent(whichAct: Int) {
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
                        "com.example.artistalley",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    if(whichAct == 1) {
                        startActivityForResult(takePictureIntent, RC_TAKE_BUSINESS_CARD_PICTURE)
                    }else{
                        startActivityForResult(takePictureIntent, RC_TAKE_PICTURE)
                    }


                }
            }
        }
    }

    private fun launchChooseIntent(whichAct: Int) {
        // https://developer.android.com/guide/topics/providers/document-provider
        val choosePictureIntent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        choosePictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
        choosePictureIntent.type = "image/*"
        if (choosePictureIntent.resolveActivity(context!!.packageManager) != null) {
            if(whichAct == 0){
                startActivityForResult(choosePictureIntent, RC_CHOOSE_PICTURE)
            }
            else{
                startActivityForResult(choosePictureIntent, RC_CHOOSE_BUSINESS_CARD_PICTURE)

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
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        Log.d(Constants.TAG, "Not showing")
        menuInflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_logout)
        Log.d(Constants.TAG, "Business")
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose a photo source")
        builder.setMessage("Would you like to take a new picture?\nOr choose an existing one?")
        builder.setPositiveButton("Take Picture") { _, _ ->
            launchCameraIntent(1)
        }

        builder.setNegativeButton("Choose Picture") { _, _ ->
            launchChooseIntent(1)
        }
        builder.create().show()
        return super.onOptionsItemSelected(item)

//        Log.d(Constants.TAG,"BusinessCard")
//        return when(item!!.itemId){
//            R.id.add_business_card -> {
//                Log.d(Constants.TAG,"Business")
//                launchCameraIntent(1)
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_TAKE_PICTURE -> {
                    sendCameraPhotoToAdapter()
                }
                RC_TAKE_BUSINESS_CARD_PICTURE -> {
                    addPhotoToGallery()
                    adapterBusiness.add(currentPhotoPath)
                }
                RC_CHOOSE_PICTURE -> {
                    sendGalleryPhotoToAdapter(data)
                }
                RC_CHOOSE_BUSINESS_CARD_PICTURE -> {
                    if (data != null && data.data != null) {
                        val location = data.data!!.toString()
                        adapterBusiness.add(location)
                    }
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
         fun newInstance(columns: Int, profile: Profile, userUID: String) =
             HomeFragment().apply {
                 arguments = Bundle().apply {
                     putInt(ARG_COLUMNS, columns)
                     profileSel = profile
                     uid = profile.id
                     putString(ARG_PROFILE, uid)
                     userUIDD = userUID
                     putString(ARG_UID, userUIDD)
                 }
             }
    }

    interface OnThumbnailListener {
        fun onThumbnailSelected(thumbnail: Thumbnail)
        fun onDeleteThumbnailSelected(thumbnail: Thumbnail, position: Int)
        fun onBusinessSelected(businessCard: BusinessCard)
        fun onBusinessDeleted(businessCard: BusinessCard)
    }
}