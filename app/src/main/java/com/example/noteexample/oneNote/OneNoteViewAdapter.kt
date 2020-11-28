package com.example.noteexample.oneNote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.Image
import com.example.noteexample.database.Note
import com.example.noteexample.databinding.HeaderViewBinding
import com.example.noteexample.databinding.RecyclerNoteContentViewItemBinding
import com.example.noteexample.utils.DataDiffCallBack
import com.example.noteexample.utils.NoteWithImagesRecyclerItems

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class OneNoteViewAdapter :
    ListAdapter<NoteWithImagesRecyclerItems, RecyclerView.ViewHolder>(DataDiffCallBack()) {

//    private val _noteHolder = MutableLiveData<NoteViewHolder>()
//    val noteHolder: LiveData<NoteViewHolder> = _noteHolder

    private val _noteContentHolder = MutableLiveData<NoteContentViewHolder>()
    val noteContentHolder: LiveData<NoteContentViewHolder> = _noteContentHolder

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            ITEM_VIEW_TYPE_HEADER
        } else {
            ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NoteViewHolder -> {
                getItem(position).note?.let { holder.bind(it) }
            }
            is NoteContentViewHolder -> {
                getItem(position).image?.let { holder.bind(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> {
                val binding: HeaderViewBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.header_view,
                        parent,
                        false
                    )
                return NoteViewHolder(binding)
            }
            ITEM_VIEW_TYPE_ITEM -> {
                val binding: RecyclerNoteContentViewItemBinding =
                    DataBindingUtil
                        .inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.recycler_note_content_view_item,
                            parent,
                            false
                        )
                return NoteContentViewHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    inner class NoteContentViewHolder(val binding: RecyclerNoteContentViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            _noteContentHolder.value = this
            binding.data = image
            if (image.signature.isNotEmpty()){
                binding.viewNoteItem.visibility = View.VISIBLE
            }
        }
    }

    inner class NoteViewHolder(val binding: HeaderViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
//            _noteHolder.value = this
            binding.note = note
            if (note.title.isNotEmpty() && note.text.isNotEmpty()){
                binding.headerView.visibility = View.VISIBLE
            }
        }
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