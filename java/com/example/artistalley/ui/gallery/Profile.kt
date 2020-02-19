package com.example.artistalley.ui.gallery

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Profile (var name: String = "", var location: String = "", var resourceID: String = ""): Parcelable
{
    @get:Exclude
    var id =""
    @ServerTimestamp
    var lastTouched: Timestamp? = null

    companion object {
        const val LAST_TOUCHED_KEY = "lastTouched"
        fun fromSnapshot(snapshot: DocumentSnapshot):Profile{

            val pc = snapshot.toObject(Profile::class.java)!!
            pc.id = snapshot.id
            return pc
        }
    }
}