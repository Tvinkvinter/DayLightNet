package com.atarusov.daylightnet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.atarusov.daylightnet.R
import com.atarusov.daylightnet.databinding.FragmentLoginBinding
import com.atarusov.daylightnet.viewmodels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.btnLogIn.setOnClickListener {
            val email = binding.textInputEmail.text.toString()
            val password = binding.textInputPassword.text.toString()
            viewModel.signInWithEmailAndPassword(email, password)
            binding.textInputEmail.clearFocus()
            binding.textInputPassword.clearFocus()
        }

        binding.tvSignUpLink.setOnClickListener {
            viewModel.navigateToRegisterScreen()
        }

        lifecycleScope.launch {
            viewModel.navigationEvent.collect { navigationEvent ->
                when(navigationEvent){
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
            viewModel.authErrorSharedFlow.collect {
                if (it == true) Snackbar.make(
                    requireView(),
                    getString(R.string.login_sign_in_snackbar_error),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }


        return binding.root
    }

    companion object {
        val TAG = "LoginFragment"
        fun newInstance() = LoginFragment()
    }
}