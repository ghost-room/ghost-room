package com.bbt.ghostroom.activities.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbt.ghostroom.common.extensions.toDeffered
import com.bbt.ghostroom.extensions.observeSingleValueEvent
import com.bbt.ghostroom.utils.FireConstants
import com.bbt.ghostroom.utils.network.FireManager
import io.reactivex.Observable
import kotlinx.coroutines.launch

class EnterUsernameViewModel : ViewModel() {

    private val _loadUserImage = MutableLiveData<String>()
    val loadUserImageLiveData: LiveData<String>
        get() = _loadUserImage

    fun fetchUserImage() {

        viewModelScope.launch {

            val snapshot = FireConstants.usersRef.child(FireManager.uid)
                .toDeffered().await()

            val photoUrl = snapshot.child("photo").value as? String?

            photoUrl?.let {
                _loadUserImage.value = it
            }

        }
    }

}