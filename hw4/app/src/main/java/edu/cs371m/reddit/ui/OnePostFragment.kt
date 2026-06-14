package edu.cs371m.reddit.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import edu.cs371m.reddit.R
import edu.cs371m.reddit.databinding.FragmentOnePostBinding
import edu.cs371m.reddit.glide.Glide
import kotlin.getValue

class OnePostFragment: Fragment(R.layout.fragment_one_post) {
    private val viewModel: MainViewModel by activityViewModels()
    private val args: OnePostFragmentArgs by navArgs()

    private fun bindPost(binding: FragmentOnePostBinding) {
        binding.onePostSubreddit.text = "r/${args.post.subreddit}"
        binding.onePostTitle.text = args.post.title

        if (args.post.selfText.isNullOrEmpty()) {
            binding.onePostSelfText.visibility = View.GONE
            binding.onePostScroll.visibility = View.GONE
        } else {
            binding.onePostSelfText.visibility = View.VISIBLE
            binding.onePostScroll.visibility = View.VISIBLE
            binding.onePostSelfText.text = args.post.selfText
        }

        Glide.glideFetch(args.post.imageURL, args.post.thumbnailURL, binding.onePostImage)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(javaClass.simpleName, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnePostBinding.bind(view)

        viewModel.titleValue = "One Post"
        bindPost(binding)
    }
}
