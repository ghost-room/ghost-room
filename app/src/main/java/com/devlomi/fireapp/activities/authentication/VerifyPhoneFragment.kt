package com.bbt.ghostroom.activities.authentication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.navigation.Navigation
import com.bbt.ghostroom.R
import com.bbt.ghostroom.databinding.FragmentVerifyPhoneBinding
import com.bbt.ghostroom.utils.IntentUtils



class VerifyPhoneFragment : BaseAuthFragment() {
    private var _binding: FragmentVerifyPhoneBinding? = null
    private val binding: FragmentVerifyPhoneBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVerifyPhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(IntentUtils.PHONE)?.let { phone ->
            binding.tvOtpInfo.text = requireActivity().getString(R.string.enter_the_otp_sent_to, phone)
        }

        binding.etOtp.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 6) {
                binding.etOtp.onEditorAction(EditorInfo.IME_ACTION_DONE)
                completeRegistration()
            }
        }


        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {

                    AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
                        setMessage(R.string.cancel_verification_confirmation_message)
                        setNegativeButton(R.string.no, null)
                        setPositiveButton(R.string.yes) { _, _ ->
                            callbacks?.cancelVerificationRequest()
                            Navigation.findNavController(binding.etOtp).navigateUp()
                        }
                        show()
                    }

                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


    }

    private fun completeRegistration() {
        callbacks?.verifyCode(binding.etOtp.text.toString())
    }

    override fun enableViews() {
        super.enableViews()
        binding.etOtp.isEnabled = true
    }

    override fun disableViews() {
        super.disableViews()
        binding.etOtp.isEnabled = false

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}