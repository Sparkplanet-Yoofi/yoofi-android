package ai.yoofi.app.domain.auth

/** 注销确认必须原样输入的短语，对齐 Figma `2252:16627`。 */
const val DeleteConfirmPhrase = "DELETE"

/**
 * 注销二次核验。有密走密码，无密走邮箱验证码；短语都必须是 [DeleteConfirmPhrase]。
 */
sealed interface DeleteAccountProof {
    fun isValid(): Boolean

    data class Password(
        val password: String,
        val phrase: String,
    ) : DeleteAccountProof {
        override fun isValid(): Boolean =
            password.isNotBlank() && phrase == DeleteConfirmPhrase
    }

    data class EmailCode(
        val code: String,
        val phrase: String,
    ) : DeleteAccountProof {
        override fun isValid(): Boolean =
            code.isNotBlank() && phrase == DeleteConfirmPhrase
    }
}
