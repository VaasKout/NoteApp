package com.example.noteexample.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.R
import com.example.noteexample.adapters.EditNoteAdapter
import com.example.noteexample.database.FirstNote
import com.example.noteexample.databinding.FragmentEditNoteBinding
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.Camera
import com.example.noteexample.utils.ItemHelperCallback
import com.example.noteexample.viewmodels.EditNoteViewModel
import com.example.noteexample.viewmodels.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class EditNoteFragment : Fragment() {

    /**
     * [requestPermissionLauncher] request permission for storage
     * [startCamera] opens camera to make and save photo in photoPath
     *
     * @see Camera
     */

    //TODO read about material card
    //TODO insert header bug in AllNotes
    //TODO bugs with view in OneNoteFragment

    @Inject
    lateinit var camera: Camera

    @Inject
    lateinit var repository: NoteRepository

    private val noteAdapter = EditNoteAdapter()
    private val itemHelperCallback = ItemHelperCallback()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var startCamera: ActivityResultLauncher<Intent>
    private val args by navArgs<EditNoteFragmentArgs>()

    lateinit var binding: FragmentEditNoteBinding

    private val viewModel by viewModels<EditNoteViewModel> {
        NoteViewModelFactory(args.noteID, repository)
    }

    private val imgHelper = itemHelperCallback.getHelper(
        startIndex = 1,
        swapAction = { from: Int, to: Int ->
            viewModel.swapItems(from, to)
        },
        clearViewAction = {
            viewModel.updateAfterSwap()
        }
    )


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        /**
         * binding for EditNoteFragment
         * @see R.layout.fragment_edit_note
         */

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_edit_note,
            container,
            false
        )
        binding.lifecycleOwner = this
        initCameraAndRequest()

        binding.editRecycler.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
        }
        imgHelper.attachToRecyclerView(binding.editRecycler)

        //navIcon clickListener
        binding.toolbarNoteEdit.setNavigationOnClickListener {
            viewModel.onStartNavigating()
        }


        //menuClickListener
        binding.toolbarNoteEdit.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.insert_photo -> {
                    viewModel.updateCurrentNote()
                    val items =
                        requireNotNull(this.activity).application
                            .resources.getStringArray(R.array.dialog_array)
                    MaterialAlertDialogBuilder(requireContext())
                        .setItems(items) { _, index ->
                            when (index) {
                                0 -> {
                                    startCamera.launch(
                                        camera.dispatchTakePictureIntent()
                                    )
                                }
                                1 -> {
                                    if (ContextCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        viewModel.startNote?.header?.let { item ->
                                            this@EditNoteFragment.findNavController()
                                                .navigate(
                                                    EditNoteFragmentDirections
                                                        .actionEditNoteFragmentToGalleryFragment(
                                                            item.headerID
                                                        )
                                                )
                                        }

                                    } else {
                                        requestPermissionLauncher.launch(
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        )
                                    }
                                }
                            }
                        }.show()
                    true
                }
                R.id.todo -> {
                    viewModel.currentNote?.notes?.get(0)?.let { note ->
                        lifecycleScope.launch {
                            if (note.todoItem) {
                                it.icon = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_todo_list
                                )
                            } else {
                                it.icon = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_simple_list
                                )
                            }
                            viewModel.changeListMod()
                            binding.editRecycler.adapter = noteAdapter
                        }
                    }
                    true
                }
                R.id.save_note -> {
                    viewModel.onStartNavigating()
                    true
                }
                else -> false
            }
        }

        /**
         * LiveData for header
         * @see EditNoteAdapter.headerHolder
         *
         * TextChangeListener write title and text values and updates them when exit
         * or accidentally close app if this is note is new
         * @see onPause
         */

//        remove flag
//        someTextView.setPaintFlags(someTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG))

        noteAdapter.headerHolder.observe(viewLifecycleOwner) { holder ->
            holder.binding.titleEdit.addTextChangedListener {
                viewModel.currentNote?.header?.title = it.toString()
            }
        }


