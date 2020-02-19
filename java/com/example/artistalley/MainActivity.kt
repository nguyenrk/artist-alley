package com.example.artistalley

import SplashFragment
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.artistalley.ui.gallery.GalleryFragment
import com.example.artistalley.ui.gallery.Profile
import com.example.artistalley.ui.home.BusinessCardDetail
import com.example.artistalley.ui.home.DetailFragment
import com.example.artistalley.ui.home.HomeFragment
import com.example.artistalley.ui.home.Thumbnail
import com.example.artistalley.ui.home.businessCard.BusinessCard
import com.firebase.ui.auth.AuthUI
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity(), HomeFragment.OnThumbnailListener, GalleryFragment.OnProfileSelectedListener, SplashFragment.OnLoginButtonPressedListener {


    private val WRITE_EXTERNAL_STORAGE_PERMISSION = 2
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var uid : String
    lateinit var authListener : FirebaseAuth.AuthStateListener
    private val RC_SIGN_IN = 1
    val auth = FirebaseAuth.getInstance()
    lateinit var fab: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        fab = findViewById(R.id.fab)
        authListener = FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
            val user = auth.currentUser
            if(user==null){
                Log.d(Constants.TAG, "Login")
                fab.hide()
                switchToSplashFragment()
            } else{
                //Log.d(Constants.TAG, user.uid)
                fab.show()
                Log.d(Constants.TAG, "Open Home Page")
                openHomePageApp(user.uid)

            }
        }
        checkPermissions()
//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }

    }
    private fun switchToSplashFragment(){
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, SplashFragment())
        ft.commit()
    }

    private fun checkPermissions() {
        // Check to see if we already have permissions
        if (ContextCompat
                .checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If we do not, request them from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

//    private fun signInAnonymously() {
//        auth.signInAnonymously().addOnSuccessListener(this) {
//            openHomePageApp()
//
//        }.addOnFailureListener(this) { e ->
//            Log.e(Constants.TAG, "signInAnonymously:FAILURE", e)
//        }
//    }

    override fun onLoginButtonPressed() {
        launchLoginUI()
    }

    private fun openHomePageApp(uid: String){
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, GalleryFragment.newInstance(uid))
        ft.addToBackStack("List")
        ft.commit()
//        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
//        val navView: NavigationView = findViewById(R.id.nav_view)
//        val navController = findNavController(R.id.nav_host_fragment)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//
//
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
//                R.id.nav_tools, R.id.nav_share, R.id.nav_send
//            ), drawerLayout
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }
    override fun onStart(){
        super.onStart()
        auth.addAuthStateListener(authListener)
//        val user = auth.currentUser
//        if (user != null) {
//            openHomePageApp(user.uid)
//        } else {
//            switchToSplashFragment()
//        }
    }
    override fun onStop(){
        super.onStop()
        auth.removeAuthStateListener(authListener)
    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Log.d(Constants.TAG, "Permission granted")
                } else {
                    // permission denied
                }
                return
            }
        }
    }

    private fun launchLoginUI(){
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()

        )
        val loginIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(android.R.drawable.ic_menu_gallery)
            .build()
        startActivityForResult(
            loginIntent,
            RC_SIGN_IN
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBusinessDeleted(businessCard: BusinessCard) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBusinessSelected(businessCard: BusinessCard) {
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out)
        ft.replace(R.id.fragment_container, BusinessCardDetail.newBusinessInstance(businessCard))
        Log.d(Constants.TAG, "Business Open")
        ft.addToBackStack("Business")
        ft.commit()
    }

    override fun onProfileSelected(profile: Profile) {
//        Log.d(Constants.TAG, profile.id)
        val ft = supportFragmentManager.beginTransaction()
        val orientation = resources.configuration.orientation
        val columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3
        val user = auth.currentUser
        fab.hide()
        ft.replace(R.id.fragment_container, HomeFragment.newInstance(columns, profile, user!!.uid))
        ft.addToBackStack("Home")
        ft.commit()
    }

    override fun onThumbnailSelected(thumbnail: Thumbnail) {
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out)
        ft.replace(R.id.fragment_container, DetailFragment.newInstance(thumbnail))
        ft.addToBackStack("List")
        ft.commit()
    }

    override fun onDeleteThumbnailSelected(thumbnail: Thumbnail, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
//    override fun onThumbnailSelected(thumbnail: Thumbnail) {
//        val ft = supportFragmentManager.beginTransaction()
//        ft.replace(R.id.fragment_container, ThumbnailDetailFragment.newInstance(thumbnail))
//        ft.addToBackStack("List")
//        ft.commit()
//    }
}
