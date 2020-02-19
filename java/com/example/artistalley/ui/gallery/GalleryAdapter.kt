package com.example.artistalley.ui.gallery

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Spinner
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.Constants
import com.example.artistalley.R
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.profile_add.view.*

class GalleryAdapter(val context: Context?, private val listener : GalleryFragment.OnProfileSelectedListener?, uid: String):RecyclerView.Adapter<GalleryViewHolder>() {
    lateinit var listenerRegistration: ListenerRegistration
    var profiles = ArrayList<Profile>()
    private val profileRef = FirebaseFirestore
        .getInstance()
        .collection("users")
        .document(uid)
        .collection("artists")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.profile_card_row, parent, false)

        return GalleryViewHolder(view, this, context)
    }

    override fun getItemCount() = profiles.size

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(profiles[position])
    }
    fun addSnapshotListener(){
        listenerRegistration = profileRef
            .orderBy("location", Query.Direction.DESCENDING)
            .addSnapshotListener{snapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    return@addSnapshotListener
                    //Log.w("PB", "listen error", firebaseFirestoreException)
                }
                else{
                    processSnapshot(snapshot!!)
                }

            }
    }

    private fun processSnapshot(snapshot: QuerySnapshot){
        for (documentChange in snapshot.documentChanges){
            val pic = Profile.fromSnapshot(documentChange.document)
            when(documentChange.type){
                DocumentChange.Type.ADDED -> {
                    profiles.add(0,pic)
                    notifyItemInserted(0)
                }
                DocumentChange.Type.REMOVED -> {
                    val pos = profiles.indexOfFirst { it.id == pic.id }
                    profiles.removeAt(pos)
                    notifyItemRemoved(pos)
                }
                DocumentChange.Type.MODIFIED -> {
                    val pos = profiles.indexOfFirst { it.id == pic.id }
                    profiles[pos] = pic
                    notifyItemChanged(pos)
                }
            }
        }
    }
    fun removeAt(position: Int){
        profileRef.document(profiles[position].id).delete()

    }
    @SuppressLint("InflateParams")
    fun showAddDialog(position:Int = -1){

        dialogBuilder(position)
    }

    private fun dialogBuilder(position: Int = -1){
        val builder = AlertDialog.Builder(context)


        val view = LayoutInflater.from(context).inflate(R.layout.profile_add, null, false)
        builder.setView(view)
        if(position >= 0){
            builder.setTitle("Edit profile")
            view.profile_edit_name.setText(profiles[position].name)
            view.profile_edit_location.setText(profiles[position].location)
            view.image_profile_link.setText(profiles[position].resourceID)
//            builder.setNeutralButton("Set Profile Picture") { _, _ ->
//
//            }
            //view.image_profile_link.setText(profiles[position].resourceID)
            builder.setPositiveButton(android.R.string.ok){ _, _ ->
                var name = view.profile_edit_name.text.toString()
                var location = view.profile_edit_location.text.toString()
                var profileURL = view.image_profile_link.text.toString()
//            if(position>=0){
//                edit(position,title,url)
//            }
                //else{
                edit(position, name, location, profileURL)
                //}
            }
        }
        else {
            builder.setTitle("Add profile")
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                var name = view.profile_edit_name.text.toString()
                var location = view.profile_edit_location.text.toString()
                var profileURL = view.image_profile_link.text.toString()
//            if(position>=0){
//                edit(position,title,url)
//            }
                //else{
                add(Profile(name, location, profileURL))
                //}
            }
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }
//    private fun launchChooseIntent() {
//        // https://developer.android.com/guide/topics/providers/document-provider
//        val choosePictureIntent = Intent(
//            Intent.ACTION_OPEN_DOCUMENT,
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        )
//        choosePictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
//        choosePictureIntent.type = "image/*"
//        if (choosePictureIntent.resolveActivity(context!!.packageManager) != null) {
//
//            setProfileImage(choosePictureIntent)
//
//        }
//    }
//
//    fun setProfileImage(localPath: String) {
//        // TODO: You'll want to wait to add this to Firestore until after you have a Storage download URL.
//        // Move this line of code there.
//        //thumbnailRef.add(Thumbnail(localPath))
//        ImageRescaleTask(localPath).execute()
//    }

    fun showProfile(adapterPosition: Int){
        val profile = profiles[adapterPosition]
//        Log.d(Constants.TAG, profile.id)
        listener?.onProfileSelected(profile)
        //listener?.onPicSelected(pic)

    }

    fun removeSnapshotListener(){
        listenerRegistration.remove()
    }
    fun add(profile: Profile){
        profileRef.add(profile)
    }

    fun edit(position: Int, name:String, location: String, url: String){
        profiles[position].name = name
        profiles[position].location = location
        profiles[position].resourceID = url
//        Log.d(Constants.TAG, "edit profile: "+ profiles[position].id)
        profileRef.document(profiles[position].id).set(profiles[position])
    }


}