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
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
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
            NewPostDialogFragment { text ->
                viewModel.addPost(text)
            }.show(parentFragmentManager, "NEW_POST_DIALOG")
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadAndShowPostCards(true) {
                binding.swipeRefreshLayout.isRefreshing = false
            }

        }

        lifecycleScope.launch {
            viewModel.uiState.collect() { state ->
                when (state) {
                    is HomeViewModel.UiState.Loading -> setLoadingState()
                    is HomeViewModel.UiState.ShowingNoPostsMessage -> setShowingNoPostsMessageState()
                    is HomeViewModel.UiState.ShowingPostCards -> setShowingPostsState(state.postCards)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.scrollUpEvent.collect() {
                binding.postsRw.smoothScrollToPosition(0)
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

    private fun setLoadingState() {
        binding.loadingAnim.visibility = View.VISIBLE
        binding.loadingAnim.setMinFrame(0)
        binding.loadingAnim.setMaxFrame(102)
        binding.loadingAnim.playAnimation()

        binding.postsRw.visibility = View.GONE
        binding.addPostBtn.visibility = View.GONE
    }

    private fun setShowingNoPostsMessageState() {
        binding.noPostsTv.visibility = View.VISIBLE
        binding.addPostBtn.visibility = View.VISIBLE

        binding.postsRw.visibility = View.GONE
        binding.loadingAnim.visibility = View.GONE
        binding.loadingAnim.pauseAnimation()
    }

    private fun setShowingPostsState(postCards: List<PostCard>) {
        (binding.postsRw.adapter as PostsAdapter).postCards = postCards
        binding.postsRw.visibility = View.VISIBLE
        binding.addPostBtn.visibility = View.VISIBLE

        binding.noPostsTv.visibility = View.GONE
        binding.loadingAnim.visibility = View.GONE
        binding.loadingAnim.pauseAnimation()
    }
}