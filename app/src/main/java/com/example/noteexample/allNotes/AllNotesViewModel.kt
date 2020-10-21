package com.example.noteexample.allNotes

import android.app.Application
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.*

class AllNotesViewModel(application: Application) : AndroidViewModel(application){

    private val repository : NoteRepository
    val allNotes : LiveData<List<Note>>

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
    }


    var actionMode: ActionMode? = null
    private val actionModeController = object : ActionMode.Callback{
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.action_menu, menu)
            return true
        }
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean{
            return when(item?.itemId){
                R.id.delete_action -> {
                    onDeleteSelected()
                    mode?.finish()
                    true
                }
                else -> false
            }
        }
        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            if (allNotes.value?.any { it.isChecked } == true){
           allNotes.value?.map {it.isChecked = false}
            }
            _checkedState.value = false
        }
    }

    /**
     * LiveData
     */
    private val _navigateToEditNoteFragment = MutableLiveData<Boolean>()
    val navigateToEditNoteFragment : LiveData<Boolean> = _navigateToEditNoteFragment

    private val _navigateToUpdateNoteFragment = MutableLiveData<Int?>()
    val navigateToUpdateNoteFragment : LiveData<Int?> = _navigateToUpdateNoteFragment

    private val _checkedState = MutableLiveData<Boolean>()
    var checkedState: LiveData<Boolean> = _checkedState

    private lateinit var currentNote: LiveData<Note>
    /**
     * Navigating methods
     */
    fun onStartNavigating(){
        _navigateToEditNoteFragment.value = true
    }
    fun onDoneEditNavigating(){
        _navigateToEditNoteFragment.value = false
    }

    fun onNoteClicked(id: Int){
        _navigateToUpdateNoteFragment.value = id
    }
    fun onDoneUpdateNavigating(){
        _navigateToUpdateNoteFragment.value = null
    }



    fun onInitCheckList(isChecked: Boolean, noteId: Int){
        currentNote = repository.selectNote(noteId)
        currentNote.value?.isChecked = isChecked
        _checkedState.value = true
    }

    /**
     * Action mode lifecycle functions
     */

    fun onStartActionMode(activity: FragmentActivity){
        actionMode = activity.startActionMode(actionModeController)
        actionMode?.title =
            "${allNotes.value?.filter { it.isChecked }?.size}"

    }
    fun onResumeActionMode() {
       actionMode?.title =
           "${allNotes.value?.filter { it.isChecked }?.size}"
    }

    fun onDoneActionMode(){
       actionMode?.finish()
    }

    /**
     * Coroutine functions
     */

    fun onDeleteSelected(){
        allNotes.value?.let {
            viewModelScope.launch {
                val deleteNoteList =
                    it.filter { it.isChecked }
                repository.deleteNotes(deleteNoteList)
            }
        }
    }

    fun onClear(){
       viewModelScope.launch {
           repository.deleteAllNotes()
           repository.deleteAllNoteContent()
       }
    }
}