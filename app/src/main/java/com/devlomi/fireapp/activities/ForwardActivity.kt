package com.bbt.ghostroom.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.TaskStackBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbt.ghostroom.R
import com.bbt.ghostroom.activities.main.MainActivity
import com.bbt.ghostroom.activities.main.messaging.ChatActivity
import com.bbt.ghostroom.adapters.ForwardAdapter
import com.bbt.ghostroom.common.extensions.setInsetPadding
import com.bbt.ghostroom.databinding.ActivityForwardBinding
import com.bbt.ghostroom.model.ExpandableContact
import com.bbt.ghostroom.model.constants.MessageType
import com.bbt.ghostroom.model.realms.User
import com.bbt.ghostroom.utils.BuildVerUtil.isApi29OrAbove
import com.bbt.ghostroom.utils.ContactUtils
import com.bbt.ghostroom.utils.ContactUtils.getContactAsVcard
import com.bbt.ghostroom.utils.IntentUtils
import com.bbt.ghostroom.utils.MessageCreator
import com.bbt.ghostroom.utils.MimeTypes
import com.bbt.ghostroom.utils.RealPathUtil
import com.bbt.ghostroom.utils.RealmHelper
import com.bbt.ghostroom.utils.ServiceHelper
import com.bbt.ghostroom.utils.StringUtils
import com.bbt.ghostroom.utils.Util
import com.bbt.ghostroom.views.DevlomiSnackbar
import io.realm.RealmResults

open class ForwardActivity : BaseActivity() {
    private lateinit var binding: ActivityForwardBinding
    var usersList: RealmResults<User?>? = null
    private lateinit var adapter: ForwardAdapter
    var selectedForwardedUsers: MutableList<User?>? = null
    private var mSnackbar: DevlomiSnackbar? = null


    public override fun enablePresence(): Boolean {
        return false
    }


    interface SearchCallback {
        fun onQuery(newText: String)

        fun onSearchClose()
    }

    fun setSearchCallback(searchCallback: SearchCallback?) {
        this.searchCallback = searchCallback
    }

    private var searchCallback: SearchCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForwardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.fabSend.hide()
        setSupportActionBar(binding.toolbarForward)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initAdapter()
        binding.fabSend.setOnClickListener { view ->
            if (isHasIncomingShare) {
                if (adapter.getSelectedForwardedUsers().isEmpty()) return@setOnClickListener
                handleIncomingShare(adapter.getSelectedForwardedUsers())
            } else {
                if (adapter.getSelectedForwardedUsers().isEmpty()) setResult(RESULT_CANCELED)
                val intent = Intent()
                intent.putParcelableArrayListExtra(
                    IntentUtils.EXTRA_DATA_RESULT,
                    adapter.getSelectedForwardedUsers() as ArrayList<out Parcelable?>?
                )
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        binding.rvForward.setInsetPadding()
    }

    private val isHasIncomingShare: Boolean
        get() {
            val intent = getIntent()
            val action = intent.action
            val type = intent.type

            if (Intent.ACTION_SEND == action && type != null || Intent.ACTION_SEND_MULTIPLE == action && type != null) {
                return true
            }

            return false
        }

    private fun handleIncomingShare(selectedUsers: MutableList<User>) {
        val intent = getIntent()
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if (type == MimeTypes.TEXT_PLAIN) {
                handleTextShare(selectedUsers)
            } else if (type.startsWith(MimeTypes.IMAGE)) {
                handleImageShare(selectedUsers)
            } else if (type.startsWith(MimeTypes.VIDEO)) {
                handleVideoShare()
            } else if (type.startsWith(MimeTypes.CONTACT)) {
                val uri = getIntent().getParcelableExtra<Parcelable?>(Intent.EXTRA_STREAM) as Uri?

                val vcards = getContactAsVcard(this, uri)


                val contactNameList =
                    ContactUtils.getContactNamesFromVcard(vcards)

                val mIntent = Intent(this, SelectContactNumbersActivity::class.java)
                mIntent.putParcelableArrayListExtra(
                    IntentUtils.EXTRA_CONTACT_LIST,
                    contactNameList as ArrayList<out Parcelable?>
                )
                startActivityForResult(mIntent, PICK_NUMBERS_REQUEST)
            } else if (type.startsWith(MimeTypes.AUDIO)) {
                handleIncomingAudio()
            }

            //Multiple Items
        } else if (Intent.ACTION_SEND_MULTIPLE == action && type != null) {
            if (type.startsWith("image/")) {
                handleImageShare(adapter.getSelectedForwardedUsers())
            }
        }
    }


