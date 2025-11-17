package com.bbt.ghostroom.model;

import android.os.Parcelable;

import com.bbt.ghostroom.model.realms.PhoneNumber;
import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

import io.realm.RealmList;



// expandable contact
// make user selects which numbers want to send for the contact
public class ExpandableContact extends MultiCheckExpandableGroup implements Parcelable {

    public ExpandableContact(String contactName, RealmList<PhoneNumber> phoneNumbers) {
        super(contactName, phoneNumbers);

    }

}
