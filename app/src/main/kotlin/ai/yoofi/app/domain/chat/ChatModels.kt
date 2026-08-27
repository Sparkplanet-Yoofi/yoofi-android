package ai.yoofi.app.domain.chat

/**
 * 多人聊天室一局的展示数据。不含 Android 类型，方便日后迁 KMP。
 */
data class ChatRoomContent(
    val chapterTitle: String,
    val chapterObjective: String,
    val items: List<ChatItem>,
    val cast: List<ChatCastMember>,
    val inspirations: List<String>,
)

sealed interface ChatItem {
    val id: String

    /**
     * 旁白，书本图标 + 白字。
     * [sceneCharacters] 非空时在旁白正文下方挂一组「Scene Characters」角色卡（Figma `1826:9625`）。
     */
    data class Narrative(
        override val id: String,
        val body: String,
        val sceneCharacters: List<ChatSceneCharacter> = emptyList(),
    ) : ChatItem

    /** 角色对白：头像、名字、语音胶囊、气泡。 */
    data class Speech(
        override val id: String,
        val speakerName: String,
        val avatarKey: String,
        val body: String,
        val audioSeconds: Int,
    ) : ChatItem

    /** 玩家自己发出的浅色右气泡。 */
    data class Player(
        override val id: String,
        val body: String,
    ) : ChatItem

    /**
     * 居中的任务事件提示（Figma `1826:9654` / `1826:9660`）。
     * 服务端一次可下发多条连续事件，同组内间距比普通消息更紧凑。
     */
    data class Events(
        override val id: String,
        val events: List<ChatEvent>,
    ) : ChatItem
}

/** 旁白引出的场景人物，点击可查看角色详情。 */
data class ChatSceneCharacter(
    val id: String,
    val name: String,
    val avatarKey: String,
)

/**
 * 一条任务事件。[kind] 决定配色与图标，[subject] 是事件主体（道具名 / 地点名）。
 * 前缀文案属于界面用语，放 UI 层做本地化，不由服务端下发。
 */
data class ChatEvent(
    val id: String,
    val kind: ChatEventKind,
    val subject: String,
)

enum class ChatEventKind {
    /** 获得道具，琥珀色全圆角胶囊 */
    ItemAcquired,

    /** 解锁地点，紫色 12dp 圆角卡 */
    LocationUnlocked,
}

data class ChatCastMember(
    val id: String,
    val displayName: String,
    val identity: String,
    val avatarKey: String,
)
