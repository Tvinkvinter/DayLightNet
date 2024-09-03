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
import com.atarusov.daylightnet.model.User
import com.atarusov.daylightnet.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels { ProfileViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)


        binding.btnLogOut.setOnClickListener {
            viewModel.signOut()
        }

        lifecycleScope.launch {
            viewModel.currentUserData.collect { userData ->
                userData?.let { setViewsByCurrentUserData(it) }
            }
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

        return binding.root
    }

    fun setViewsByCurrentUserData(user: User) {
        with(binding) {
            binding.userNameTv.text =
                getString(R.string.profile_username, user.firstName, user.lastName)

            if (user.additionalInfo != null)
                binding.userAdditionalInfoTv.text = user.additionalInfo
        }
    }
}