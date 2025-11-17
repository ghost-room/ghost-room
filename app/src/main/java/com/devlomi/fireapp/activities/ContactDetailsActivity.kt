package com.bbt.ghostroom.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbt.ghostroom.R
import com.bbt.ghostroom.activities.main.messaging.ChatActivity
import com.bbt.ghostroom.adapters.ContactDetailsAdapter
import com.bbt.ghostroom.common.extensions.setInsetPadding
import com.bbt.ghostroom.databinding.ActivityContactDetailsBinding
import com.bbt.ghostroom.model.realms.User
import com.bbt.ghostroom.utils.ClipboardUtil
import com.bbt.ghostroom.utils.IntentUtils
import com.bbt.ghostroom.utils.NetworkHelper
import com.bbt.ghostroom.utils.RealmHelper
import com.bbt.ghostroom.utils.SnackbarUtil
import com.google.android.material.snackbar.Snackbar
import io.reactivex.functions.Consumer

class ContactDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityContactDetailsBinding


    var b: AlertDialog? = null
    var dialogBuilder: AlertDialog.Builder? = null


     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

         setSupportActionBar(binding.toolbar)

        if (!getIntent().hasExtra(IntentUtils.EXTRA_MESSAGE_ID)) return

        val id = getIntent().getStringExtra(IntentUtils.EXTRA_MESSAGE_ID)
        val chatId = getIntent().getStringExtra(IntentUtils.EXTRA_CHAT_ID)
        val message = RealmHelper.getInstance().getMessage(id, chatId)
        if (message == null) return

        supportActionBar?.title = getString(R.string.contact_info)
        val contact = message.getContact()
        binding.tvContactNameDetails.text = contact.getName()
        val adapter = ContactDetailsAdapter(contact.getRealmList())
        binding.rvContactDetails.layoutManager = LinearLayoutManager(this)
        binding.rvContactDetails.adapter = adapter

        binding.btnAddContact.setOnClickListener {
            startActivity(
                IntentUtils.getAddContactIntent(
                    contact
                )
            )
        }

        adapter.setOnItemClick(object : ContactDetailsAdapter.OnItemClick {
            override fun onItemClick(view: View?, pos: Int) {
                if (!NetworkHelper.isConnected(this@ContactDetailsActivity)) {
                    Snackbar.make(
                        findViewById<View?>(android.R.id.content),
                        R.string.no_internet_connection,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return
                }

                val phoneNumber = contact.getRealmList().get(pos)
                showProgress()
                disposables.add(
                    fireManager.fetchAndSaveUserByPhone(phoneNumber!!.getNumber()).subscribe(
                        Consumer { user: User? ->
                            if (user == null) {
                                hideProgress()
                                SnackbarUtil.showDoesNotFireAppSnackbar(this@ContactDetailsActivity)
                            } else {
                                hideProgress()
                                startChatActivityWithDifferentUser(user)
                            }
                        }, Consumer { throwable: Throwable? ->
                            hideProgress()
                            SnackbarUtil.showDoesNotFireAppSnackbar(this@ContactDetailsActivity)
                        })
                )
            }

            override fun onItemLongClick(view: View?, pos: Int) {
                val phoneNumber = contact.getRealmList().get(pos)
                ClipboardUtil.copyTextToClipboard(
                    this@ContactDetailsActivity,
                    phoneNumber!!.getNumber()
                )
                Toast.makeText(
                    this@ContactDetailsActivity,
                    R.string.copied_to_clipboard,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        binding.rvContactDetails.setInsetPadding()
    }


    fun showProgress() {
        dialogBuilder = AlertDialog.Builder(this@ContactDetailsActivity, R.style.AlertDialogTheme)
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.progress_dialog_layout, null)
        dialogBuilder!!.setView(dialogView)
        dialogBuilder!!.setCancelable(true)
        b = dialogBuilder!!.create()
        b!!.show()
    }

    fun hideProgress() {
        b!!.dismiss()
    }

    private fun startChatActivityWithDifferentUser(user: User) {
        val intent = Intent(this@ContactDetailsActivity, ChatActivity::class.java)
        intent.putExtra(IntentUtils.UID, user.getUid())
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    public override fun enablePresence(): Boolean {
        return false
    }
}
