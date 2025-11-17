package com.bbt.ghostroom.utils

import android.content.Context
import com.bbt.ghostroom.model.constants.DownloadUploadStat
import com.bbt.ghostroom.model.realms.Message
import com.bbt.ghostroom.model.realms.TempMessage
import com.bbt.ghostroom.model.realms.User
import com.bbt.ghostroom.utils.enc.MessageDecryptor
import com.bbt.ghostroom.utils.network.FireManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//This will handle sent web messages
class SentMessageHandler(
    val context: Context,
    val fireManager: FireManager,
    private val messageDecryptor: MessageDecryptor,
    val disposables: CompositeDisposable
) {
    //fire notification
    suspend fun handleNewMessage(phone: String, message: Message) {

        withContext(Dispatchers.Main) {
            try {

                //if message is already exists don't save it
                if (RealmHelper.getInstance()
                        .getTempMessage(message.messageId) != null || RealmHelper.getInstance()
                        .getMessage(message.messageId) != null
                ) {
                    return@withContext
                }
                val chatId = message.chatId


                //if unknown number contacted us ,we want to download his data and save it in local db
                if (!message.isGroup && RealmHelper.getInstance()
                        .getUser(chatId) == null
                )
                    fireManager.fetchAndSaveUserByPhone(phone).subscribe()
                        .addTo(disposables) //CAN WE ADD THIS TO DISPOSABLES

                //check if auto download is enabled for current network type
                val canDownload = SharedPreferencesManager.canDownload(
                    message.type,
                    NetworkHelper.getCurrentNetworkType(context)
                )
                if (canDownload) {
                    //set state to downloading
                    message.downloadUploadStat = DownloadUploadStat.LOADING
                }

                //save message to database
                if (message.isGroup) {
                    val user = RealmHelper.getInstance().getUser(chatId)
                    if (user != null) {
                        saveAndDecryptMessage(message, user)
                    }
                } else {
                    saveAndDecryptMessage(message, phone)
                }

                //start auto download
                if (canDownload) {
                    ServiceHelper.startNetworkRequest(context, message.messageId, chatId)
                }


                val messageId = message.messageId

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }


    private suspend fun saveAndDecryptMessage(
        encryptedMessage: Message,
        phone: String,
    ) {
        val tempMessage = TempMessage.mapMessageToTempMessage(encryptedMessage)
        RealmHelper.getInstance().saveObjectToRealm(tempMessage)


        var decryptedMessage: Message
        withContext(Dispatchers.IO) {
            decryptedMessage = messageDecryptor.decryptMessage(encryptedMessage, this)
        }


        RealmHelper.getInstance().deleteTempMessage(tempMessage.messageId)

        //save message
        RealmHelper.getInstance().saveMessageFromFCM(context, decryptedMessage, phone)

    }

    private suspend fun saveAndDecryptMessage(
        encryptedMessage: Message,
        user: User,
    ) {


        val tempMessage = TempMessage.mapMessageToTempMessage(encryptedMessage)
        RealmHelper.getInstance().saveObjectToRealm(tempMessage)


        var decryptedMessage: Message
        withContext(Dispatchers.IO) {
            decryptedMessage = messageDecryptor.decryptMessage(encryptedMessage, this)
        }

        RealmHelper.getInstance().deleteTempMessage(tempMessage.messageId)

        //save message
        RealmHelper.getInstance().saveMessageFromFCM(decryptedMessage, user)


    }


}