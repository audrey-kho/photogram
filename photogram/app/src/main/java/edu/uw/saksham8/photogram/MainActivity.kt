package edu.uw.saksham8.photogram

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.parcel.Parcelize

const val TAG = "log"

class MainActivity : AppCompatActivity() {
    companion object {
        const val SIGN_IN_RESULT_CODE = 1001
        var uid = FirebaseAuth.getInstance().currentUser?.displayName
        var user = FirebaseAuth.getInstance().currentUser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        Firebase.database.reference.child("emails").push().setValue(pref.all["email"])
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        user = FirebaseAuth.getInstance().currentUser
        return when (item.itemId) {
            R.id.login -> {
                if (user == null) {
                    launchSignInFlow()
                    Log.i(TAG, "Logged in")
                } else {
                    launchSignOutFlow()
                    Log.i(TAG, "Logged out")
                }
                true
            }
            else -> item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment)) || super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        user = FirebaseAuth.getInstance().currentUser
        uid = FirebaseAuth.getInstance().currentUser?.displayName
        val item = menu?.findItem(R.id.login)
        if (user != null && item != null) {
            item.title = "Log out"
        } else if (user == null && item != null) {
            item.title = "Log in"
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                user = FirebaseAuth.getInstance().currentUser
                uid = FirebaseAuth.getInstance().currentUser?.displayName
                invalidateOptionsMenu()
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
                Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email
        // If users choose to register with their email,
        // they will need to create a password as well
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }

    private fun launchSignOutFlow() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                invalidateOptionsMenu()
            }
    }

}