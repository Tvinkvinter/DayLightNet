package com.atarusov.daylightnet.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.atarusov.appComponent
import com.atarusov.daylightnet.R
import com.atarusov.daylightnet.databinding.FragmentLoginBinding
import com.atarusov.daylightnet.model.User
import com.atarusov.daylightnet.viewmodels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    @Inject
    lateinit var factory: LoginViewModel.Factory
    private val viewModel: LoginViewModel by viewModels { factory }

    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btnLogIn.setOnClickListener {
            val email = binding.textInputEmail.text.toString()
            val password = binding.textInputPassword.text.toString()
            viewModel.signInWithEmailAndPassword(User.LoginData(email, password))
            binding.textInputEmail.clearFocus()
            binding.textInputPassword.clearFocus()
        }

        binding.tvSignUpLink.setOnClickListener {
            viewModel.navigateToRegisterScreen()
        }

        lifecycleScope.launch {
            viewModel.navigationEvent.collect { navigationEvent ->
                when (navigationEvent) {
                    LoginViewModel.NavigationEvent.NavigateToRegisterScreen ->
                        findNavController().navigate(R.id.registerFragment)

                    LoginViewModel.NavigationEvent.NavigateToBottomNavigationScreens ->
                        findNavController().navigate(R.id.bottomNavFragments)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.validationStateFlow.collect { validationState ->
                binding.inputLayoutEmail.error =
                    if (validationState.isEmailValid) null else getString(R.string.tif_email_error)
                binding.inputLayoutPassword.error =
                    if (validationState.isPasswordValid) null else getString(R.string.tif_password_error)
            }
        }

        binding.textInputEmail.setOnFocusChangeListener { _, focused ->
            if (focused) viewModel.hideEmailError()
        }

        binding.textInputPassword.setOnFocusChangeListener { _, focused ->
            if (focused) viewModel.hidePasswordError()
        }

        lifecycleScope.launch {
            viewModel.authErrorSharedFlow.collect { e ->
                val error_message: String
                if (e is FirebaseAuthException)
                    when (e.errorCode) {
                        "ERROR_INVALID_CREDENTIAL" ->
                            error_message = getString(R.string.error_invalid_credential)

                        "ERROR_USER_DISABLED" ->
                            error_message = getString(R.string.error_user_disabled)

                        else -> error_message = getString(R.string.error_unexpected)
                    }
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
}