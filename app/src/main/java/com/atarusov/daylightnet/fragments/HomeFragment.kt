package com.atarusov.daylightnet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.atarusov.daylightnet.adapters.PostsAdapter
import com.atarusov.daylightnet.databinding.FragmentHomeBinding
import com.atarusov.daylightnet.model.Post
import com.atarusov.daylightnet.viewmodels.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.postsRw.adapter = PostsAdapter(requireContext()) { post: Post ->
            viewModel.handleLikeButtonClick(post)
        }
        binding.postsRw.layoutManager = LinearLayoutManager(requireContext())

        binding.addPostBtn.setOnClickListener {
            viewModel.addTestPost()
        }

        lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                (binding.postsRw.adapter as PostsAdapter).posts = posts
            }
        }

        return binding.root
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}