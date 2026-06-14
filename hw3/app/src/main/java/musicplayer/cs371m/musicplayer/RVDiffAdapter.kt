package musicplayer.cs371m.musicplayer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import musicplayer.cs371m.musicplayer.databinding.SongRowBinding

// Pass in a function called clickListener that takes a view and a songName
// as parameters.  Call clickListener when the row is clicked.
class RVDiffAdapter(private val viewModel: MainViewModel,
                    private val clickListener: (songIndex : Int)->Unit)
    // https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
    // Slick adapter that provides submitList, so you don't worry about how to update
    // the list, you just submit a new one when you want to change the list and the
    // Diff class computes the smallest set of changes that need to happen.
    // NB: Both the old and new lists must both be in memory at the same time.
    // So you can copy the old list, change it into a new list, then submit the new list.
    : ListAdapter<SongInfo,
        RVDiffAdapter.ViewHolder>(Diff())
{
    companion object {
        val TAG = "RVDiffAdapter"
    }

    // ViewHolder pattern holds row binding
    inner class ViewHolder(val songRowBinding : SongRowBinding)
        : RecyclerView.ViewHolder(songRowBinding.root) {
        init {
            //XXX Write me.
            songRowBinding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    // update previous song
                    val previousIndex = viewModel.currentIndex
                    notifyItemChanged(previousIndex)

                    // update current song
                    viewModel.currentIndex = pos
                    clickListener(pos)
                    notifyItemChanged(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //XXX Write me.
        val inflater = LayoutInflater.from(parent.context)
        val binding = SongRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //XXX Write me.
        val song = getItem(position)
        holder.songRowBinding.songTitle.text = song.name
        holder.songRowBinding.songTime.text = song.time

        // setting background color
        if (position == viewModel.currentIndex) {
            holder.songRowBinding.root.setBackgroundColor(Color.YELLOW)
        } else {
            holder.songRowBinding.root.setBackgroundColor(Color.WHITE)
        }

    }

    class Diff : DiffUtil.ItemCallback<SongInfo>() {
        // Item identity
        override fun areItemsTheSame(oldItem: SongInfo, newItem: SongInfo): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
        // Item contents are the same, but the object might have changed
        override fun areContentsTheSame(oldItem: SongInfo, newItem: SongInfo): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.rawId == newItem.rawId
                    && oldItem.time == newItem.time
        }
    }
}
