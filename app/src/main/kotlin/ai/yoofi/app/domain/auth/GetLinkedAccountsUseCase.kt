package ai.yoofi.app.domain.auth

/**
 * 读取已关联账号。接口未定时返回双账号都已绑定的 Demo，接上后只改这里。
 */
class GetLinkedAccountsUseCase {
    operator fun invoke(): List<LinkedAccount> = listOf(
        LinkedAccount(
            provider = LinkedAccountProvider.Google,
            linked = true,
            maskedIdentity = DemoMaskedIdentity,
        ),
        LinkedAccount(
            provider = LinkedAccountProvider.Apple,
            linked = true,
            maskedIdentity = DemoMaskedIdentity,
        ),
    )
}

internal const val DemoMaskedIdentity = "z***@gmail.com"
