package com.atarusov.daylightnet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.atarusov.daylightnet.R
import com.atarusov.daylightnet.databinding.FragmentRegisterBinding
import com.atarusov.daylightnet.model.User
import com.atarusov.daylightnet.viewmodels.RegisterViewModel
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        binding.arrowBackIc.setOnClickListener {
            viewModel.navigateBack()
        }

        binding.btnSignUp.setOnClickListener {
            viewModel.signUpWithEmailAndPassword(collectUserDataFromInputFields())
            clearFocusOnAllInputFields()
        }

        lifecycleScope.launch {
            viewModel.navigationEvent.collect { navigationEvent ->
                when (navigationEvent) {
                    RegisterViewModel.NavigationEvent.NavigateToBottomNavigationScreens ->
                        findNavController().navigate(R.id.bottomNavFragments)

                    RegisterViewModel.NavigationEvent.NavigateBack ->
                        findNavController().navigateUp()
                }
            }
        }
        // TODO: оптимизировать
        lifecycleScope.launch {
            viewModel.validationStateFlow.collect { validationState ->
                binding.inputLayoutFirstName.error =
                    if (validationState.isFirstNameValid) null else getString(R.string.tif_first_name_error)
                binding.inputLayoutLastName.error =
                    if (validationState.isLastNameValid) null else getString(R.string.tif_last_name_error)
                binding.inputLayoutEmail.error =
                    if (validationState.isEmailValid) null else getString(R.string.tif_email_error)
                binding.inputLayoutEmail.error =
                    if (validationState.isEmailValid) null else getString(R.string.tif_email_error)
                binding.inputLayoutPassword.error =
                    if (validationState.isPasswordValid) null else getString(R.string.tif_password_error)
                binding.inputLayoutRepeatPassword.error =
                    if (validationState.isRepeatPasswordValid) null else getString(R.string.tif_repeat_password_error)
            }
        }

        setOnFocusChangeListenerToAllFields()

        return binding.root
    }

    fun collectUserDataFromInputFields(): User.RegistrationData {
        val firstName = binding.textInputFirstName.text.toString()
        val lastName = binding.textInputLastName.text.toString()
        val email = binding.textInputEmail.text.toString()
        val password = binding.textInputPassword.text.toString()
        val repeatPassword = binding.textInputRepeatPassword.text.toString()

        return User.RegistrationData(
            firstName, lastName, email, password, repeatPassword
        )
    }

    fun setOnFocusChangeListenerToAllFields() {
        binding.inputFieldsLayout.children.forEach {
            (it as TextInputLayout).editText?.setOnFocusChangeListener { view, b ->
                when (it.editText?.id) {
                    R.id.text_input_first_name -> viewModel.hideFirstNameError()
                    R.id.text_input_last_name -> viewModel.hideLastNameError()
                    R.id.text_input_email -> viewModel.hideEmailError()
                    R.id.text_input_password -> viewModel.hidePasswordError()
                    R.id.text_input_repeat_password -> viewModel.hideRepeatPasswordError()
                }
            }
        }
    }

    fun clearFocusOnAllInputFields() {
        binding.textInputFirstName.clearFocus()
        binding.textInputLastName.clearFocus()
        binding.textInputEmail.clearFocus()
        binding.textInputPassword.clearFocus()
        binding.textInputRepeatPassword.clearFocus()
    }

    companion object {
        val TAG = "RegisterFragment"
        fun newInstance() = RegisterFragment()
    }
}