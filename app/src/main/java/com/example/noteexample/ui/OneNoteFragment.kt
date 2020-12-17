package com.example.noteexample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentOneNoteBinding
import com.example.noteexample.viewmodels.OneNoteViewModel
import com.example.noteexample.adapters.NoteWithImagesRecyclerItems
import com.example.noteexample.adapters.ViewSimpleNoteAdapter
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.viewmodels.NoteViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class OneNoteFragment : Fragment() {

    @Inject
    lateinit var repository: NoteRepository
    lateinit var binding: FragmentOneNoteBinding
    private val args by navArgs<OneNoteFragmentArgs>()
    private val oneNoteAdapter = ViewSimpleNoteAdapter()
    private val viewModel: OneNoteViewModel by viewModels {
        NoteViewModelFactory(args.noteID, repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        /**
         * Binding for OneNoteFragment
         * @see R.layout.fragment_one_note
         */

        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_one_note, container, false)
        binding.lifecycleOwner = this

        //Adapter options

        binding.recyclerOneNote.apply {
            adapter = oneNoteAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.currentNoteLiveData.observe(viewLifecycleOwner, { current ->
            lifecycleScope.launch(Dispatchers.Default) {
                if (viewModel.dataItemList.isEmpty()) {
                    viewModel.dataItemList.add(0, NoteWithImagesRecyclerItems(current.header))
                    current.images.forEach { image ->
                        viewModel.dataItemList.add(NoteWithImagesRecyclerItems(image = image))
                    }
                    withContext(Dispatchers.Main) {
                        oneNoteAdapter.submitList(viewModel.dataItemList)
                    }
                }
                /**
                 * Scroll to specific position when user returns from [OnePhotoFragment]
                 * by default it gets to 0, so user can have bad experience
                 */

                withContext(Dispatchers.Main) {
                    delay(16)
                    binding.recyclerOneNote.scrollToPosition(viewModel.scrollPosition)
                }
            }
        })

        /**
         * goto [OnePhotoFragment] with specific photo
         */

        oneNoteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->
            holder.binding.photoOneNote.setOnClickListener {
                viewModel.scrollPosition = holder.adapterPosition
                oneNoteAdapter.currentList[holder.adapterPosition].image?.let {
                    this.findNavController()
                        .navigate(
                            OneNoteFragmentDirections.actionOneNoteFragmentToOnePhotoFragment(
                                args.noteID,
                                viewModel.scrollPosition - 1
                            )
                        )
                }
            }
        })

        /**
         * NavIcon clickListener
         */
        binding.toolbarOneNote.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }

        /**
         * Menu clickListener
         */
        binding.toolbarOneNote.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_item -> {
                    this.findNavController()
                        .navigate(
                            OneNoteFragmentDirections.actionOneNoteFragmentToEditNoteFragment(args.noteID)
                        )
                    true
                }
                else -> false
            }
        }

        return binding.root
    }
}