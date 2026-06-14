package edu.utap.livedata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import edu.utap.livedata.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val consumerTitle = "Consumer"
        private const val producerTitle = "Producer"
    }
    private fun navigateProducer() {
        // XXX Write me, navigate to produce fragment, pass title
        val navController = findNavController(R.id.produceFrame)
        val action = NavGraphDirections.createProducer(producerTitle)
        navController.navigate(action)
    }
    private fun navigateConsumer() {
        // Make sure that if we navigate to the consumer many times, we only ever push
        // a single fragment.  That way, we can click spawn consumer many times, but
        // a single click of kill consumer will get rid of it.
        // This is equivalent to adding
        // app:launchSingleTop="true" to the createConsumer action
        // Use this navOptions object when navigating to the consumer.
        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
        // XXX Write me, navigate to consume fragment, pass title
        val navController = findNavController(R.id.consumeFrame)
        val action = NavGraphDirections.createConsumer(consumerTitle)
        navController.navigate(action, navOptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateProducer()
        navigateConsumer()
        binding.killConsumeBut.setOnClickListener {
            // XXX Write me, find nav controller, pop back stack
            val navController = findNavController(R.id.consumeFrame)
            navController.popBackStack()
        }
        binding.spawnConsumeBut.setOnClickListener {
            navigateConsumer()
        }
    }
}