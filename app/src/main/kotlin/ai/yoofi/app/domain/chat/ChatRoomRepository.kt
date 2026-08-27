package ai.yoofi.app.domain.chat

/**
 * 聊天室内容入口。当前是演示快照；接流式协议后可扩成 Flow。
 */
interface ChatRoomRepository {
    fun current(): ChatRoomContent

    /**
     * 推进剧情，返回本轮新增的消息。[turn] 从 0 起累加，接服务端后换成对话轮次入参。
     * 一轮可能同时产出旁白、对白与任务事件，故返回列表。
     */
    fun storyBeat(turn: Int): List<ChatItem>
}
