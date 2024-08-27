package com.atarusov.daylightnet.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.atarusov.daylightnet.MainActivity
import com.atarusov.daylightnet.viewmodels.HomeViewModel
import com.atarusov.daylightnet.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}