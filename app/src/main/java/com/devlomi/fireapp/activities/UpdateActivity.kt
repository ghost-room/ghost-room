package com.bbt.ghostroom.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bbt.ghostroom.R
import com.bbt.ghostroom.activities.main.MainActivity
import com.bbt.ghostroom.databinding.ActivityUpdateBinding
import com.bbt.ghostroom.events.ExitUpdateActivityEvent
import com.bbt.ghostroom.utils.IntentUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class UpdateActivity : AppCompatActivity() {
    private lateinit var binding:ActivityUpdateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUpdate.setOnClickListener {
            try {
                startActivity(IntentUtils.getOpenWebsiteIntent(getString(R.string.update_app_link)))
            } catch (e: Exception) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun exitActivity( data: ExitUpdateActivityEvent){
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        finish()
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }
}