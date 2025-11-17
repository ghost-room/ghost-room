package com.bbt.ghostroom.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.bbt.ghostroom.R
import com.bbt.ghostroom.utils.SharedPreferencesManager
import com.bbt.ghostroom.utils.enc.ethree.EthreeInstance
import com.bbt.ghostroom.utils.enc.ethree.EthreeRegistration
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class SaveE2EActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_e2_e)
        lifecycleScope.launchWhenCreated {
            try {
                withContext(IO) {
                    val ethree = EthreeInstance.initialize().await()
                    EthreeRegistration.registerEthree(ethree, this)
                }

                SharedPreferencesManager.setE2ESaved(true)
                startActivity(Intent(this@SaveE2EActivity, SplashActivity::class.java))
                finish()

            } catch (e: Exception) {
                withContext(Main) {
                    AlertDialog.Builder(this@SaveE2EActivity, R.style.AlertDialogTheme).apply {
                        setMessage(R.string.unknown_error)
                            .setPositiveButton(R.string.ok, null)
                            .show()
                    }
                }
            }
        }
    }
}