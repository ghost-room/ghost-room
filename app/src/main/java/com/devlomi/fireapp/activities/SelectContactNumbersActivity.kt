package com.bbt.ghostroom.activities

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbt.ghostroom.R
import com.bbt.ghostroom.adapters.NumbersForContactAdapter
import com.bbt.ghostroom.common.extensions.setInsetPadding
import com.bbt.ghostroom.databinding.ActivitySelectContactNumbersBinding
import com.bbt.ghostroom.model.ExpandableContact
import com.bbt.ghostroom.utils.ContactUtils
import com.bbt.ghostroom.utils.IntentUtils
import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class SelectContactNumbersActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectContactNumbersBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectContactNumbersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        if (!intent.hasExtra(IntentUtils.EXTRA_CONTACT_LIST)) return


        supportActionBar?.setTitle(R.string.select_numbers)
        val result: MutableList<ExpandableContact?>? =
            intent.getParcelableArrayListExtra<ExpandableContact?>(IntentUtils.EXTRA_CONTACT_LIST)

        val adapter = NumbersForContactAdapter(result)

        //EXPAND ALL GROUPS
        adapter.toggleAllGroups()

        setItemsChecked(adapter)

        binding.rvNumbersForContactSelector.layoutManager = LinearLayoutManager(this)
        binding.rvNumbersForContactSelector.adapter = adapter


        binding.fabSendContactSelect.setOnClickListener { //getting selected numbers from contacts
            val contactNameList =
                ContactUtils.getContactsFromExpandableGroups(
                    (adapter.groups as MutableList<out ExpandableGroup<*>?>?)!!
                )
            val intent = Intent()
            intent.putParcelableArrayListExtra(
                IntentUtils.EXTRA_CONTACT_LIST,
                contactNameList as ArrayList<out Parcelable?>
            )
            setResult(RESULT_OK, intent)
            finish()
        }


        with(binding.fabSendContactSelect) {
            setInsetPadding(
                plusLeft = paddingLeft,
                plusBottom = paddingBottom,
                plusRight = paddingRight
            )
        }
        binding.rvNumbersForContactSelector.setInsetPadding()
    }


    //set all numbers as Checked
    private fun setItemsChecked(adapter: NumbersForContactAdapter) {
        for (i in adapter.groups.indices) {
            val group = adapter.groups.get(i) as MultiCheckExpandableGroup
            for (x in group.items.indices) {
                group.checkChild(x)
            }
        }
    }
}
