package com.bbt.ghostroom.activities.settings

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.bbt.ghostroom.R
import com.bbt.ghostroom.databinding.FragmentSettingsBinding
import com.bbt.ghostroom.extensions.await
import com.bbt.ghostroom.utils.FireConstants
import com.bbt.ghostroom.utils.IntentUtils
import com.bbt.ghostroom.utils.network.FireManager
import com.google.firebase.functions.FirebaseFunctions
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanOptions.QR_CODE


class SettingsFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding get() = _binding!!

    private lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        barcodeLauncher = registerForActivityResult(
            ScanContract()
        ) { result ->

            if (result.contents == null) {
                return@registerForActivityResult
            }
            val prefix = "fapreqid_"

            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.setMessage(getString(R.string.loading))
            progressDialog.show()
            val data = mapOf(Pair("requestId", result.contents))
            lifecycleScope.launchWhenStarted {
                try {
                    val decoded = String(Base64.decode(result.contents, Base64.DEFAULT))
                    if (!decoded.startsWith(prefix)) {
                        Toast.makeText(
                            requireActivity(),
                            R.string.invalid_qr_code,
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialog.dismiss()
                        return@launchWhenStarted
                    }
                    FirebaseFunctions.getInstance().getHttpsCallable("approveDesktopLogin")
                        .call(data).await()
                    progressDialog.dismiss()
                    AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
                        .setMessage(getString(R.string.please_continue_on_your_desktop_app))
                        .setPositiveButton(getString(R.string.ok), null).show()


                } catch (e: Exception) {
                    progressDialog.dismiss()
                    Toast.makeText(requireActivity(), R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvProfile.setOnClickListener(this)
        binding.tvNotifications.setOnClickListener(this)
        binding.tvSecurity.setOnClickListener(this)
        binding.tvChat.setOnClickListener(this)
        binding.tvPrivacyPolicy.setOnClickListener(this)
        binding.tvAbout.setOnClickListener(this)
        binding.tvDesktopLogin.setOnClickListener(this)
        binding.tvDeleteAccount.setOnClickListener(this)
    }

    override fun onClick(view: View) {

        when (view.id) {

            R.id.tv_profile -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_profilePreferenceFragment)

            R.id.tv_notifications -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_notificationPreferenceFragment)

            R.id.tv_security -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_securityPreferencesFragment)

            R.id.tv_chat -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_chatSettingsPreferenceFragment2)

            R.id.tv_privacy_policy -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)

            R.id.tv_about -> Navigation.findNavController(view)
                .navigate(R.id.action_settingsFragment_to_aboutFragment2)

            R.id.tv_desktop_login -> {
                barcodeLauncher.launch(ScanOptions().apply {
                    setDesiredBarcodeFormats(QR_CODE)
                    setBeepEnabled(false)
                })
            }

            R.id.tv_delete_account -> {
                AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
                    .setMessage(R.string.delete_account_confirmation)
                    .setTitle(R.string.pref_header_delete_account)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        val progressDialog = ProgressDialog(requireActivity())
                        progressDialog.setMessage(getString(R.string.loading))
                        progressDialog.show()

                        lifecycleScope.launchWhenStarted {
                            try {
                                FireConstants.mainRef.child("deleteUsersRequests")
                                    .child(FireManager.uid).setValue(true).await()
                                FireManager.logoutAndDelete()
                                LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(
                                    Intent().setAction(IntentUtils.ACTION_LOGOUT)
                                )
                                progressDialog.dismiss()

                            } catch (e: Exception) {
                                progressDialog.dismiss()
                                Toast.makeText(
                                    requireActivity(),
                                    R.string.error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .show()
            }


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}