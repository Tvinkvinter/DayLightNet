package com.atarusov.daylightnet.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.atarusov.daylightnet.databinding.FragmentDialogNewPostBinding

class NewPostDialogFragment(val onClickPublish: (text: String) -> Unit) : DialogFragment() {
    private var _binding: FragmentDialogNewPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogNewPostBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }

        binding.publishBtn.setOnClickListener {
            onClickPublish(binding.postContentEt.text.toString())
            dismiss()
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}