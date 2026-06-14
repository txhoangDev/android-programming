package edu.cs371m.reddit.ui

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.cs371m.reddit.R
import edu.cs371m.reddit.api.RedditPost
import edu.cs371m.reddit.databinding.RowPostBinding
import edu.cs371m.reddit.glide.Glide

/**
 * Created by witchel on 8/25/2019
 */

class SearchAwareDiff : DiffUtil.ItemCallback<RedditPost>() {
    var oldSearchTerm = ""
    var newSearchTerm = ""
    override fun areItemsTheSame(oldItem: RedditPost, newItem: RedditPost) =
        oldItem.key == newItem.key
    override fun areContentsTheSame(oldItem: RedditPost, newItem: RedditPost) =
        oldItem == newItem && oldSearchTerm == newSearchTerm
}

// https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
// Slick adapter that provides submitList, so you don't worry about how to update
// the list, you just submit a new one when you want to change the list and the
// Diff class computes the smallest set of changes that need to happen.
// NB: Both the old and new lists must both be in memory at the same time.
// So you can copy the old list, change it into a new list, then submit the new list.
//
// You can call adapterPosition to get the index of the selected item
// Private constructor so we can both pass SearchAwareDiff to ListAdapter
// and keep a reference to it for updating search terms in submitListWithTerm.
class PostRowAdapter private constructor(
    private val viewModel: MainViewModel,
    private val onPostSelected: (RedditPost)->Unit,
    private val diffCallback: SearchAwareDiff
) : ListAdapter<RedditPost, PostRowAdapter.VH>(diffCallback) {
    constructor(viewModel: MainViewModel, onPostSelected: (RedditPost)->Unit)
        : this(viewModel, onPostSelected, SearchAwareDiff())

    inner class VH(val binding: RowPostBinding)
        : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.selfText.setOnClickListener {
                    onPostSelected(getItem(bindingAdapterPosition))
                }

                binding.title.setOnClickListener {
                    onPostSelected(getItem(bindingAdapterPosition))
                }

                binding.image.setOnClickListener {
                    onPostSelected(getItem(bindingAdapterPosition))
                }
            }
        }

    var searchTerm = ""
        private set

    private fun setFavoritesIcon(holder: VH, post: RedditPost) {
        if (viewModel.favoritesValue.contains(post)) {
            holder.binding.rowFav.setImageResource(R.drawable.ic_favorite_black_24dp)
        } else {
            holder.binding.rowFav.setImageResource(R.drawable.ic_favorite_border_black_24dp)
        }
    }

    private fun highlightText(text: String, term: String): SpannableString {
        val spannable = SpannableString(text)
        if (term.isBlank()) return spannable

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

    fun submitListWithTerm(list: List<RedditPost>?, term: String) {
        diffCallback.oldSearchTerm = searchTerm
        diffCallback.newSearchTerm = term
        searchTerm = term
        notifyItemRangeChanged(0, itemCount)
        submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val post = getItem(position)

        holder.binding.title.text = highlightText(post.title, searchTerm)

        if (post.selfText.isNullOrEmpty()) {
            holder.binding.selfText.visibility = View.GONE
        } else {
            holder.binding.selfText.visibility = View.VISIBLE
            holder.binding.selfText.text = highlightText(post.selfText, searchTerm)
        }

        Glide.glideFetch(post.imageURL, post.thumbnailURL, holder.binding.image)

        holder.binding.score.text = post.score.toString()

        holder.binding.comments.text = post.commentCount.toString()

        // setting favorites icon
        setFavoritesIcon(holder, post)
        holder.binding.rowFav.setOnClickListener {
            viewModel.addFavorites(post)
            setFavoritesIcon(holder, post)
        }
    }
}
