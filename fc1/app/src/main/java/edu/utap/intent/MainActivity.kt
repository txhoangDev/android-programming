package edu.utap.intent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import com.google.android.material.snackbar.Snackbar
import edu.utap.intent.databinding.ActivityMainBinding
import edu.utap.intent.databinding.ContentMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        const val userKey = "userKey"
        const val scoreIntKey = "scoreIntKey"
    }
    class Score(var name: String, var score: Int) {
        override fun toString(): String {
            return this.name + ": " + this.score
        }
    }

    // List is  sorted descending by score
    private var highScores = mutableListOf<Score>()

    // A binding to our layout
    private lateinit var contentMainBinding: ContentMainBinding

    // https://developer.android.com/training/basics/intents/result
    private var resultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(javaClass.simpleName, "result ok")
                // Process the result of intent
                // Show "Correct" Snackbar message to user
                // (see GuessingGame.messageUser for Snackbar ideas)
                // Process score
                // XXX Write me
                contentMainBinding.nameField.setText("")
                Snackbar.make(contentMainBinding.root, "Correct", Snackbar.LENGTH_LONG).show()
                val score = result.data?.getIntExtra(scoreIntKey, 0)
                addHighScore(Score(result.data?.getStringExtra(userKey).toString(), score!!))
                renderHighScores()
            } else {
                Log.w(javaClass.simpleName, "Bad activity return code ${result.resultCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        contentMainBinding = activityMainBinding.contentMain

        highScores.addAll(
            listOf(
                Score("Frank Zappa", 997),
                Score("A Student", 997),
                Score("Time to touch grass", 13)
            )
        )
        this.renderHighScores()
        contentMainBinding.playButton.setOnClickListener {
            play()
        }
        // Add menu items without overriding methods in the Activity
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Inflate the menu; this adds items to the action bar if it is present.
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle action bar item clicks here.
                // Other entities (e.g., fragments) can handle other menu items
                return when (menuItem.itemId) {
                    R.id.menu_exit -> {
                        finish(); true
                    }
                    else -> false
                }
            }
        })
    }
    private fun addHighScore(score: Score) {
        highScores.add(score)
        renderHighScores()
    }

    private fun renderHighScores() {
        // Sort list.  I love you Kotlin!
        highScores.sortWith(compareByDescending<Score>{it.score}.thenBy{it.name})
        // Convert Score objects into a list of strings
        val stringList = highScores.map { it.toString() }
        // A simple way to display lists
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1,
            stringList)
        contentMainBinding.highScoreList.adapter = adapter
    }

    private fun play() {
        // XXX Write me.  Snackbar on empty name, otherwise launch GuessingGame
        if (contentMainBinding.nameField.text.isEmpty()) {
            Snackbar.make(contentMainBinding.root, "Please enter your name", Snackbar.LENGTH_LONG).show()
        } else {
            val intent = Intent(this, GuessingGame::class.java)
            intent.putExtra(userKey, contentMainBinding.nameField.text.toString())
            intent.putExtra(scoreIntKey, 0)
            resultLauncher.launch(intent)
        }
    }
}
