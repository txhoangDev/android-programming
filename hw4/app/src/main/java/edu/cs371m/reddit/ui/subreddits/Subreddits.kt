package edu.cs371m.reddit.ui.subreddits

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cs371m.reddit.R
import edu.cs371m.reddit.databinding.FragmentRvBinding
import edu.cs371m.reddit.ui.MainViewModel

class Subreddits : Fragment(R.layout.fragment_rv) {
    // XXX initialize viewModel
    private val viewModel: MainViewModel by activityViewModels()

    // XXX Write me, onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentRvBinding.bind(view)

        viewModel.titleValue = "Subreddits"
        viewModel.hideActionBarFavorites()
        binding.swipeRefreshLayout.isEnabled = false

        val adapter = SubredditListAdapter(viewModel, findNavController())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.observeSubreddits().observe(viewLifecycleOwner) {
            adapter.submitListWithTerm(it, viewModel.searchTermValue)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.searchTerm.observe(viewLifecycleOwner) {
            adapter.submitListWithTerm(viewModel.postsValue, it)
        }
    }
}
