package edu.utap.photolist.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import edu.utap.photolist.MainActivity
import edu.utap.photolist.MainViewModel
import edu.utap.photolist.R
import edu.utap.photolist.SortColumn
import edu.utap.photolist.databinding.FragmentHomeBinding

class HomeFragment :
    Fragment(R.layout.fragment_home) {
    private val viewModel: MainViewModel by activityViewModels()
    // It is a real bummer that we must initialize a registerForActivityResult
    // here or in onViewCreated.  You CAN'T initialize it in an onClickListener
    // where it could capture state like the file name.
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.pictureSuccess()
        } else {
            viewModel.pictureFailure()
        }
    }

    // Touch helpers provide functionality like detecting swipes or moving
    // entries in a recycler view.  Here we do swipe left to delete
    private fun initTouchHelper(): ItemTouchHelper {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START)
            {
                override fun onMove(recyclerView: RecyclerView,
                                    viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder): Boolean {
                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                      direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    // ew: Drop the touch event in this difficult corner case
                    if (position == RecyclerView.NO_POSITION) return
                    Log.d(javaClass.simpleName, "Swipe delete $position")
                    viewModel.removePhotoAt(position)
                }
            }
        return ItemTouchHelper(simpleItemTouchCallback)
    }
    // No need for onCreateView because we passed R.layout to Fragment constructor
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")
        val mainActivity = (requireActivity() as MainActivity)

        // Long press to edit.
        val adapter = PhotoMetaAdapter(viewModel)

        val rv = binding.photosRV
        val itemDecor = DividerItemDecoration(rv.context, LinearLayoutManager.VERTICAL)
        rv.addItemDecoration(itemDecor)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(rv.context)
        // Swipe left to delete
        initTouchHelper().attachToRecyclerView(rv)

        // XXX Write me, onclick listeners and observers
        viewModel.photoMetaList.observe(viewLifecycleOwner) {
            adapter.submitList(it.toList())
            mainActivity.progressBarOff()
        }

        // title sort listener
        binding.rowPictureTitle.setOnClickListener {
            mainActivity.progressBarOn()

            viewModel.sortInfoClick(SortColumn.TITLE) {
                mainActivity.progressBarOff()
            }
        }

        // size sort listener
        binding.rowSize.setOnClickListener {
            mainActivity.progressBarOn()

            viewModel.sortInfoClick(SortColumn.SIZE) {
                mainActivity.progressBarOff()
            }
        }

        // camera btn listener
        binding.cameraButton.setOnClickListener {
            val title = binding.inputET.text?.toString()?.trim().orEmpty()
            if (title.isEmpty()) {
                Snackbar.make(view, "You must title picture", Snackbar.LENGTH_SHORT).show()
            } else {
                binding.inputET.text?.clear()
                mainActivity.progressBarOn()
                TakePictureWrapper.takePicture(title, requireContext(), viewModel, cameraLauncher)
            }
        }

        // observe sort changes
        viewModel.sortInfo.observe(viewLifecycleOwner) {
            binding.rowPictureTitle.setBackgroundColor(Color.WHITE)
            binding.rowSize.setBackgroundColor(Color.WHITE)
            if (it.sortColumn == SortColumn.TITLE) {
                if (it.ascending) {
                    binding.rowPictureTitle.setBackgroundColor(Color.YELLOW)
                } else {
                    binding.rowPictureTitle.setBackgroundColor(Color.RED)
                }
            } else {
                if (it.ascending) {
                    binding.rowSize.setBackgroundColor(Color.YELLOW)
                } else {
                    binding.rowSize.setBackgroundColor(Color.RED)
                }
            }

            mainActivity.progressBarOn()
            viewModel.fetchPhotoMeta {
                mainActivity.progressBarOff()
            }
        }
    }
}