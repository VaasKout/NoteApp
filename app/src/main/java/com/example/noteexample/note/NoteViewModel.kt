package com.example.noteexample.note

import android.app.Application
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.*

class NoteViewModel(application: Application) : AndroidViewModel(application){

    private var selected: List<Note>? = null
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
     * Coroutines
     */
    
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * LiveData
     */
    private val _navigateToEditNoteFragment = MutableLiveData<Boolean>()
    val navigateToEditNoteFragment : LiveData<Boolean> = _navigateToEditNoteFragment

    private val _navigateToUpdateNoteFragment = MutableLiveData<Int>()
    val navigateToUpdateNoteFragment : LiveData<Int> = _navigateToUpdateNoteFragment

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

    fun onStartActionMode(activity: FragmentActivity){
        actionMode = activity.startActionMode(actionModeController)
        selected = allNotes.value?.filter { it.isChecked }
        actionMode?.title =
            "${allNotes.value?.filter { it.isChecked }?.size}"

    }
    fun onResumeActionMode() {
       selected = allNotes.value?.filter { it.isChecked }
       actionMode?.title =
           "${allNotes.value?.filter { it.isChecked }?.size}"
    }

    fun onDoneActionMode(){
       actionMode?.finish()
    }





    /**
     * Coroutine pattern
     */

    fun onDeleteSelected(){
        uiScope.launch {
            deleteSelected()
        }
    }

    private suspend fun deleteSelected(){
        withContext(Dispatchers.IO){
            selected?.let { repository.deleteNote(it) }
        }
    }

    fun onClear(){
       uiScope.launch {
           clear()
       }
    }

    private suspend fun clear(){
        withContext(Dispatchers.IO){
            repository.deleteAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}