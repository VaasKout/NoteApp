package com.example.noteexample.oneNote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.NoteContent
import com.example.noteexample.databinding.RecyclerNoteContentViewItemBinding


class OneNoteViewAdapter :
    ListAdapter<NoteContent, OneNoteViewAdapter.NoteContentViewHolder>(NoteDiffCallBack()) {

    private val _holder = MutableLiveData<NoteContentViewHolder>()
    val holder: LiveData<NoteContentViewHolder> = _holder

    override fun onBindViewHolder(holder: NoteContentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteContentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: RecyclerNoteContentViewItemBinding =
            DataBindingUtil
                .inflate(layoutInflater, R.layout.recycler_note_content_view_item, parent, false)
        return NoteContentViewHolder(binding)
    }

    inner class NoteContentViewHolder(val binding: RecyclerNoteContentViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(noteContent: NoteContent) {
            _holder.value = this
        }
    }
}

class NoteDiffCallBack : DiffUtil.ItemCallback<NoteContent>() {
    override fun areItemsTheSame(oldItem: NoteContent, newItem: NoteContent): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NoteContent, newItem: NoteContent): Boolean {
        return oldItem == newItem
    }
}

//class NoteAdapter(private val clickListener: NoteListener)
//        : ListAdapter<Note, NoteAdapter.InsertUpdateViewHolder>(NoteDiffCallBack()){
//        val checkedList = mutableListOf<Boolean>()

//    inner class InsertUpdateViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
//        private val titleTextView : TextView = itemView.findViewById(R.id.titleRecyclerItem)
//        private val noteText : TextView = itemView.findViewById(R.id.noteRecyclerItem)
//        val materialCard: MaterialCardView = itemView.findViewById(R.id.materialCard)
//
//
//        fun bind(note: Note, clickListener: NoteListener, setChecked: Boolean){
//            val currentNote = getItem(adapterPosition)
//            if (currentNote.title.isEmpty()){
//                titleTextView.visibility = View.GONE
//            } else titleTextView.text = currentNote.title
//            if (currentNote.note.isEmpty()){
//                noteText.visibility = View.GONE
//            } else noteText.text = currentNote.note
//
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsertUpdateViewHolder {
//        val layoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.
//        inflate(R.layout.recycler_main_item, parent, false)
//        return InsertUpdateViewHolder(view)
//    }
//
//
//    override fun onBindViewHolder(holder: InsertUpdateViewHolder, position: Int) {
//        holder.bind(getItem(position), clickListener, checkedList[position])
//    }
//}