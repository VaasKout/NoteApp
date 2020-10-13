package com.example.noteexample.allNotes

import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.databinding.RecyclerMainItemBinding

class NoteAdapter():
    ListAdapter<Note, NoteAdapter.InsertUpdateViewHolder>(NoteDiffCallBack()){
    /**
     * [isActive] gets instance of InsertUpdateViewHolder and observed in [AllNotesFragment]
     * to set clickListeners for recycler items
     */
    private val _isActive = MutableLiveData<InsertUpdateViewHolder>()
    val isActive: LiveData<InsertUpdateViewHolder> = _isActive

    override fun onBindViewHolder(holder: InsertUpdateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsertUpdateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        /**
         * Use only DataBindingUtil else layout options crash in recycler_main_item
         */
        val binding: RecyclerMainItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.recycler_main_item, parent, false)
        return InsertUpdateViewHolder(binding)
    }

     inner class InsertUpdateViewHolder (val binding: RecyclerMainItemBinding):
        RecyclerView.ViewHolder(binding.root){

         fun bind(note: Note){
             _isActive.value = this
            binding.note = note
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