    private fun handleIncomingAudio() {
        val uri = getIntent().getParcelableExtra<Parcelable?>(Intent.EXTRA_STREAM) as Uri?
        val filePath = if (isApi29OrAbove()) uri.toString() else RealPathUtil.getRealPath(this, uri)
        if (filePath == null) {
            showFileNotFoundToast()
            return
        }
        val length = Util.getVideoLength(this, filePath)


        if (adapter.getSelectedForwardedUsers().size > 1) {
            for (user in adapter.getSelectedForwardedUsers()) {
                val message = MessageCreator.Builder(user, MessageType.SENT_AUDIO).path(filePath)
                    .duration(length).build()
                if (message != null) {
                    ServiceHelper.startNetworkRequest(
                        this,
                        message.getMessageId(),
                        message.getChatId()
                    )
                }
            }
        } else {
            val user = adapter.getSelectedForwardedUsers().get(0)
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(IntentUtils.EXTRA_REAL_PATH, filePath)
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.AUDIO)
            intent.putExtra(IntentUtils.UID, user.getUid())
            startTheActivityWithFlags(intent)
        }
        finish()
    }

    private fun handleVideoShare() {
        val videoUri = getIntent().getParcelableExtra<Parcelable?>(Intent.EXTRA_STREAM) as Uri?

        val filePath =
            if (isApi29OrAbove()) videoUri.toString() else RealPathUtil.getRealPath(this, videoUri)

        if (filePath == null) {
            showFileNotFoundToast()
            return
        }
        if (adapter.getSelectedForwardedUsers().size > 1) {
            for (user in adapter.getSelectedForwardedUsers()) {
                val message = MessageCreator.Builder(user, MessageType.SENT_VIDEO).context(this)
                    .path(filePath).fromCamera(false).build()
                if (message != null) {
                    ServiceHelper.startNetworkRequest(
                        this,
                        message.getMessageId(),
                        message.getChatId()
                    )
                }
            }
            showSendingToast()
        } else {
            val intent = Intent(this, ChatActivity::class.java)
            val user = adapter.getSelectedForwardedUsers().get(0)
            intent.putExtra(IntentUtils.EXTRA_REAL_PATH, filePath)
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.VIDEO)
            intent.putExtra(IntentUtils.UID, user.getUid())
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startTheActivityWithFlags(intent)
        }
        finish()
    }

    private fun showSendingToast() {
        Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show()
    }

    private fun showFileNotFoundToast() {
        Toast.makeText(this, R.string.could_not_get_this_file, Toast.LENGTH_SHORT).show()
    }


    private fun handleImageShare(selectedUsers: MutableList<User>) {
        val imageUris = getIntent().getParcelableArrayListExtra<Uri?>(Intent.EXTRA_STREAM)


        //Multiple Images
        if (imageUris != null) {
            if (selectedUsers.size > 1) {
                for (user in selectedUsers) {
                    for (uri in imageUris) {
                        val filePath =
                            if (isApi29OrAbove()) uri.toString() else RealPathUtil.getRealPath(
                                this,
                                uri
                            )
                        if (filePath != null) {
                            val message =
                                MessageCreator.Builder(user, MessageType.SENT_IMAGE).path(filePath)
                                    .fromCamera(false).build()
                            if (message != null) {
                                ServiceHelper.startNetworkRequest(
                                    this,
                                    message.getMessageId(),
                                    message.getChatId()
                                )
                            }
                        } else {
                            showFileNotFoundToast()
                        }
                    }
                }
            } else {
                val realPathList = getRealPathList(imageUris) as ArrayList<String?>
                val user = selectedUsers.get(0)
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(IntentUtils.EXTRA_REAL_PATH_LIST, realPathList)
                intent.putExtra(IntentUtils.UID, user.getUid())
                intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.IMAGE)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startTheActivityWithFlags(intent)
            }
        } else {
            //One Image
            val imageUri = getIntent().getParcelableExtra<Parcelable?>(Intent.EXTRA_STREAM) as Uri?
            val filePath = if (isApi29OrAbove()) imageUri.toString() else RealPathUtil.getRealPath(
                this,
                imageUri
            )
            if (filePath == null) {
                showFileNotFoundToast()
                return
            }

            if (selectedUsers.size > 1) {
                for (user in selectedUsers) {
                    val message =
                        MessageCreator.Builder(user, MessageType.SENT_IMAGE).path(filePath)
                            .fromCamera(false).build()
                    if (message != null) {
                        ServiceHelper.startNetworkRequest(
                            this,
                            message.getMessageId(),
                            message.getChatId()
                        )
                    }
                }
            } else {
                val user = selectedUsers.get(0)
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(IntentUtils.EXTRA_REAL_PATH, filePath)
                intent.putExtra(IntentUtils.UID, user.getUid())
                intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.IMAGE)
                startTheActivityWithFlags(intent)
            }
        }
        finish()
    }

    private fun getRealPathList(imageUris: ArrayList<Uri>): List<String?> {
        if (isApi29OrAbove()) {
            return imageUris.map { it.toString() }
        } else {
            val realPathList: MutableList<String?> = ArrayList<String?>()
            for (uri in imageUris) {
                realPathList.add(RealPathUtil.getRealPath(this, uri))
            }
            return realPathList
        }
    }

    private fun startTheActivityWithFlags(intent: Intent) {
        val sBuilder = TaskStackBuilder.create(this)
        sBuilder.addNextIntentWithParentStack(Intent(this, MainActivity::class.java))
        sBuilder.addNextIntent(intent)
        sBuilder.startActivities()
    }


    private fun handleTextShare(selectedUsers: MutableList<User>) {
        val sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText == null) return

        if (selectedUsers.size > 1) {
            for (selectedUser in selectedUsers) {
                val message =
                    MessageCreator.Builder(selectedUser, MessageType.SENT_TEXT).text(sharedText)
                        .build()
                if (message != null) {
                    ServiceHelper.startNetworkRequest(
                        this,
                        message.getMessageId(),
                        message.getChatId()
                    )
                }
            }
            showSendingToast()
        } else {
            val user = selectedUsers.get(0)
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(IntentUtils.EXTRA_SHARED_TEXT, sharedText)
            intent.putExtra(IntentUtils.UID, user.getUid())
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.TEXT_PLAIN)
            startTheActivityWithFlags(intent)
        }
        finish()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) onBackPressed()

        return super.onOptionsItemSelected(item)
    }


    private fun init() {
        mSnackbar = DevlomiSnackbar(binding.rootView, getResources().getColor(R.color.blue))

        this.listOfUsers

        selectedForwardedUsers = ArrayList<User?>()
    }

    private val listOfUsers: Unit
        get() {
            usersList = RealmHelper.getInstance().getForwardList()
        }

    private fun initAdapter() {
        adapter = ForwardAdapter(usersList, selectedForwardedUsers, true, this, null)
        binding.rvForward.layoutManager = LinearLayoutManager(this)
        binding.rvForward.adapter = adapter
    }


    fun updateSelectedUsers() {
        var userName = ""
        for (user1 in adapter.getSelectedForwardedUsers()) {
            userName += user1.getProperUserName() + SEPARATOR
        }


        binding.tvSelectedContact.text = StringUtils.removeExtraSeparators(userName, SEPARATOR)
        mSnackbar!!.getSnackbarTextView()
            .setText(StringUtils.removeExtraSeparators(userName, SEPARATOR))
    }


    fun showSnackbar() {
        if (!mSnackbar!!.isShowing()) {
            mSnackbar!!.showSnackBar()
            binding.fabSend.show()
        }
    }

    fun hideSnackbar() {
        binding.tvSelectedContact.text = ""
        binding.fabSend.hide()
        mSnackbar!!.dismissSnackbar()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_NUMBERS_REQUEST) {
            if (resultCode == RESULT_OK) {
                val selectedContacts: MutableList<ExpandableContact?>? =
                    data?.getParcelableArrayListExtra<ExpandableContact?>(IntentUtils.EXTRA_CONTACT_LIST)
                handleContacts(selectedContacts)
            } else {
                Toast.makeText(this, R.string.not_contact_selected, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleContacts(selectedContacts: MutableList<ExpandableContact?>?) {
        if (adapter.getSelectedForwardedUsers().size > 1) {
            for (user in adapter.getSelectedForwardedUsers()) {
                val messages = MessageCreator.Builder(user, MessageType.SENT_CONTACT)
                    .contacts(selectedContacts).buildContacts()
                if (messages != null) {
                    for (message in messages) {
                        if (message != null) {
                            ServiceHelper.startNetworkRequest(
                                this,
                                message.getMessageId(),
                                message.getChatId()
                            )
                        }
                    }
                }
            }

            showSendingToast()
        } else {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putParcelableArrayListExtra(
                IntentUtils.EXTRA_CONTACT_LIST,
                selectedContacts as ArrayList<out Parcelable?>?
            )
            val user = adapter.getSelectedForwardedUsers().get(0)
            intent.putExtra(IntentUtils.UID, user.getUid())
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.CONTACT)
            startTheActivityWithFlags(intent)
            finish()
        }
    }

    protected override fun onDestroy() {
        setResult(RESULT_CANCELED)
        adapter.onDestroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_forward, menu)
        val menuItem = menu.findItem(R.id.menu_item_search)
        val searchView = menuItem.getActionView() as SearchView?

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //notify new group activity if it's not null

                if (searchCallback != null) searchCallback!!.onQuery(newText)

                if (!newText.trim { it <= ' ' }.isEmpty()) {
                    val users = RealmHelper.getInstance().searchForUser(newText, true)
                    adapter = ForwardAdapter(
                        users,
                        selectedForwardedUsers,
                        true,
                        this@ForwardActivity,
                        null
                    )
                    binding.rvForward.adapter = adapter
                } else {
                    adapter = ForwardAdapter(
                        usersList,
                        selectedForwardedUsers,
                        true,
                        this@ForwardActivity,
                        null
                    )
                    binding.rvForward.adapter = adapter
                }
                return false
            }
        })
        searchView.setOnCloseListener {
            searchCallback!!.onSearchClose()
            adapter = ForwardAdapter(
                usersList,
                selectedForwardedUsers,
                true,
                this@ForwardActivity,
                null
            )
            binding.rvForward.adapter = adapter
            false
        }

        return super.onCreateOptionsMenu(menu)
    }


    companion object {
        const val PICK_NUMBERS_REQUEST: Int = 1478
        const val SEPARATOR: String = " , "
    }
}
