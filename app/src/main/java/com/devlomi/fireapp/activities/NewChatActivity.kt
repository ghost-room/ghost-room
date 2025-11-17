package com.bbt.ghostroom.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbt.ghostroom.R
import com.bbt.ghostroom.activities.main.messaging.ChatActivity
import com.bbt.ghostroom.adapters.UsersAdapter
import com.bbt.ghostroom.common.extensions.setInsetPadding
import com.bbt.ghostroom.common.extensions.superOnBackPressed
import com.bbt.ghostroom.databinding.ActivityNewChatBinding
import com.bbt.ghostroom.model.realms.User
import com.bbt.ghostroom.utils.ContactUtils.syncContacts
import com.bbt.ghostroom.utils.IntentUtils
import com.bbt.ghostroom.utils.RealmHelper
import com.bbt.ghostroom.utils.SharedPreferencesManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.realm.RealmResults

class NewChatActivity : BaseActivity(), UsersAdapter.OnItemClickListener {
    var adapter: UsersAdapter? = null
    var userList: RealmResults<User?>? = null
    private var _binding: ActivityNewChatBinding? = null
    private val binding: ActivityNewChatBinding get() = _binding!!
    private var isInSearchMode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNewChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        userList = this.listOfUsers
        setTheAdapter()

        supportActionBar?.setTitle(R.string.select_contact)
        //enable arrow item in toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadAd()
        updateNoContactsLayout()

        binding.refreshContactsBtn.setOnClickListener { syncContacts() }

        binding.btnInvite.setOnClickListener { startActivity(IntentUtils.getShareAppIntent(this)) }

        binding.recyclerContainer.setInsetPadding()

        onBackPressedDispatcher.addCallback(this) {
            // Call default back behavior if needed
            if (isInSearchMode) {
                binding.toolbar.collapseActionView()
                isInSearchMode = false
            } else {
                superOnBackPressed(onBackPressedDispatcher)
            }
        }
    }

    private fun loadAd() {
        binding.adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                binding.adView.visibility = View.GONE
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                binding.adView.visibility = View.VISIBLE
            }
        }

        if (getResources().getBoolean(R.bool.is_new_chat_ad_enabled)) binding.adView.loadAd(
            AdRequest.Builder().build()
        )
    }

    private fun syncContacts() {
        binding.progressBarSync.visibility = View.VISIBLE
        binding.refreshContactsBtn.visibility = View.GONE
        disposables.add(
            syncContacts(SharedPreferencesManager.isDesktopEnabled()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    Action {
                        onSyncFinished()
                    }, Consumer { throwable: Throwable? ->
                        onSyncFinished()
                        Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show()
                    })
        )
    }


    private val listOfUsers: RealmResults<User?>?
        get() = RealmHelper.getInstance().listOfUsers


    private fun setTheAdapter() {
        adapter = UsersAdapter(userList, true, this)
        binding.rvNewChat.setLayoutManager(LinearLayoutManager(this))
        binding.rvNewChat.setAdapter(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        else if (item.itemId == R.id.invite_item) {
            startActivity(IntentUtils.getShareAppIntent(this))
        } else if (item.itemId == R.id.menu_item_search) {
            isInSearchMode = true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_new_chat, menu)
        val menuItem = menu.findItem(R.id.menu_item_search)
        val searchView = menuItem.actionView as SearchView?

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                if (item.itemId == R.id.menu_item_search) {
                    isInSearchMode = true
                }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if (item.itemId == R.id.menu_item_search) {
                    isInSearchMode = false
                }
                return true
            }
        })
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (!newText.trim { it <= ' ' }.isEmpty()) {
                    val users = RealmHelper.getInstance().searchForUser(newText, false)
                    adapter = UsersAdapter(users, true, this@NewChatActivity)
                    binding.rvNewChat.setAdapter(adapter)
                } else {
                    adapter = UsersAdapter(userList, true, this@NewChatActivity)
                    binding.rvNewChat.setAdapter(adapter)
                }
                return false
            }
        })

        searchView.setOnCloseListener {
            isInSearchMode = false
            adapter = UsersAdapter(userList, true, this@NewChatActivity)
            binding.rvNewChat.setAdapter(adapter)
            false
        }

        return super.onCreateOptionsMenu(menu)
    }

    //hide progress bar when sync contacts finishes
    fun onSyncFinished() {
        binding.progressBarSync.visibility = View.GONE
        binding.refreshContactsBtn.visibility = View.VISIBLE
        updateNoContactsLayout()
    }

    override fun onItemClick(user: User) {
        val intent = Intent(this@NewChatActivity, ChatActivity::class.java)
        intent.putExtra(IntentUtils.UID, user.uid)
        startActivity(intent)
        finish()
    }

    override fun onUserPhotoClick(user: User) {
        val intent = Intent(this@NewChatActivity, ProfilePhotoDialog::class.java)
        intent.putExtra(IntentUtils.UID, user.uid)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun updateNoContactsLayout() {
        if (userList == null) return
        if (userList!!.isEmpty()) {
            val noContactsText =
                String.format(getString(R.string.no_contacts), getString(R.string.app_name))
            binding.tvNoContacts.text = noContactsText
            binding.noContactsContainer.visibility = View.VISIBLE
            binding.recyclerContainer.visibility = View.GONE
        } else {
            binding.noContactsContainer.visibility = View.GONE
            binding.recyclerContainer.visibility = View.VISIBLE
        }
    }

    public override fun enablePresence(): Boolean {
        return false
    }
}
