package agent.memory.persistence

import agent.memory.model.ConversationSummary
import agent.memory.model.LongTermMemory
import agent.memory.model.MemoryLayer
import agent.memory.model.MemoryNote
import agent.memory.model.MemoryState
import agent.memory.model.PendingMemoryCandidate
import agent.memory.model.PendingMemoryState
import agent.memory.model.ShortTermMemory
import agent.memory.model.SummaryStrategyState
import agent.memory.model.WorkingMemory
import kotlin.test.Test
import kotlin.test.assertEquals
import llm.core.model.ChatMessage
import llm.core.model.ChatRole

class ConversationMemoryStateMapperTest {
    private val mapper = ConversationMemoryStateMapper()

    @Test
    fun `maps layered memory with pending candidates to stored and back`() {
        val messages = listOf(
            ChatMessage(ChatRole.SYSTEM, "system"),
            ChatMessage(ChatRole.USER, "u1")
        )
        val runtimeState = MemoryState(
            shortTerm = ShortTermMemory(
                rawMessages = messages,
                derivedMessages = messages,
                strategyState = SummaryStrategyState(
                    summary = ConversationSummary(
                        content = "summary",
                        coveredMessagesCount = 2
                    ),
                    coveredMessagesCount = 2
                )
            ),
            working = WorkingMemory(
                notes = listOf(MemoryNote(category = "goal", content = "Собрать ТЗ"))
            ),
            longTerm = LongTermMemory(
                notes = listOf(MemoryNote(category = "communication_style", content = "Отвечай кратко"))
            ),
            pending = PendingMemoryState(
                candidates = listOf(
                    PendingMemoryCandidate(
                        id = "p1",
                        targetLayer = MemoryLayer.LONG_TERM,
                        category = "communication_style",
                        content = "Отвечай кратко",
                        sourceRole = ChatRole.USER,
                        sourceMessage = "Отвечай кратко"
                    )
                ),
                nextId = 2
            )
        )

        val restoredState = mapper.toRuntime(mapper.toStored(runtimeState))

        assertEquals(runtimeState, restoredState)
    }
}
