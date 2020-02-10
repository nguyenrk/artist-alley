package com.example.artistalley.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.artistalley.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class HomeAdapter(val context: Context, private val listener: HomeFragment.OnThumbnailListener?): RecyclerView.Adapter<HomeViewHolder>(){
    lateinit var listenerRegistration: ListenerRegistration
    private val thumbnails = ArrayList<Thumbnail>()
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1
    private val thumbnailRef = FirebaseFirestore
        .getInstance()
        .collection("artwork")
    private val storageRef = FirebaseStorage
        .getInstance()
        .reference
        .child("images")
    init {
        thumbnailRef.addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->

            if (exception != null) {
            }
            processThumbnailDiffs(snapshot!!)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.thumbnail_item, parent, false)
        return HomeViewHolder(itemView, context, this)
    }

    override fun getItemCount() = thumbnails.size

    override fun onBindViewHolder(viewHolder: HomeViewHolder, position: Int) {
        viewHolder.bind(thumbnails[position])
    }

//    override fun getItemViewType(position: Int): Int {
//
//        if (isPosi)
//        //return super.getItemViewType(position)
//    }
    fun onThumbnailSelected(position: Int) {
        listener?.onThumbnailSelected(thumbnails[position])
    }

    fun add(localPath: String) {
        // Move this line of code there.
        //thumbnailRef.add(Thumbnail(localPath))
        ImageRescaleTask(localPath).execute()
    }
    inner class ImageRescaleTask(val localPath: String) : AsyncTask<Void, Void, Bitmap>() {
        override fun doInBackground(vararg p0: Void?): Bitmap? {
            // Reduces length and width by a factor (currently 2).
            val ratio = 2
            return BitmapUtils.rotateAndScaleByRatio(context, localPath, ratio)
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            // that uses Firebase storage.
            // https://firebase.google.com/docs/storage/android/upload-files
            storageAdd(localPath, bitmap)
        }

    }

    private fun storageAdd(localPath: String, bitmap: Bitmap?){
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val id = Math.abs(Random.nextLong()).toString()
        var uploadTask = storageRef.child(id).putBytes(data)

        uploadTask.continueWithTask (Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation storageRef.child(id).downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                thumbnailRef.add(Thumbnail(downloadUri.toString()))
            } else {
                // Handle failures
                // ...
            }
        }
    }


//    fun addSoloSnapshotListener(){
//        listenerRegistration = thumbnailRef
//            .whereEqualTo("uid", uid)
//            .addSnapshotListener{snapshot, firebaseFirestoreException ->
//                if(firebaseFirestoreException != null){
//                    return@addSnapshotListener
//                    //Log.w("PB", "listen error", firebaseFirestoreException)
//                }
//                else{
//                    processThumbnailDiffs(snapshot!!)
//                }
//
//            }
//    }
    private fun processThumbnailDiffs(snapshot: QuerySnapshot) {
        for (documentChange in snapshot.documentChanges) {
            val thumbnail = Thumbnail.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    if (thumbnail.url.isNotEmpty()) {
                        thumbnails.add(0, thumbnail)
                        notifyItemInserted(0)
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    val index = thumbnails.indexOfFirst { it.id == thumbnail.id }
                    thumbnails.removeAt(index)
                    notifyItemRemoved(index)
                }
                DocumentChange.Type.MODIFIED -> {
                    val index = thumbnails.indexOfFirst { it.id == thumbnail.id }
                    thumbnails[index] = thumbnail
                    notifyItemChanged(index)
                }
            }
        }
    }


}