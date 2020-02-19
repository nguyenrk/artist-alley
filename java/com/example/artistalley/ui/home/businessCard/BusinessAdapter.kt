package com.example.artistalley.ui.home.businessCard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.artistalley.Constants
import com.example.artistalley.R
import com.example.artistalley.ui.home.BitmapUtils
import com.example.artistalley.ui.home.HomeFragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class BusinessAdapter(val context: Context, private val listener: HomeFragment.OnThumbnailListener?, private val profileIdVal: String, uid: String):
    BaseAdapter() {
    private val storageRef = FirebaseStorage
        .getInstance()
        .reference
        .child(uid)
        .child(profileIdVal)
        .child("businessCard")
    private val businessRef = FirebaseFirestore
        .getInstance()
        .collection("users")
        .document(uid)
        .collection("artists")
        .document(profileIdVal)
        .collection("businessCard")
    private val businessCards = ArrayList<BusinessCard>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    init {
        businessRef.addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->

            if (exception != null) {
            }
            processThumbnailDiffs(snapshot!!)
        }
    }
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val convertView = inflater.inflate(R.layout.business_card_item, parent, false)
        val imageView : ImageView = convertView.findViewById(R.id.business_image)
        imageView.setOnClickListener{
            listener?.onBusinessSelected(businessCards[position])
        }
        imageView.setOnLongClickListener{
            //removeAt(position)
            if(businessCards.size > 1){
                deleteTest(position)
            }

//            listener?.onBusinessDeleted(businessCards[position])
            true
        }
        Log.d(Constants.TAG, "Getting image: ${businessCards[position].url}")
        Picasso.get().load(businessCards[position].url).into(imageView)
        //imageView.setImageResource(R.drawable.ic_menu_camera)
        return convertView
    }

    fun removeAt(position: Int){ businessRef.document(
        businessCards[position].id).delete()
        notifyDataSetChanged()
    }
    fun deleteTest(position: Int){
        var storage = FirebaseStorage.getInstance()
        //val deleteRef = storageRef.child("images/"+thumbnails[position].url.substring(87, 106))
        val deleteRef = storage.getReferenceFromUrl(businessCards[position].url)

//        Log.d(Constants.TAG, )
        deleteRef.delete().addOnSuccessListener {
            businessRef.document(businessCards[position].id).delete()
            Log.d(Constants.TAG, "DELETED")
        }.addOnFailureListener{
            Log.d(Constants.TAG, "NOT DELETED")
        }
    }

    override fun getItem(position: Int) {

        TODO("NOT IMPLEMENTED")
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return businessCards.size
    }


    fun add(localPath: String) {
        // Move this line of code there.
        //thumbnailRef.add(Thumbnail(localPath))
        ImageRescaleTask(localPath).execute()
    }

    inner class ImageRescaleTask(val localPath: String) : AsyncTask<Void, Void, Bitmap>() {
        override fun doInBackground(vararg p0: Void?): Bitmap? {
            // Reduces length and width by a factor (currently 2).
            val ratio = 1
            return BitmapUtils.rotateAndScaleByRatio(context, localPath, ratio)
        }

        override fun onPostExecute(bitmap: Bitmap?) {
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
                businessRef.add(
                    BusinessCard(
                        downloadUri.toString()
                    )
                )
            } else {
                // Handle failures
                // ...
            }
        }
    }
    private fun processThumbnailDiffs(snapshot: QuerySnapshot) {
        for (documentChange in snapshot.documentChanges) {
            val business = BusinessCard.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    if (business.url.isNotEmpty()) {
                        businessCards.add(0, business)
                        notifyDataSetChanged()
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    val index = businessCards.indexOfFirst { it.id == business.id }
                    businessCards.removeAt(index)
                    notifyDataSetChanged()
                }
                DocumentChange.Type.MODIFIED -> {
                    val index = businessCards.indexOfFirst { it.id == business.id }
                    businessCards[index] = business
                    notifyDataSetChanged()
                }
            }
        }
    }
}