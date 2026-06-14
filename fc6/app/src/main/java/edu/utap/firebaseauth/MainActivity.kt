package edu.utap.firebaseauth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import edu.utap.firebaseauth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
    private lateinit var binding : ActivityMainBinding
    private lateinit var authUser : AuthUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(savedInstanceState == null) {
            binding.displayNameET.text.clear()
        }
        binding.logoutBut.setOnClickListener {
            // XXX Write me.
            binding.displayNameET.text.clear()
            authUser.logout()
        }
        // If the user spam clicks the login button (clicking it many times in
        // a row), we only want to log in once.
        binding.loginBut.setOnClickListener {
            // XXX Write me.
            authUser.login()
        }
        binding.setDisplayName.setOnClickListener {
            // XXX Write me.
            if (!binding.displayNameET.text.isEmpty()) {
                authUser.setDisplayName(binding.displayNameET.text.toString())
            }
        }
    }

    // We can only safely initialize AuthUser once onCreate has completed.
    override fun onStart() {
        super.onStart()
        // Initialize AuthUser, observe data to display in UI
        // https://developer.android.com/reference/androidx/lifecycle/Lifecycle#addObserver(androidx.lifecycle.LifecycleObserver)
        // XXX Write me.
        authUser = AuthUser(this.activityResultRegistry)
        this.lifecycle.addObserver(authUser)
        authUser.observeUser().observe(this) { user ->
            Log.d(TAG, "XXX user $user")
            binding.displayName.text = user.name
            binding.userEmail.text = user.email
            binding.userUid.text = user.uid
        }
    }
}