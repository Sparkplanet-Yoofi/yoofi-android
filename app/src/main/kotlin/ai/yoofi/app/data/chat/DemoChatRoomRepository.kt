package ai.yoofi.app.data.chat

import ai.yoofi.app.domain.chat.ChatCastMember
import ai.yoofi.app.domain.chat.ChatEvent
import ai.yoofi.app.domain.chat.ChatEventKind
import ai.yoofi.app.domain.chat.ChatItem
import ai.yoofi.app.domain.chat.ChatRoomContent
import ai.yoofi.app.domain.chat.ChatRoomRepository
import ai.yoofi.app.domain.chat.ChatSceneCharacter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 对齐 Figma `1826:9178` / `1826:11556` / `1826:9937` / `1826:9610` 的英文演示稿。
 * 接真实对局接口后只换这一处实现（照 `ChatRoomRepository` 契约换成远端 DataSource 即可）。
 */
@Singleton
class DemoChatRoomRepository @Inject constructor() : ChatRoomRepository {

    override fun current(): ChatRoomContent = DemoContent

    override fun storyBeat(turn: Int): List<ChatItem> =
        listOf(DemoStoryBeats[turn.mod(DemoStoryBeats.size)]("beat-$turn"))
}

/** 演示用的剧情轮次，按顺序循环产出，覆盖旁白 / 对白 / 任务事件三种形态。 */
private val DemoStoryBeats: List<(String) -> ChatItem> = listOf(
    { id ->
        ChatItem.Speech(
            id = id,
            speakerName = "Tomy",
            avatarKey = AvatarTomy,
            body = SpeechQuote,
            audioSeconds = 20,
        )
    },
    { id -> ChatItem.Narrative(id = id, body = Cornucopia) },
    { id ->
        ChatItem.Events(
            id = id,
            events = listOf(
                ChatEvent(
                    id = "$id-item",
                    kind = ChatEventKind.ItemAcquired,
                    subject = "Untitled Diary",
                ),
            ),
        )
    },
    { id ->
        ChatItem.Speech(
            id = id,
            speakerName = "Anmi",
            avatarKey = AvatarAnmi,
            body = SpeechQuote,
            audioSeconds = 20,
        )
    },
)

private val DemoContent = ChatRoomContent(
    chapterTitle = "Chapter Title",
    chapterObjective = "Chapter Objective",
    items = listOf(
        ChatItem.Narrative(
            id = "n1",
            body = Cornucopia,
            sceneCharacters = listOf(
                ChatSceneCharacter(id = "scene-tomy", name = "Tomy", avatarKey = AvatarTomy),
            ),
        ),
        ChatItem.Speech(
            id = "s1",
            speakerName = "Tomy",
            avatarKey = AvatarTomy,
            body = SpeechQuote,
            audioSeconds = 20,
        ),
        ChatItem.Events(
            id = "e1",
            events = listOf(
                ChatEvent(
                    id = "e1-item",
                    kind = ChatEventKind.ItemAcquired,
                    subject = "Untitled Diary",
                ),
                ChatEvent(
                    id = "e1-location",
                    kind = ChatEventKind.LocationUnlocked,
                    subject = "Police Station",
                ),
            ),
        ),
        ChatItem.Narrative(id = "n2", body = Cornucopia),
        ChatItem.Speech(
            id = "s2",
            speakerName = "Anmi",
            avatarKey = AvatarAnmi,
            body = SpeechQuote,
            audioSeconds = 20,
        ),
    ),
    cast = buildCast(),
    inspirations = listOf(
        "frozen for a single heartbeat.",
        "a single, horrible heartbeat.",
        "horrible heartbeat.",
    ),
)

private fun buildCast(): List<ChatCastMember> {
    val pages = 5
    val perPage = 5
    return buildList {
        repeat(pages) { page ->
            repeat(perPage) { index ->
                val lastOnPage = index == perPage - 1
                add(
                    ChatCastMember(
                        id = "cast-${page + 1}-${index + 1}",
                        displayName = if (lastOnPage) "tomytomytomy…" else "tomy",
                        identity = if (lastOnPage) {
                            "Player identity Player identity…"
                        } else {
                            "Player identity"
                        },
                        avatarKey = AvatarTomy,
                    ),
                )
            }
        }
    }
}

private const val AvatarAnmi = "anmi"
private const val AvatarTomy = "tomy"

private const val Cornucopia =
    "The metal floor of the capsule rises beneath you, and then the world " +
        "detonates into sound and heat. Sand, sun, and the roar of a bloodthirsty crowd " +
        "on a thousand screens crash over you as you're ejected onto a golden beach " +
        "encircled by churning turquoise water. The Cornucopia rises ahead—a twenty-foot " +
        "horn of gold spilling weapons across the sand: spears, knives, axes, packs. " +
        "The other tributes materialize around it in their own columns of light, " +
        "frozen for a single, horrible heartbeat."

private const val SpeechQuote =
    "“The other tributes materialize around it in their own columns of light, " +
        "frozen for a single, horrible heartbeat.”"
