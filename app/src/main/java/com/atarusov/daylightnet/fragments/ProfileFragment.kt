package com.atarusov.daylightnet.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.atarusov.appComponent
import com.atarusov.daylightnet.R
import com.atarusov.daylightnet.databinding.FragmentProfileBinding
import com.atarusov.daylightnet.model.User
import com.atarusov.daylightnet.viewmodels.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    @Inject
    lateinit var factory: ProfileViewModel.Factory
    private val viewModel: ProfileViewModel by viewModels { factory }

    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogOut.setOnClickListener {
            viewModel.signOut()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentUserData.collect { userData ->
                        userData?.let { setViewsByCurrentUserData(it) }
                    }
                }

                launch {
                    viewModel.navigationEvent.collect { navigationEvent ->
                        when (navigationEvent) {
                            ProfileViewModel.NavigationEvent.NavigateToLoginScreen ->
                                findNavController().navigate(R.id.loginFragment)

                            ProfileViewModel.NavigationEvent.NavigateBack ->
                                findNavController().navigateUp()
                        }
                    }
                }

                launch {
                    viewModel.signOutErrorSharedFlow.collect { e ->
                        val errorMessage: String =
                            when {
                                e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.CANCELLED ->
                                    getString(R.string.error_request_was_cancelled)

                                e is FirebaseTooManyRequestsException ->
                                    getString(R.string.error_too_many_requests)

                                e is FirebaseNetworkException ->
                                    getString(R.string.error_network_request_failed)

                                else -> getString(R.string.error_unexpected)
                            }
                        Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setViewsByCurrentUserData(user: User) {
        binding.userNameTv.text =
            getString(R.string.profile_username, user.firstName, user.lastName)
        if (user.additionalInfo != null)
            binding.userAdditionalInfoTv.text = user.additionalInfo
    }
}