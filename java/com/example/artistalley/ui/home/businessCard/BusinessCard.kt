package com.example.artistalley.ui.home.businessCard

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BusinessCard(val url: String =""): Parcelable {
    @IgnoredOnParcel
    @get:Exclude
    var id = ""

    companion object {
        fun fromSnapshot(snapshot: DocumentSnapshot): BusinessCard {
            val businessCard = snapshot.toObject(BusinessCard::class.java)!!
            businessCard.id = snapshot.id
            return businessCard
        }
    }
}