package com.bbt.ghostroom.utils.enc

import com.bbt.ghostroom.R
import com.bbt.ghostroom.model.constants.EncryptionType
import com.bbt.ghostroom.utils.MyApp
import com.bbt.ghostroom.utils.enc.aes.AESCrypto
import com.bbt.ghostroom.utils.enc.ethree.EthreeHelper
import kotlinx.coroutines.CoroutineScope

class EncryptionHelper {

    private val aesCrypto: AESCrypto by lazy {
        AESCrypto()
    }

    suspend fun encrypt(
        scope: CoroutineScope,
        singleUidOrMultiple: SingleUidOrMultiple,
        message: String,
        encryptionType: String,
    ): String {
        return when {
            encryptionType.equals(
                EncryptionType.AES,
                ignoreCase = true
            ) -> aesCrypto.encryptPlainTextWithRandomIV(message)
            encryptionType.equals(EncryptionType.E2E, ignoreCase = true) -> {
                if (singleUidOrMultiple.uids != null) {
                    EthreeHelper.encryptMessage(scope, singleUidOrMultiple.uids!!, message)
                } else {
                    EthreeHelper.encryptMessage(scope, singleUidOrMultiple.uid!!, message)
                }
            }
            else -> message
        }
    }


}