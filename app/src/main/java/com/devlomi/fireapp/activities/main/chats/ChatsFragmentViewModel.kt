package com.bbt.ghostroom.activities.main.chats

import com.bbt.ghostroom.common.DisposableViewModel
import com.bbt.ghostroom.model.realms.User
import com.bbt.ghostroom.utils.TimeHelper
import com.bbt.ghostroom.utils.network.FireManager
import io.reactivex.rxkotlin.addTo
import java.util.*

class ChatsFragmentViewModel : DisposableViewModel() {
    private val currentDownloads = mutableListOf<String>()


    fun fetchUserImage(pos: Int, user: User) {
        if (user.isBroadcastBool)return
        if (TimeHelper.canFetchUserImage(Date().time, user.lastTimeFetchedImage) && !currentDownloads.contains(user.uid)) {
            currentDownloads.add(user.uid)
            FireManager.checkAndDownloadUserThumbImg(user).subscribe({image ->

            }, { throwable ->

            }).addTo(disposables)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}