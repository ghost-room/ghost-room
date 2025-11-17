package com.bbt.ghostroom.activities.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bbt.ghostroom.R
import com.bbt.ghostroom.databinding.FragmentDesktopLoginBinding


class DesktopLoginFragment : Fragment() {

    private var _binding: FragmentDesktopLoginBinding? = null
    private val binding: FragmentDesktopLoginBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDesktopLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}