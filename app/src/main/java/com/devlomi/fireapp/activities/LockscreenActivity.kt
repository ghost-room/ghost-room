package com.bbt.ghostroom.activities

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bbt.ghostroom.R
import com.bbt.ghostroom.databinding.ActivityLockscreenBinding
import com.bbt.ghostroom.utils.SharedPreferencesManager
import com.bbt.ghostroom.utils.biometricks.BiometricException
import com.bbt.ghostroom.utils.biometricks.BiometricPromptInfo
import com.bbt.ghostroom.utils.biometricks.Biometricks
import com.bbt.ghostroom.utils.biometricks.Crypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.InvalidAlgorithmParameterException


class LockscreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLockscreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRetry.setOnClickListener {
            showBiometricPrompt()
        }

        onBackPressedDispatcher.addCallback(this){
            //Prevent exiting
        }
    }

    override fun onResume() {
        super.onResume()
        showBiometricPrompt()
    }

    private fun showBiometricPrompt() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }


        val biometricks = Biometricks.from(applicationContext)

        if (biometricks !is Biometricks.Available) {
            val string = getString(R.string.biometrics_not_available)
            binding.tvUnlockText.text = string
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
            return
        }

        val biometricName = when (biometricks) {
            Biometricks.Available.Face -> getString(R.string.face)
            Biometricks.Available.Fingerprint -> getString(R.string.fingerprint)
            Biometricks.Available.Iris -> getString(R.string.iris)
            Biometricks.Available.Unknown,
            Biometricks.Available.Multiple -> getString(R.string.biometric)
        }

        binding.btnRetry.isVisible = false



        lifecycleScope.launch {
            try {

                val cryptoObject = withContext(Dispatchers.IO) {
                    Crypto().cryptoObject()
                }


                Biometricks.showPrompt(
                    this@LockscreenActivity,
                    BiometricPromptInfo(
                        title = getString(R.string.authenticate_with, biometricName),
                        negativeButtonText = getString(R.string.cancel),
                        cryptoObject = cryptoObject
                    )
                ) { showLoading ->

                    binding.progressBar.isVisible = showLoading


                }

                SharedPreferencesManager.setLastActive(System.currentTimeMillis())
                finish()

            } catch (e: Exception) {
                if (e is BiometricException) {

                    if (e.code == BiometricPrompt.ERROR_CANCELED || e.code == BiometricPrompt.ERROR_USER_CANCELED || e.code == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        binding.btnRetry.isVisible = true
                    } else {
                        binding.btnRetry.isVisible = false
                    }

                    binding.imgUnlockIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                    binding.tvUnlockText.text = e.errString
                } else if (e is InvalidAlgorithmParameterException) {
                    SharedPreferencesManager.setFingerprintLock(false)
                    finish()

                }
            }
        }
    }



}