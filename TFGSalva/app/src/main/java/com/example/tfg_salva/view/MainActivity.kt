package com.example.tfg_salva.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.tfg_salva.R
import com.example.tfg_salva.databinding.ActivityMainBinding
import com.example.tfg_salva.localDatabase.LocalDatabaseRepo
import com.example.tfg_salva.viewModel.CompletedMatchViewModel
import com.example.tfg_salva.viewModel.LiveMatchViewModel
import com.example.tfg_salva.viewModel.ScheduledMatchViewModel
import com.example.tfg_salva.viewModel.UsersViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var TAG: String
    lateinit var binding: ActivityMainBinding

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TAG = "MainActivity"

        askNotificationPermission()

        var refreshedToken = FirebaseMessaging.getInstance().token

        // Log.d(TAG, "Token " + refreshedToken)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(LiveMatchCardsFragment())

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.live -> replaceFragment(LiveMatchCardsFragment())
                R.id.scheduled -> replaceFragment(ScheduledMatchCardsFragment())
                R.id.completed -> replaceFragment(CompletedMatchCardsFragment())

                else -> {

                }
            }

            true
        }

        recoverAppDataFromLocalDatabase()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.log_out_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.logOut){
            var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail().build()
            var gsc = GoogleSignIn.getClient(this, gso)

            gsc.signOut()

            var intent = Intent(applicationContext, LoginActivity::class.java)

            finish()

            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout_main_activity, fragment)
        fragmentTransaction.commit()
    }

    private fun recoverAppDataFromLocalDatabase(){

        // Recover user data
        thread{
            try{
                // var userData = LocalDatabaseRepo.getUserData()
                // UsersViewModel.setUserData(LocalDatabaseRepo.getUserData())
                UsersViewModel.userDataMLD.postValue(LocalDatabaseRepo.getUserData())
                Log.d(TAG, "User data recovered from local database")
            }catch(e: Exception){
                Log.d(TAG, "No user data to be recovered from local database")
            }
        }

        // Recover scheduled matches
        thread{
            try{
                ScheduledMatchViewModel.scheduledMatches.postValue(LocalDatabaseRepo.getScheduledMatches())
                Log.d(TAG, "Scheduled matches data recovered from local database")
            }catch(e: Exception){
                Log.d(TAG, "No scheduled matches data to be recovered from local database")
            }

            try{
                LiveMatchViewModel.liveMatches.postValue(LocalDatabaseRepo.getLiveMatches())
                Log.d(TAG, "Scheduled matches data recovered from local database")
            }catch(e: Exception){
                Log.d(TAG, "No scheduled matches data to be recovered from local database")
            }

            try{
                CompletedMatchViewModel.completedMatches.postValue(LocalDatabaseRepo.getCompletedMatches())
                Log.d(TAG, "Completed matches data recovered from local database")
            }catch(e: Exception){
                Log.d(TAG, "No completed matches data to be recovered from local database")
            }
        }
    }


    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}