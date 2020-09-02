package com.example.noteexample.note

import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.databinding.RecyclerItemBinding

class NoteAdapter(private val clickListener: NoteListener):
    ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallBack()){
    private val _isActive = MutableLiveData<NoteViewHolder>()
    val isActive: LiveData<NoteViewHolder> = _isActive
    val click = clickListener
    /**
     * holder takes item with position and define, which note it is, then pass it into note variable
     * from recycler_item where onClick method calls from NoteListener. Same as setOnClickListener,
     * but with binding and viewModel
     */

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        /**
         * Use only DataBindingUtil else layout options crash in recycler_item
         */
        val binding: RecyclerItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.recycler_item, parent, false)
        return NoteViewHolder(binding)
    }

     inner class NoteViewHolder (val binding: RecyclerItemBinding):
        RecyclerView.ViewHolder(binding.root){


         fun bind(note: Note, clickListener: NoteListener){
             _isActive.value = this
            binding.note = note
            binding.clickListener = clickListener
            binding.materialCard.isChecked = note.isChecked
            binding.executePendingBindings()
        }
     }
}

class NoteDiffCallBack : DiffUtil.ItemCallback<Note>(){
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}

/**
 * ClickListener for RecyclerView
 */
class NoteListener(val noteListener: (noteId: Int) -> Unit,
                   val actionModeListener: (isChecked: Boolean, noteId: Int) -> Unit){
    fun onClick(note: Note) = noteListener(note.id)
    fun onLongClick(note: Note) = actionModeListener(note.isChecked, note.id)
}



    /**
     * Classic RecyclerView.Adapter, do not delete, use it to handle problems with database
    */
//class NoteAdapter(private val clickListener: NoteListener)
//        : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallBack()){
//        val checkedList = mutableListOf<Boolean>()
//    inner class NoteViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
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
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
//        val layoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.
//        inflate(R.layout.recycler_item, parent, false)
//        return NoteViewHolder(view)
//    }
//
//
//    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
//        holder.bind(getItem(position), clickListener, checkedList[position])
//    }
//}