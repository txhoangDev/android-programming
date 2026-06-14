package edu.cs371m.layout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.cs371m.layout.databinding.ActivityMainBinding
import edu.cs371m.layout.databinding.ActivityMainLinearBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainLinearBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        // To test AI challenge layout, uncomment the line below:
         setContentView(R.layout.ai_challenge)
    }
}
