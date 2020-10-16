package com.example.noteexample.oneNote

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentOneNoteBinding

class OneNoteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentOneNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_one_note, container, false)

        // Inflate the layout for this fragment
        binding.lifecycleOwner = this
        return binding.root
    }
}