package com.atarusov.daylightnet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.atarusov.daylightnet.R
import com.atarusov.daylightnet.databinding.FragmentProfileBinding
import com.atarusov.daylightnet.viewmodels.ProfileViewModel
import com.atarusov.daylightnet.viewmodels.RegisterViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = Firebase.auth

        binding.btnLogOut.setOnClickListener {
            viewModel.signOut()
        }

        lifecycleScope.launch {
            viewModel.navigationEvent.collect { navigationEvent ->
                when (navigationEvent) {
                    ProfileViewModel.NavigationEvent.NavigateToLoginScreen ->
                        findNavController().navigate(R.id.loginFragment)

                    ProfileViewModel.NavigationEvent.NavigateBack ->
                        findNavController().navigateUp()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.currentUserData.collect { currentUser ->
                if (currentUser != null) {
                    binding.userNameTv.text = getString(
                        R.string.profile_username,
                        currentUser.firstName,
                        currentUser.lastName
                    )
                    if (currentUser.additionalInfo != null)
                        binding.userAdditionalInfoTv.text = currentUser.additionalInfo
                }
            }
        }

        return binding.root
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}