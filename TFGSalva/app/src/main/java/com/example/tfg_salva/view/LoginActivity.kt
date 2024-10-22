package com.example.tfg_salva.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tfg_salva.MyApplication
import com.example.tfg_salva.R
import com.example.tfg_salva.viewModel.UsersViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    val TAG: String = "LoginActivity"
    lateinit var signInButtonGoogle: com.google.android.gms.common.SignInButton
    lateinit var gso: GoogleSignInOptions
    lateinit var gsc: GoogleSignInClient
    private val REQ_ONE_TAP = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        
        // Una mica brut això d'aquí
        MyApplication.mContext = applicationContext

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail().build()
        gsc = GoogleSignIn.getClient(this, gso)

        var account = GoogleSignIn.getLastSignedInAccount(this)

        if(account != null){
            goToMainActivity()
        }

        signInButtonGoogle = findViewById(R.id.sign_in_button_google)

        signInButtonGoogle.setOnClickListener {
            googleSignIn()
        }

        auth = Firebase.auth
    }

    private fun googleSignIn(){
        var intent = gsc.signInIntent
        startActivityForResult(intent, REQ_ONE_TAP)
        Log.d(TAG, "googleSignIn")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Log.d(TAG, "onActivityResult")
        if(requestCode == REQ_ONE_TAP){
            // Log.d(TAG, "REQ_ONE_TAP")
            var task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                task.getResult(ApiException::class.java)
                goToMainActivity()
            } catch(e: ApiException){

            }
        }
    }

    private fun goToMainActivity(){
        var account = GoogleSignIn.getLastSignedInAccount(this)
        /*if (account != null) {
            Log.d(TAG, account.id.toString())
            Log.d(TAG, account.idToken.toString())
            Log.d(TAG, account.email.toString())
        }*/

        if (account != null) {
            UsersViewModel.addUserIdToDB(account.id.toString())
        }

        var intent = Intent(applicationContext, MainActivity::class.java)

        finish()

        startActivity(intent)
    }
}
