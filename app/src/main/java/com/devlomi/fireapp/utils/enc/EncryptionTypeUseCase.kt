package com.bbt.ghostroom.utils.enc

import com.bbt.ghostroom.R
import com.bbt.ghostroom.model.constants.EncryptionType
import com.bbt.ghostroom.model.realms.Message
import com.bbt.ghostroom.utils.MyApp.Companion.context

object EncryptionTypeUseCase {
     fun getEncryptionType(message: Message): String? {
        val encryptionTypeSetting =
            context().getString(R.string.encryption_type)
        return if (message.isGroup && !encryptionTypeSetting.equals(
                EncryptionType.NONE,
                ignoreCase = true
            )
        ) {
            EncryptionType.AES
        } else encryptionTypeSetting
    }
}