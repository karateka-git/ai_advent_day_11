package agent.memory.layer

import agent.memory.model.MemoryLayer
import agent.memory.model.MemoryState
import kotlin.test.Test
import kotlin.test.assertEquals
import llm.core.model.ChatMessage
import llm.core.model.ChatRole

class RuleBasedMemoryLayerAllocatorTest {
    private val allocator = RuleBasedMemoryLayerAllocator()

    @Test
    fun `extracts working candidates from task details`() {
        val candidates = allocator.extractCandidates(
            state = MemoryState(),
            message = ChatMessage(
                role = ChatRole.USER,
                content = "Цель проекта - Telegram-бот. Интеграция только с Google Sheets. Срок две недели."
            )
        )

        assertEquals(
            listOf("goal", "constraint", "deadline", "integration"),
            candidates.filter { it.targetLayer == MemoryLayer.WORKING }.map { it.category }
        )
    }

    @Test
    fun `extracts long-term communication style candidate`() {
        val candidates = allocator.extractCandidates(
            state = MemoryState(),
            message = ChatMessage(
                role = ChatRole.USER,
                content = "Отвечай кратко и пиши на русском."
            )
        )

        assertEquals(
            listOf("communication_style"),
            candidates.filter { it.targetLayer == MemoryLayer.LONG_TERM }.map { it.category }.distinct()
        )
    }
}
