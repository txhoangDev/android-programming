package edu.cs371m.reddit.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import edu.cs371m.reddit.R
import edu.cs371m.reddit.databinding.FragmentRvBinding

// XXX Write most of this file
class HomeFragment: Fragment(R.layout.fragment_rv) {
    // XXX initialize viewModel
    private val viewModel: MainViewModel by activityViewModels()

    // Set up the adapter and recycler view
    private fun initAdapter(binding: FragmentRvBinding) {
        val postRowAdapter = PostRowAdapter(viewModel) {
            Log.d("OnePost",
                String.format("OnePost title %s",
                    if (it.title.length > 32)
                        it.title.substring(0, 31) + "..."
                    else it.title))
            Log.d("doOnePost", "image ${it.imageURL}")
            // XXX Write me
            val action = HomeFragmentDirections.actionHomeFragmentToOnePostFragment(it)
            findNavController().navigate(action)
        }
        // XXX Write me, observe posts
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = postRowAdapter

        viewModel.observePosts().observe(viewLifecycleOwner) {
            postRowAdapter.submitListWithTerm(it, viewModel.searchTermValue)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.searchTerm.observe(viewLifecycleOwner) {
            postRowAdapter.submitListWithTerm(viewModel.postsValue, it)
        }
    }

    private fun initSwipeLayout(swipe : SwipeRefreshLayout) {
        // XXX Write me
        swipe.isEnabled = true
        swipe.setOnRefreshListener {
            viewModel.repoFetch()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentRvBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")
        // XXX Write me.  Set title based on current subreddit
        viewModel.titleValue = "r/${viewModel.subredditValue}"
        viewModel.showActionBarFavorites()
        initAdapter(binding)
        initSwipeLayout(binding.swipeRefreshLayout)
    }
}
