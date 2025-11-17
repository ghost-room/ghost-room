package com.bbt.ghostroom

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.bbt.ghostroom.activities.calling.CallingActivity
import com.bbt.ghostroom.activities.calling.model.CallEndedReason
import com.bbt.ghostroom.model.constants.FireCallDirection
import com.bbt.ghostroom.model.realms.FireCall
import com.bbt.ghostroom.utils.IntentUtils
import com.bbt.ghostroom.utils.MyApp
import com.bbt.ghostroom.utils.NotificationHelper
import com.bbt.ghostroom.utils.RealmHelper
import com.bbt.ghostroom.utils.RingtonePlayer
import com.bbt.ghostroom.utils.ServiceHelper
import com.bbt.ghostroom.utils.network.CallsManager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.TimeUnit

/*
On Android 14 (API 34) and higher, the system restricts starting a Foreground Service from the Bacgkround while having Mic and Camera Permissions
Previously we were starting a Foreground Service from the Background using FCM Service.
To Resolve this we have to fire a notification to from FCM or the activity, and when the user clicks on the notification, we can start the Foreground Service.
 */
class IncomingCallReceiver(
    private val context: Context,
    private val disposables: CompositeDisposable,
) {



    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val callManager: CallsManager by lazy {
        CallsManager()
    }

    private var callTimeoutDeferDisposable: Disposable? = null
    private val callTimeoutDuration = 30L
    private var fireCall: FireCall? = null
    private var notificationId = -1


    fun onCallReceived(callId: String) {

        RealmHelper.getInstance().getFireCall(callId)?.let { fireCall ->
            this.fireCall = fireCall
            //prevent multiple incoming calls
            if (MyApp.isIsCallActive) return@let

            MyApp.setCallActive(true)


            val uid = fireCall.user.uid
            val callId = fireCall.callId
            Intent(context, CallingActivity::class.java).apply {
                putExtra(IntentUtils.CALL_DIRECTION, FireCallDirection.INCOMING)
                putExtra(IntentUtils.CALL_TYPE, fireCall.callType)
                putExtra(IntentUtils.CALL_ID, callId)
                putExtra(IntentUtils.UID, uid)
                putExtra(IntentUtils.PHONE, fireCall.phoneNumber)
                putExtra(
                    IntentUtils.CALL_ACTION_TYPE,
                    IntentUtils.NOTIFICATION_ACTION_START_INCOMING
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }

            val notificationHelper = NotificationHelper(context)
            val notificationId = getNotificationId()
            val incomingCallNotification =
                notificationHelper.createIncomingCallNotification(
                    fireCall,
                    notificationId
                )

            notificationHelper.notifyNotification(notificationId, incomingCallNotification)

//            ServiceCompat.startForeground(
//                context,
//                getNotificationId(),
//                incomingCallNotification,
//                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
//            )
            if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
//                requestAudioFocus(AudioManager.STREAM_VOICE_CALL)
                MyApp.app().ringtonePlayer().playIncomingRingtone()
            }

            startDefer()

            if (!fireCall.isGroupCall) {

                callManager.listenForEndingCall(
                    fireCall.callId,
                    fireCall.user.uid,
                    true
                ).subscribe({
                    if (it.exists()) {
                        endCall(CallEndedReason.REMOTE_REJECTED)
                    }
                }, { error ->

                }).addTo(disposables)
            }
        }

    }

    private fun endCall(reason: CallEndedReason) {
        MyApp.app().ringtonePlayer().stopRingtone()
        NotificationHelper(context).cancelIncomingCallNotification()
        
        fireCall?.let { call ->
            if (reason == CallEndedReason.NO_ANSWER) {
                call.direction = FireCallDirection.MISSED
                RealmHelper.getInstance().saveObjectToRealm(call)
            }
        }
        
        MyApp.setCallActive(false)
        callTimeoutDeferDisposable?.dispose()
        callTimeoutDeferDisposable = null
        disposables.clear()
    }

    private fun startDefer() {
        if (callTimeoutDeferDisposable == null) {
            callTimeoutDeferDisposable = Completable.complete()
                .delay(callTimeoutDuration, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    fireCall?.let { fireCall ->
                        setCallEnded(fireCall.callId, fireCall.user.uid, true)
                    }

                    endCall(CallEndedReason.NO_ANSWER)
                }, { error ->

                }).addTo(disposables)
        }
    }

    private fun setCallEnded(callId: String, otherUid: String, isIncoming: Boolean) {
        fireCall?.let { fireCall ->
            if (!fireCall.isGroupCall) {
                ServiceHelper.setCallEnded(context, callId, otherUid, isIncoming)
            }
        }
    }

    private fun getNotificationId(): Int {
        if (notificationId == -1) notificationId = NotificationHelper.generateId()
        return notificationId
    }
}