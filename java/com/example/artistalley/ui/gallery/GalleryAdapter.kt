package com.example.artistalley.ui.gallery

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.artistalley.R
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.profile_add.view.*

class GalleryAdapter(val context: Context?, private val listener : GalleryFragment.OnProfileSelectedListener?):RecyclerView.Adapter<GalleryViewHolder>() {
    lateinit var listenerRegistration: ListenerRegistration
    var profiles = ArrayList<Profile>()
    private val profileRef = FirebaseFirestore
        .getInstance()
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
            .orderBy(Profile.LAST_TOUCHED_KEY, Query.Direction.DESCENDING)
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
    @SuppressLint("InflateParams")
    fun showAddDialog(position:Int = -1){

        dialogBuilder(position)




    }
    private fun dialogBuilder(position: Int = -1){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Add profile"
            )
        val view = LayoutInflater.from(context).inflate(R.layout.profile_add, null, false)
        builder.setView(view)
        if(position >= 0){
            view.profile_edit_name.setText(profiles[position].name)
            view.profile_edit_location.setText(profiles[position].location)
        }
        builder.setPositiveButton(android.R.string.ok){ _, _ ->
            var name = view.profile_edit_name.text.toString()
            var location = view.profile_edit_location.text.toString()

//            if(position>=0){
//                edit(position,title,url)
//            }
            //else{
                add(Profile(name, location, android.R.drawable.ic_menu_gallery))
            //}
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }

    fun removeSnapshotListener(){
        listenerRegistration.remove()
    }
    fun add(profile: Profile){
        profileRef.add(profile)
    }


}