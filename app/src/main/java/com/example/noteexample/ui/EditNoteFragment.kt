package com.example.noteexample.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
import com.example.noteexample.adapters.NoteWithImagesRecyclerItems
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

    @Inject
    lateinit var camera: Camera

    @Inject
    lateinit var repository: NoteRepository

    private val noteAdapter = EditNoteAdapter()
    private val itemHelperCallback = ItemHelperCallback()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var startCamera: ActivityResultLauncher<Intent>
    private val args by navArgs<EditNoteFragmentArgs>()

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

        val binding: FragmentEditNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_note, container, false)
        binding.lifecycleOwner = this

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


        binding.editRecycler.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
        }
        imgHelper.attachToRecyclerView(binding.editRecycler)

        //navIcon clickListener
        binding.toolbarNoteEdit.setNavigationOnClickListener {
            viewModel.backPressed = true
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
                    viewModel.currentNote?.header?.let { header ->
                        lifecycleScope.launch {
                            if (header.todoList) {
                                it.icon = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_todo_list
                                )
                                header.todoList = false
                                viewModel.updateCurrentNoteSuspend()
                                binding.editRecycler.adapter = noteAdapter
                            } else {
                                it.icon = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_simple_list
                                )
                                header.todoList = true
                                viewModel.updateCurrentNoteSuspend()
                                binding.editRecycler.adapter = noteAdapter
                            }
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

        noteAdapter.headerHolder.observe(viewLifecycleOwner) { holder ->
            holder.binding.titleEdit.addTextChangedListener {
                viewModel.currentNote?.header?.title = it.toString()
            }
        }


//            holder.binding.firstNoteEdit.imeOptions = EditorInfo.IME_MASK_ACTION
//            holder.binding.firstNoteEdit.setRawInputType(InputType.TYPE_CLASS_TEXT)

        noteAdapter.firstNoteSimpleHolder.observe(viewLifecycleOwner, { holder ->
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
            noteAdapter.currentList[holder.absoluteAdapterPosition].firstNote?.text?.let {
                holder.binding.editTextCheckbox.setText(it)
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
                    if (list.size > 1) {
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
            viewModel.backPressed = true
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
                viewModel.currentNote?.let { item ->
                    if (!viewModel.itemListSame) {
                        Log.e("size", viewModel.noteList.size.toString())
                        viewModel.noteList = mutableListOf()
                        viewModel.noteList.add(0, NoteWithImagesRecyclerItems(item.header))
                        val size = item.notes.size + item.images.size

                        for (i in 0 until size) {
                            item.notes.forEach { note ->
                                if (note.notePos == i) {
                                    viewModel.noteList.add(
                                        NoteWithImagesRecyclerItems(
                                            firstNote = note
                                        )
                                    )
                                }
                            }
                            item.images.forEach { image ->
                                if (image.imgPos == i) {
                                    viewModel.noteList.add(
                                        NoteWithImagesRecyclerItems(
                                            image = image
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        viewModel.itemListSame = false
                    }
                    withContext(Dispatchers.Main) {
                        noteAdapter.submitList(viewModel.noteList)
                    }
                }
            }
        })



        //Inner function to reduce repetitive code
        fun checkAndNavigate() {
            lifecycleScope.launch {
                viewModel.currentNote?.let {
                    if (it.images.isEmpty() &&
                        it.header.title.isEmpty() &&
                        it.notes.isEmpty() &&
                        viewModel.allHidden
                    ) {
                        viewModel.deleteUnused()
                    }
                }
                this@EditNoteFragment.findNavController()
                    .navigate(
                        EditNoteFragmentDirections.actionEditNoteFragmentToAllNotesFragment()
                    )
                viewModel.onDoneNavigating()
            }
        }


        /**
         * [EditNoteViewModel.startNote] and [EditNoteViewModel.currentNote]
         * checks if note is changed comparable to start state
         */
        viewModel.navigateBack.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.updateCurrentNote()
                viewModel.currentNote?.let { noteWithImages ->
                    if (noteWithImages.images.any { item -> !item.hidden } ||
                        noteWithImages.images.any { item -> item.signature.isNotEmpty() }) {
                        viewModel.allHidden = false
                    }
                    when {
                        viewModel.backPressed -> {
                            if (viewModel.startNote?.header?.title != noteWithImages.header.title ||
                                viewModel.startNote?.notes != noteWithImages.notes ||
                                viewModel.startNote?.images != noteWithImages.images
                            ) {
                                MaterialAlertDialogBuilder(requireContext())
                                    .setMessage("Сохранить изменения?")
                                    .setNegativeButton("Нет") { _, _ ->
                                        lifecycleScope.launch {
                                            if (args.noteID > -1) {
                                                viewModel.deleteNoteWithImages(noteWithImages)
                                                viewModel.insertNoteWithImages(viewModel.startNote)
                                            } else {
                                                viewModel.deleteUnused()
                                            }
                                            this@EditNoteFragment.findNavController().popBackStack()
                                        }
                                    }
                                    .setPositiveButton("Да") { _, _ ->
                                        checkAndNavigate()
                                    }.show()
                                viewModel.onDoneNavigating()
                            } else {
                                this.findNavController().popBackStack()
                                viewModel.onDoneNavigating()
                            }
                        }
                        !viewModel.backPressed -> {
                            checkAndNavigate()
                        }
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
}
