package com.atarusov.daylightnet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.atarusov.daylightnet.R
import com.atarusov.daylightnet.adapters.PostsAdapter
import com.atarusov.daylightnet.databinding.FragmentHomeBinding
import com.atarusov.daylightnet.model.PostCard
import com.atarusov.daylightnet.viewmodels.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.postsRw.adapter = PostsAdapter(requireContext()) { postCard: PostCard ->
            viewModel.handleLikeButtonClick(postCard)
        }
        binding.postsRw.layoutManager = LinearLayoutManager(requireContext())

        binding.addPostBtn.setOnClickListener {
            viewModel.addTestPost()
        }

        lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                (binding.postsRw.adapter as PostsAdapter).posts = posts
            viewModel.postCards.collectLatest { postCards ->
                (binding.postsRw.adapter as PostsAdapter).postCards = postCards
            }
        }

        lifecycleScope.launch {
            viewModel.errorSharedFlow.collect { e ->
                val error_message: String
                if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.CANCELLED)
                    error_message = getString(R.string.error_request_was_cancelled)
                else if (e is FirebaseTooManyRequestsException)
                    error_message = getString(R.string.error_too_many_requests)
                else if (e is FirebaseNetworkException)
                    error_message = getString(R.string.error_network_request_failed)
                else error_message = getString(R.string.error_unexpected)
                Snackbar.make(requireView(), error_message, Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}