package com.example.noteexample.allNotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.databinding.RecyclerMainItemBinding
import com.example.noteexample.utils.GlideApp

class NoteAdapter :
    ListAdapter<NoteWithImages, NoteAdapter.NoteViewHolder>(NoteDiffCallBack()) {

    /**
     * [holder] gets instance of InsertUpdateViewHolder and observed in [AllNotesFragment]
     * to set clickListeners for recycler items
     */

    private val _holder = MutableLiveData<NoteViewHolder>()
    val holder: LiveData<NoteViewHolder> = _holder

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        /**
         * Use only DataBindingUtil else layout options crash in recycler_main_item
         */
        val binding: RecyclerMainItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.recycler_main_item, parent, false)
        return NoteViewHolder(binding)
    }

    inner class NoteViewHolder(val binding: RecyclerMainItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Checked state of card depends on [Note.isChecked] state
         */
        fun bind(noteWithImages: NoteWithImages) {
//            binding.mainCard.isChecked = noteWithImages.note.isChecked
            binding.mainCard.isChecked = false
            binding.view1.visibility = View.GONE
            binding.view2.visibility = View.GONE
            binding.photoMain.visibility = View.GONE
            binding.titleMain.visibility = View.GONE
            binding.noteMain.visibility = View.GONE

            if (noteWithImages.header.title.isNotEmpty()) {
                binding.titleMain.visibility = View.VISIBLE
                binding.titleMain.text = noteWithImages.header.title
            }

            if (noteWithImages.header.text.isNotEmpty()) {
                binding.noteMain.visibility = View.VISIBLE
                binding.noteMain.text = noteWithImages.header.text
            }

            if (noteWithImages.images.isNotEmpty()) {
                var photoInserted = false
                for (content in noteWithImages.images) {
                    if (content.photoPath.isNotEmpty()) {
                        binding.photoMain.visibility = View.VISIBLE
                        GlideApp.with(binding.photoMain.context)
                            .load(content.photoPath)
                            .into(binding.photoMain)
                        photoInserted = true
                        break
                    }
                }
                if (!photoInserted &&
                    noteWithImages.header.text.isEmpty() &&
                    noteWithImages.images[0].signature.isNotEmpty()
                ) {
                    binding.noteMain.text = noteWithImages.images[0].signature
                    binding.noteMain.visibility = View.VISIBLE
                }

            }
            if (noteWithImages.header.title.isNotEmpty() && noteWithImages.header.text.isNotEmpty()) {
                binding.view1.visibility = View.VISIBLE
            }
            if ((noteWithImages.header.text.isNotEmpty() || noteWithImages.header.title.isNotEmpty()) &&
                binding.photoMain.visibility == View.VISIBLE
            ) {
                binding.view2.visibility = View.VISIBLE
            }
            _holder.value = this
            binding.header = noteWithImages.header
            binding.executePendingBindings()
        }
    }
}

class NoteDiffCallBack : DiffUtil.ItemCallback<NoteWithImages>() {
    override fun areItemsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
        return oldItem == newItem
    }
}