//            holder.binding.firstNoteEdit.imeOptions = EditorInfo.IME_MASK_ACTION
//            holder.binding.firstNoteEdit.setRawInputType(InputType.TYPE_CLASS_TEXT)

        noteAdapter.firstNoteSimpleHolder.observe(viewLifecycleOwner, { holder ->
            noteAdapter.currentList[holder.absoluteAdapterPosition].firstNote?.text?.let {
                holder.binding.firstNoteEdit.setText(it)
            }

            holder.binding.firstNoteEdit.setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        noteAdapter.currentList[holder.absoluteAdapterPosition]?.firstNote?.let {
                            it.text = "${it.text}\n"
                        }
                        viewModel.insertNewFirstNote()
                        noteAdapter.notifyDataSetChanged()
                        true
                    }
                    else -> false
                }
            }


            holder.binding.firstNoteEdit.addTextChangedListener {
                noteAdapter.currentList[holder.absoluteAdapterPosition]?.firstNote?.text =
                    it.toString()
            }
        })


        noteAdapter.firstNoteTodoHolder.observe(viewLifecycleOwner, { holder ->
            noteAdapter.currentList[holder.absoluteAdapterPosition].firstNote?.also {
                crossText(it, holder.binding.editTextCheckbox)
                holder.binding.editTextCheckbox.setText(it.text)
                holder.binding.checkboxEdit.isChecked = it.isChecked

                holder.binding.checkboxEdit.setOnCheckedChangeListener { _, isChecked ->
                    it.isChecked = isChecked
                    viewModel.updateFirstNote(it)
                    crossText(it, holder.binding.editTextCheckbox)
                }
            }


            holder.binding.editTextCheckbox.addTextChangedListener {
                noteAdapter.currentList[holder.absoluteAdapterPosition]?.firstNote?.text =
                    it.toString()
            }

            holder.binding.editTextCheckbox.setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        viewModel.insertNewFirstNote()
                        true
                    }
                    else -> false
                }
            }

            holder.binding.deleteFirstNoteItemButton.setOnClickListener {
                viewModel.currentNote?.notes?.let { list ->
                    if (list.size > 1 && viewModel.deleteAllowed) {
                        noteAdapter.currentList[holder.absoluteAdapterPosition]?.firstNote?.let { firstNote ->
                            viewModel.deleteFirstNote(firstNote)
                        }
                    }
                }
            }
        })


        /**
         * LiveData for images
         * @see EditNoteAdapter.imgHolder
         *
         * set visibility and clickListeners when the
         * image is in normal state or in hidden state
         * @see com.example.noteexample.database.Image.hidden
         */

        noteAdapter.imgHolder.observe(viewLifecycleOwner, { holder ->

            //set state between normal and hidden state
            if (noteAdapter.currentList[holder.absoluteAdapterPosition].image?.hidden == true) {
                holder.binding.photo.visibility = View.GONE
                holder.binding.restoreButton.visibility = View.VISIBLE
                holder.binding.deleteCircleIcon.visibility = View.GONE
                holder.binding.deleteCircle.visibility = View.GONE
            } else {
                holder.binding.photo.visibility = View.VISIBLE
                holder.binding.restoreButton.visibility = View.GONE
                holder.binding.deleteCircleIcon.visibility = View.VISIBLE
                holder.binding.deleteCircle.visibility = View.VISIBLE
            }

            //save current signature
            holder.binding.noteEditTextFirst.addTextChangedListener { editable ->
                noteAdapter.currentList[holder.absoluteAdapterPosition].image?.let {
                    it.signature = editable.toString()
                    if (it.photoPath.isEmpty() && it.signature.isEmpty()) {
                        viewModel.deleteImage(it)
                    }
                }
            }

            /**
             * When delete circle is pressed, image disappears
             * and restore button becomes [View.VISIBLE]
             */
            holder.binding.deleteCircle.setOnClickListener {
                noteAdapter.currentList[holder.absoluteAdapterPosition].image?.hidden = true
                viewModel.updateCurrentNote()
                noteAdapter.notifyItemChanged(holder.absoluteAdapterPosition)
            }

            /**
             * Restore button retuns image in normal state
             */
            holder.binding.restoreButton.setOnClickListener {
                noteAdapter.currentList[holder.absoluteAdapterPosition].image?.hidden = false
                viewModel.updateCurrentNote()
                noteAdapter.notifyItemChanged(holder.absoluteAdapterPosition)
            }
        })


        /**
         * Back button triggers [EditNoteViewModel.navigateBack] LiveData
         */
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.onStartNavigating()
        }
        /**
         * [EditNoteViewModel.currentNoteLiveData] submits list for [EditNoteAdapter]
         *
         * It doesn't define new list each time to prevent blinks each time
         * [EditNoteViewModel.currentNoteLiveData] is observed
         */

        viewModel.currentNoteLiveData.observe(viewLifecycleOwner, {
            viewModel.currentNote = it
            lifecycleScope.launch(Dispatchers.Default) {
                if (!viewModel.itemListSame) {
                    viewModel.createNoteList()
                }
                withContext(Dispatchers.Main) {
                    noteAdapter.submitList(viewModel.noteList)
                }
            }
        })


        /**
         * [EditNoteViewModel.startNote] and [EditNoteViewModel.currentNote]
         * checks if note is changed comparable to start state
         */
        viewModel.navigateBack.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.updateCurrentNote()
                if (viewModel.backPressed) {
                    if (viewModel.checkStartNote()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage("Сохранить изменения?")
                            .setNegativeButton("Нет") { _, _ ->
                                finishAndNavigate(false) {
                                    if (args.noteID > -1) {
                                        viewModel.insertNoteWithImages(viewModel.startNote)
                                    }
                                    this@EditNoteFragment.findNavController().popBackStack()
                                }
                            }
                            .setPositiveButton("Да") { _, _ ->
                                finishAndNavigate(true) {
                                    this@EditNoteFragment.findNavController()
                                        .navigate(
                                            EditNoteFragmentDirections
                                                .actionEditNoteFragmentToAllNotesFragment()
                                        )
                                }
                            }.show()
                    } else {
                        finishAndNavigate(true) {
                            this@EditNoteFragment.findNavController().popBackStack()
                        }
                    }
                } else {
                    finishAndNavigate(true) {
                        this@EditNoteFragment.findNavController()
                            .navigate(
                                EditNoteFragmentDirections
                                    .actionEditNoteFragmentToAllNotesFragment()
                            )
                    }
                }
            }
        })
        return binding.root
    }

    /**
     * [EditNoteViewModel.updateCurrentNote] updates new note in case app is closed accidentally
     */
    override fun onPause() {
        super.onPause()
        if (args.noteID == -1L) {
            viewModel.updateCurrentNote()
        }
    }

    private fun initCameraAndRequest() {
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    viewModel.startNote?.header?.let { item ->
                        this@EditNoteFragment.findNavController()
                            .navigate(
                                EditNoteFragmentDirections
                                    .actionEditNoteFragmentToGalleryFragment(item.headerID)
                            )
                    }
                } else {
                    Snackbar.make(
                        binding.editRecycler,
                        R.string.camera_request_failed,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        //Launcher for camera itself
        startCamera =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    viewModel.insertCameraPhoto(camera.currentPhotoPath)
                    noteAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun finishAndNavigate(checkNoteBeforeDelete: Boolean, action: suspend () -> Unit) {
        lifecycleScope.launch {
            if (checkNoteBeforeDelete) {
                viewModel.checkEmptyNoteAndDelete()
            } else {
                viewModel.deleteCurrentNote()
            }
            action()
            viewModel.onDoneNavigating()
        }
    }


    private fun crossText(firstNote: FirstNote, edit: EditText) {
        if (firstNote.isChecked) {
            edit.paintFlags =
                edit.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            edit.paintFlags = edit.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}
