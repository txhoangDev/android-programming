package edu.cs371m.reddit.ui.subreddits

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.cs371m.reddit.api.RedditPost
import edu.cs371m.reddit.databinding.RowSubredditBinding
import edu.cs371m.reddit.glide.Glide
import edu.cs371m.reddit.ui.MainViewModel
import edu.cs371m.reddit.ui.SearchAwareDiff

// NB: Could probably unify with PostRowAdapter if we had two
// different VH and override getItemViewType
// https://medium.com/@droidbyme/android-recyclerview-with-multiple-view-type-multiple-view-holder-af798458763b
// Private constructor so we can both pass SearchAwareDiff to ListAdapter
// and keep a reference to it for updating search terms in submitListWithTerm.
class SubredditListAdapter private constructor(
    private val viewModel: MainViewModel,
    private val navController: NavController,
    private val diffCallback: SearchAwareDiff
) : ListAdapter<RedditPost, SubredditListAdapter.VH>(diffCallback) {
    constructor(viewModel: MainViewModel, navController: NavController)
        : this(viewModel, navController, SearchAwareDiff())

    var searchTerm = ""
        private set

    fun submitListWithTerm(list: List<RedditPost>?, term: String) {
        diffCallback.oldSearchTerm = searchTerm
        diffCallback.newSearchTerm = term
        searchTerm = term
        submitList(list)
    }

    // ViewHolder pattern
    inner class VH(val rowSubredditBinding: RowSubredditBinding)
        : RecyclerView.ViewHolder(rowSubredditBinding.root) {

        init {
            rowSubredditBinding.subRowHeading.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    setSubredditAndPop(getItem(pos))
                }
            }

            rowSubredditBinding.subRowPic.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    setSubredditAndPop(getItem(pos))
                }
            }
        }
    }

    private fun highlight(text: String?, term: String): CharSequence {
        // XXX Write me (Hint: Create a SpannableString and apply ForegroundColorSpan to the matching range)
        val spannable = SpannableString(text)
        if (term.isBlank() || text == null) return spannable

        var index = text.indexOf(term, ignoreCase = true)
        while (index >= 0) {
            val endIndex = index + term.length
            spannable.setSpan(
                ForegroundColorSpan(Color.CYAN),
                index,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            index = text.indexOf(term, endIndex, ignoreCase = true)
        }
        return spannable
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowSubredditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    // Helper function to set the subreddit in the view model
    // and pop the back stack
    private fun setSubredditAndPop(post: RedditPost) {
        // XXX Write me
        viewModel.subredditValue = post.displayName ?: return
        navController.popBackStack()
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        // XXX Write me
        val subredditPost = getItem(position)

        val icon = subredditPost.iconURL ?: ""
        Glide.glideFetch(icon, icon, holder.rowSubredditBinding.subRowPic)

        holder.rowSubredditBinding.subRowHeading.text = highlight(subredditPost.displayName ?: "", searchTerm)
        holder.rowSubredditBinding.subRowDetails.text = highlight(subredditPost.publicDescription ?: "", searchTerm)
    }
}
