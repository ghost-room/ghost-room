package com.bbt.ghostroom.activities.authentication

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.bbt.ghostroom.R
import com.bbt.ghostroom.databinding.FragmentEnterPhoneNumberBinding
import com.bbt.ghostroom.utils.MyApp
import com.bbt.ghostroom.utils.NetworkHelper
import com.bbt.ghostroom.utils.Util

class EnterPhoneNumberFragment : BaseAuthFragment() {

    private var _binding: FragmentEnterPhoneNumberBinding? = null
    private val binding: FragmentEnterPhoneNumberBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEnterPhoneNumberBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cp.setDefaultCountryUsingNameCode("US")
        binding.cp.detectSIMCountry(true)




        binding.btnVerify.setOnClickListener {
            val number = binding.etNumber.text.toString().trim()
            val fullNumber = binding.cp.selectedCountryCodeWithPlus + number


            //dismiss keyboard
            binding.etNumber.onEditorAction(EditorInfo.IME_ACTION_DONE)


            AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
                val message = requireActivity().getString(
                    R.string.enter_phone_confirmation_message,
                    fullNumber
                )
                setMessage(message)
                setNegativeButton(R.string.edit, null)
                setPositiveButton(R.string.ok) { _, _ ->
                    //check for internet connection
                    if (NetworkHelper.isConnected(MyApp.context())) {

                        if (TextUtils.isEmpty(binding.etNumber.text) || TextUtils.isDigitsOnly(binding.etNumber.text)
                                .not()
                        )
                            Util.showSnackbar(
                                requireActivity(),
                                requireActivity().getString(R.string.enter_correct_number)
                            )
                        else {
                            callbacks?.verifyPhoneNumber(number, binding.cp.selectedCountryNameCode)
                        }

                    } else {
                        Util.showSnackbar(
                            requireActivity(),
                            requireActivity().getString(R.string.no_internet_connection)
                        )
                    }
                }

                show()
            }
        }

    }

    override fun enableViews() {
        super.enableViews()
        binding.etNumber.isEnabled = true
        binding.btnVerify.isEnabled = true
    }

    override fun disableViews() {
        super.disableViews()
        binding.etNumber.isEnabled = false
        binding.btnVerify.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}