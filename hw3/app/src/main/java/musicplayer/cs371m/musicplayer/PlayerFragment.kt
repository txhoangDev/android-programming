package musicplayer.cs371m.musicplayer

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import musicplayer.cs371m.musicplayer.databinding.PlayerFragmentBinding
import java.util.concurrent.atomic.AtomicBoolean

class PlayerFragment : Fragment() {
    // When this is true, the displayTime coroutine should not modify the seek bar
    val userModifyingSeekBar = AtomicBoolean(false)
    private val viewModel: MainViewModel by activityViewModels()
    // witchel: experiment
    private lateinit var adapter: RVDiffAdapter

    private var _binding: PlayerFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var lastIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlayerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initRecyclerViewDividers(rv: RecyclerView) {
        // Let's have dividers between list items
        val dividerItemDecoration = DividerItemDecoration(
            rv.context, LinearLayoutManager.VERTICAL
        )
        rv.addItemDecoration(dividerItemDecoration)
    }

    // Please put all display updates in this function
    // The exception is that
    //   displayTime updates the seek bar, time passed & time remaining
    private fun updateDisplay() {
        // If settings is active, we are in the background and do
        // not have a binding.  Return early.
        if (_binding == null) {
            return
        }
        //XXX Write me. Make sure all player UI elements are up to date
        // That includes all buttons, textViews, icons & the seek bar

        // update song name
        binding.playerCurrentSongText.text = viewModel.getCurrentSongName()
        binding.playerNextSongText.text = viewModel.getNextSongName()

        // update loop button
        if (viewModel.loop)
            binding.loopIndicator.setBackgroundColor(Color.RED)
        else
            binding.loopIndicator.setBackgroundColor(Color.WHITE)

        // update play/pause icon
        binding.playerPlayPauseButton.setImageResource(
            if (viewModel.isPlaying) R.drawable.ic_pause_black_24dp
            else R.drawable.ic_play_arrow_black_24dp
        )

        // update seek bar
        binding.playerSeekBar.max = viewModel.player.duration
        binding.playerSeekBar.progress = viewModel.player.currentPosition

        // update time
        binding.playerTimePassedText.text = convertTime(viewModel.player.currentPosition)
        binding.playerTimeRemainingText.text = convertTime(viewModel.player.duration - viewModel.player.currentPosition)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // witchel: experiment

        // Make the RVDiffAdapter and set it up
        //XXX Write me. Setup adapter.
        adapter = RVDiffAdapter(viewModel) { songIndex ->
            changeSong(songIndex)
        }
        binding.playerRV.layoutManager = LinearLayoutManager(requireContext())
        binding.playerRV.adapter = adapter
        initRecyclerViewDividers(binding.playerRV)
        adapter.submitList(viewModel.getCopyOfSongInfo())

        //XXX Write me. Write callbacks for buttons
        binding.playerPlayPauseButton.setOnClickListener {
            viewModel.isPlaying = !viewModel.isPlaying
            if (viewModel.isPlaying) {
                startAndUpdatePlayer()
            } else {
                viewModel.player.pause()
            }
            updateDisplay()
        }

        binding.playerSkipBackButton.setOnClickListener {
            viewModel.prevSong()
            changeSong(viewModel.currentIndex)
        }

        binding.playerSkipForwardButton.setOnClickListener {
            viewModel.nextSong()
            changeSong(viewModel.currentIndex)
        }

        // call back for completion on mediaplayer
        setupSongCompletion()

        binding.loopIndicator.setOnClickListener {
            viewModel.loop = !viewModel.loop
            if (viewModel.loop)
                binding.loopIndicator.setBackgroundColor(Color.RED)
            else
                binding.loopIndicator.setBackgroundColor(Color.WHITE)
        }

        binding.playerPermuteButton.setOnClickListener {
            adapter.submitList(viewModel.shuffleAndReturnCopyOfSongInfo()) {
                adapter.notifyDataSetChanged()
            }
            updateDisplay()
        }

        //XXX Write me. binding.playerSeekBar.setOnSeekBarChangeListener
        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                userModifyingSeekBar.set(true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                viewModel.player.seekTo(seekBar.progress)
                userModifyingSeekBar.set(false)
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateTime(progress, viewModel.player.duration)
                }
            }
        })


        // Observe currentIndex LiveData to update song display reactively
        //XXX Write me. Observe LiveData
        viewModel.observableCurrentIndex.observe(viewLifecycleOwner) { updatedIndex ->
            adapter.notifyItemChanged(lastIndex)
            adapter.notifyItemChanged(updatedIndex)
            lastIndex = updatedIndex
            adapter.submitList(viewModel.getCopyOfSongInfo())
            updateDisplay()
        }

        updateDisplay()

        // Don't change this code.  We are launching a coroutine (user-level thread) that will
        // execute concurrently with our code, but will update the displayed time
        val millisec = 100L
        viewLifecycleOwner.lifecycleScope.launch {
            displayTime(millisec)
        }
    }

    // The suspend modifier marks this as a function called from a coroutine
    // Note, this whole function is somewhat reminiscent of the Timer class
    // from Fling and Peck.  We use an independent thread to manage one small
    // piece of our GUI.  This coroutine should not modify any data accessed
    // by the main thread (it can read property values)
    private suspend fun displayTime(misc: Long) {
        // This only runs while the display is active
        while (viewLifecycleOwner.lifecycleScope.coroutineContext.isActive) {
            val currentPosition = viewModel.player.currentPosition
            val maxTime = viewModel.player.duration
            // Update the seek bar (if the user isn't updating it)
            // and update the passed and remaining time
            //XXX Write me
            if (userModifyingSeekBar.get()) {
                updateTime(currentPosition, maxTime)
            } else {
                binding.playerSeekBar.max = maxTime
                binding.playerSeekBar.progress = currentPosition
                updateTime(currentPosition, maxTime)
            }


            // Leave this code as is.  it inserts a delay so that this thread does
            // not consume too much CPU
            delay(misc)
        }
    }

    // This method converts time in milliseconds to minutes-second formatted string
    // with two digit minutes and two digit sections, e.g., 01:30
    private fun convertTime(milliseconds: Int): String {
        //XXX Write me
        var seconds = milliseconds / 1000
        val minutes = (seconds) / 60
        seconds -= (minutes * 60)
        return "%02d:%02d".format(minutes, seconds)
    }

    // XXX Write me, handle player dynamics and currently playing/next song
    private fun changeSong(newSongIndex: Int) {
        // reinitialize player
        viewModel.player.reset()

        // Update index and create a new player
        viewModel.currentIndex = newSongIndex
        viewModel.player.release()
        viewModel.player = MediaPlayer.create(requireContext(), viewModel.getCurrentSongResourceId())

        setupSongCompletion()

        if (viewModel.isPlaying) {
            startAndUpdatePlayer()
        }

        updateDisplay()
    }

    private fun startAndUpdatePlayer() {
        viewModel.player.start()
        viewModel.incrementSongsPlayed()
    }

    private fun setupSongCompletion() {
        viewModel.player.setOnCompletionListener {
            if (viewModel.loop) {
                viewModel.player.seekTo(0)
                startAndUpdatePlayer()
            } else {
                viewModel.nextSong()
                changeSong(viewModel.currentIndex)
            }
        }
    }

    private fun updateTime(currentPosition: Int, maxTime: Int) {
        binding.playerTimePassedText.text = convertTime(currentPosition)
        binding.playerTimeRemainingText.text = convertTime(maxTime - currentPosition)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}