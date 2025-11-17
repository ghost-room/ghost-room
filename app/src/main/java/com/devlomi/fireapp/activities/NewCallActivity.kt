package com.bbt.ghostroom.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbt.ghostroom.R
import com.bbt.ghostroom.adapters.NewCallAdapter
import com.bbt.ghostroom.common.extensions.setInsetPadding
import com.bbt.ghostroom.common.extensions.superOnBackPressed
import com.bbt.ghostroom.model.realms.User
import com.bbt.ghostroom.utils.PerformCall
import com.bbt.ghostroom.utils.RealmHelper
import com.bbt.ghostroom.databinding.ActivityNewCallBinding
import io.realm.RealmResults

class NewCallActivity : BaseActivity(), NewCallAdapter.OnClickListener {
    private lateinit var binding: ActivityNewCallBinding
    var adapter: NewCallAdapter? = null
    private var userList: RealmResults<User?>? = null
    private var isInSearchMode = false
    var searchView: SearchView? = null
    private var performCall: PerformCall? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setTitle(R.string.select_contact)
        //enable arrow item in toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userList = RealmHelper.getInstance().getListOfUsers()

        adapter = NewCallAdapter(userList, true, this)
        binding.rvNewCall.layoutManager = LinearLayoutManager(this)
        binding.rvNewCall.adapter = adapter
        adapter?.setOnUserClick(this)

        performCall = PerformCall(this, fireManager, disposables)
        binding.rvNewCall.setInsetPadding()

        onBackPressedDispatcher.addCallback(this) {
            if (isInSearchMode) exitSearchMode()
            else superOnBackPressed(onBackPressedDispatcher)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_new_call, menu)
        val menuItem = menu.findItem(R.id.search_item)
        searchView = menuItem.actionView as? SearchView?

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (!newText.trim { it <= ' ' }.isEmpty()) {
                    val users = RealmHelper.getInstance().searchForUser(newText, false)
                    adapter = NewCallAdapter(users, true, this@NewCallActivity)
                    adapter?.setOnUserClick(this@NewCallActivity)
                    binding.rvNewCall.adapter = adapter
                } else {
                    adapter = NewCallAdapter(userList, true, this@NewCallActivity)
                    adapter?.setOnUserClick(this@NewCallActivity)
                    binding.rvNewCall.adapter = adapter
                }
                return false
            }
        })

        searchView?.setOnCloseListener {
            isInSearchMode = false
            adapter = NewCallAdapter(userList, true, this@NewCallActivity)
            binding.rvNewCall.adapter = adapter
            false
        }

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                if (item.itemId == R.id.search_item) {
                    isInSearchMode = true
                }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if (item.itemId == R.id.search_item) {
                    isInSearchMode = false
                }
                return true
            }
        })




        return super.onCreateOptionsMenu(menu)
    }


    override fun onUserClick(view: View?, user: User, isVideo: Boolean) {
        performCall!!.performCall(isVideo, user.getUid())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) onBackPressed()
        else if (item.getItemId() == R.id.search_item) {
            isInSearchMode = true

            if (searchView?.isIconified ?: false) searchView?.onActionViewExpanded()

            searchView?.requestFocus()
        }

        return super.onOptionsItemSelected(item)
    }


    @SuppressLint("RestrictedApi")
    private fun exitSearchMode() {
        isInSearchMode = false
        binding.toolbar.collapseActionView()
        adapter?.notifyDataSetChanged()
    }

    public override fun enablePresence(): Boolean {
        return false
    }
}
