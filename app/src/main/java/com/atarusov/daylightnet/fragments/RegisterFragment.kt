package com.atarusov.daylightnet.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.atarusov.appComponent
import com.atarusov.daylightnet.R
import com.atarusov.daylightnet.databinding.FragmentRegisterBinding
import com.atarusov.daylightnet.model.User
import com.atarusov.daylightnet.viewmodels.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    @Inject
    lateinit var factory: RegisterViewModel.Factory
    private val viewModel: RegisterViewModel by viewModels { factory }

    override fun onAttach(context: Context) {
        context.appComponent.inject(this)
        super.onAttach(context)
    }

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

        lifecycleScope.launch {
            viewModel.authErrorSharedFlow.collect { e ->
                val error_message: String
                if (e is FirebaseAuthException && e.errorCode == "ERROR_EMAIL_ALREADY_IN_USE")
                    error_message = getString(R.string.error_email_already_in_use)
                else if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.CANCELLED)
                    error_message = getString(R.string.error_request_was_cancelled)
                else if (e is FirebaseTooManyRequestsException)
                    error_message = getString(R.string.error_too_many_requests)
                else if (e is FirebaseNetworkException)
                    error_message = getString(R.string.error_network_request_failed)
                else error_message = getString(R.string.error_unexpected)
                Snackbar.make(requireView(), error_message, Snackbar.LENGTH_SHORT).show()
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
}