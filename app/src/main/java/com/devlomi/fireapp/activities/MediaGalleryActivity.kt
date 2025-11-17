package com.bbt.ghostroom.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bbt.ghostroom.R
import com.bbt.ghostroom.activities.main.messaging.ChatActivity
import com.bbt.ghostroom.adapters.MediaGalleryAdapter
import com.bbt.ghostroom.common.extensions.setInsetPadding
import com.bbt.ghostroom.common.extensions.superOnBackPressed
import com.bbt.ghostroom.model.realms.Message
import com.bbt.ghostroom.model.realms.User
import com.bbt.ghostroom.utils.IntentUtils
import com.bbt.ghostroom.utils.MessageCreator
import com.bbt.ghostroom.utils.RealmHelper
import com.bbt.ghostroom.utils.ServiceHelper
import com.bbt.ghostroom.utils.network.FireManager.Companion.uid
import com.bbt.ghostroom.views.GridItemDecoration
import com.bbt.ghostroom.views.dialogs.DeleteDialog
import com.bbt.ghostroom.databinding.ActivityMediaGalleryBinding

class MediaGalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMediaGalleryBinding
    @JvmField
    var isInActionMode: Boolean = false
    var adapter: MediaGalleryAdapter? = null
    private lateinit var user: User
    private var mediaInChat: MutableList<Message?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarGallery)

        val uid = intent.getStringExtra(IntentUtils.UID)
        user = RealmHelper.getInstance().getUser(uid)

        supportActionBar?.title = user.getProperUserName()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mediaInChat = RealmHelper.getInstance().getMediaInChat(user.getUid())

        adapter = MediaGalleryAdapter(this, mediaInChat)

        binding.rvMediaGallery.layoutManager = GridLayoutManager(this, ITEMS_COUNT_PER_ROW)
        binding.rvMediaGallery.addItemDecoration(GridItemDecoration(SPAN_COUNT, SPACING, false))
        binding.rvMediaGallery.adapter = adapter
        binding.rvMediaGallery.setInsetPadding()

        onBackPressedDispatcher.addCallback(this){
            if (isInActionMode) {
                exitActionMode()
                // Event is consumed, default back is NOT called
            } else {
                // Call default back behavior if needed
                superOnBackPressed(onBackPressedDispatcher)
            }
        }
    }

    fun addItemToActionMode(itemsCount: Int) {
        binding.tvSelectedImagesCount.text = itemsCount.toString() + ""
    }


    fun onActionModeStarted() {
        if (!isInActionMode) {
            binding.toolbarGallery.menu.clear()
            binding.toolbarGallery.inflateMenu(R.menu.menu_gallery_action)
            setToolbarTitle(false)
        }

        isInActionMode = true
        binding.tvSelectedImagesCount.visibility = View.VISIBLE
    }

    fun exitActionMode() {
        adapter!!.exitActionMode()
        isInActionMode = false
        binding.tvSelectedImagesCount.visibility = View.GONE
        binding.toolbarGallery.menu.clear()
        setToolbarTitle(true)
    }


//    override fun onBackPressed() {
//        if (isInActionMode) exitActionMode()
//        else super.onBackPressed()
//    }


    private fun setToolbarTitle(setVisible: Boolean) {
        if (setVisible) supportActionBar?.title = user.getProperUserName()
        else supportActionBar?.title = ""
    }


    fun getUser(): User {
        return user
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        //update items if items deleted
        adapter?.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menu_item_forward -> forwardItemClicked()
            R.id.menu_item_delete -> deleteItemClicked()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteItemClicked() {
        val deleteDialog = DeleteDialog(this, true)
        deleteDialog.setmListener(object : DeleteDialog.OnFragmentInteractionListener {
            override fun onPositiveClick(isDeleteChecked: Boolean) {
                adapter!!.deleteItems(isDeleteChecked)
                exitActionMode()
            }
        })
        deleteDialog.show()
    }

    private fun forwardItemClicked() {
        val intent = Intent(this, ForwardActivity::class.java)
        startActivityForResult(intent, REQUEST_FORWARD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FORWARD && resultCode == RESULT_OK) {
            //get selected users
            val pickedUsers =
                data?.getSerializableExtra(IntentUtils.EXTRA_DATA_RESULT) as? List<User>? 

            //if the user selects only one user to send the images to him
            //then send the images and the launch activity with that user
            if (pickedUsers!!.size == 1) {
                for (message in adapter!!.getSelectedItems()) {
                    val forwardedMessage = MessageCreator.createForwardedMessage(message, user, uid)
                    if (forwardedMessage != null) {
                        ServiceHelper.startNetworkRequest(
                            this,
                            forwardedMessage.getMessageId(),
                            message.getChatId()
                        )
                    }
                }
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(IntentUtils.UID, user.getUid())
                startActivity(intent)
                finish()
                //otherwise send the images to the users and finish this activity
            } else {
                for (pickedUser in pickedUsers) {
                    for (message in adapter!!.getSelectedItems()) {
                        val forwardedMessage =
                            MessageCreator.createForwardedMessage(message, pickedUser, uid)
                        if (forwardedMessage != null) {
                            ServiceHelper.startNetworkRequest(
                                this,
                                forwardedMessage.getMessageId(),
                                message.getChatId()
                            )
                        }
                    }
                }

                Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val SPAN_COUNT = 3
        private const val SPACING = 16
        private const val ITEMS_COUNT_PER_ROW = 3
        private const val REQUEST_FORWARD = 145
    }
}
