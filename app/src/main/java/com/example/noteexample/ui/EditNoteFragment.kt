package com.example.noteexample.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.adapters.EditSimpleNoteAdapter
import com.example.noteexample.adapters.NoteWithImagesRecyclerItems
import com.example.noteexample.databinding.FragmentEditNoteBinding
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.Camera
import com.example.noteexample.viewmodels.EditNoteViewModel
import com.example.noteexample.viewmodels.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var startCamera: ActivityResultLauncher<Intent>
    private val args by navArgs<EditNoteFragmentArgs>()

    private val viewModel by viewModels<EditNoteViewModel> {
        NoteViewModelFactory(args.noteID, repository)
    }


    val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or
                ItemTouchHelper.DOWN or ItemTouchHelper.UP,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.absoluteAdapterPosition - 1
            val to = target.absoluteAdapterPosition - 1

            if (from >= 0 && to >= 0) {
                viewModel.swap(from, to)
                recyclerView.adapter?.notifyItemMoved(from + 1, to + 1)
            }
            return true
        }

        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            super.clearView(recyclerView, viewHolder)
            viewModel.updateCurrentNote()
        }

        override fun onSwiped(
            viewHolder: RecyclerView.ViewHolder,
            direction: Int
        ) {
        }

    })


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

        val noteAdapter = EditSimpleNoteAdapter()
        binding.editRecycler.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
        }
        helper.attachToRecyclerView(binding.editRecycler)


        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    viewModel.startNote?.header?.let { item ->
                        this@EditNoteFragment.findNavController()
                            .navigate(
                                EditNoteFragmentDirections
                                    .actionEditNoteFragmentToGalleryFragment(item.noteID)
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

        //navIcon clickListener
        binding.toolbarNoteEdit.setNavigationOnClickListener {
            viewModel.backPressed = true
            viewModel.onStartNavigating()
        }


        //menuClickListener
        binding.toolbarNoteEdit.setOnMenuItemClickListener {

//            val newIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back_arrow)
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
                                                            item.noteID
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
                    if (viewModel.todoList) {
                        it.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_todo_list)
                        viewModel.todoList = false
                    } else {
                        it.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_simple_list)
                        viewModel.todoList = true
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
         * @see EditSimpleNoteAdapter.headerHolder
         *
         * TextChangeListener write title and text values and updates them when exit
         * or accidentally close app if this is note is new
         * @see onPause
         */
        noteAdapter.headerHolder.observe(viewLifecycleOwner) { holder ->
            holder.binding.titleEdit.addTextChangedListener {
                viewModel.currentNote?.header?.title = it.toString()
            }
            holder.binding.firstNoteEdit.addTextChangedListener {
                viewModel.currentNote?.header?.text = it.toString()
            }
        }

        /**
         * LiveData for images
         * @see EditSimpleNoteAdapter.imgHolder
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
         * [EditNoteViewModel.currentNoteLiveData] submits list for [EditSimpleNoteAdapter]
         *
         * It doesn't define new list each time to prevent blinks each time
         * [EditNoteViewModel.currentNoteLiveData] is observed
         */
        viewModel.currentNoteLiveData.observe(viewLifecycleOwner, {
            viewModel.currentNote = it
            lifecycleScope.launch(Dispatchers.Default) {
                it?.let { note ->
                    if (!viewModel.itemListSame) {
                        viewModel.noteList = mutableListOf()
                        viewModel.noteList.add(0, NoteWithImagesRecyclerItems(note.header))
                        note.images.forEach { image ->
                            viewModel.noteList.add(NoteWithImagesRecyclerItems(image = image))
                        }
                    } else {
                        viewModel.noteList.forEachIndexed { index, item ->
                            if (index > 0) {
                                item.image = note.images[index - 1]
                            }
                        }
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
                        it.header.text.isEmpty() &&
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
                                viewModel.startNote?.header?.text != noteWithImages.header.text ||
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
