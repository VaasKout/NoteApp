package com.example.noteexample.allNotes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.databinding.RecyclerMainItemBinding
import com.example.noteexample.utils.adapter.NoteDiffCallBack

class NoteAdapter :
    ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallBack()) {
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
        fun bind(note: Note) {
            _holder.value = this
            binding.note = note
            binding.mainCard.isChecked = note.isChecked
            binding.executePendingBindings()
        }
    }
}
/**
 * Classic RecyclerView.Adapter, do not delete, use it to handle problems with database
 */
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