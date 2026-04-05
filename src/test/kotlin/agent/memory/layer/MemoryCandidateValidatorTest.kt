package agent.memory.layer

import agent.memory.model.MemoryCandidateDraft
import agent.memory.model.MemoryLayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import llm.core.model.ChatMessage
import llm.core.model.ChatRole

class MemoryCandidateValidatorTest {
    private val validator = MemoryCandidateValidator()

    @Test
    fun `keeps structurally valid candidates without semantic filtering`() {
        val message = ChatMessage(ChatRole.USER, "Напомни мой стиль общения.")
        val candidates = listOf(
            MemoryCandidateDraft(
                targetLayer = MemoryLayer.LONG_TERM,
                category = "communication_style",
                content = "Отвечать лаконично и дружелюбно"
            ),
            MemoryCandidateDraft(
                targetLayer = MemoryLayer.WORKING,
                category = "deadline",
                content = "Реалистичный срок на ближайший релиз"
            )
        )

        assertEquals(candidates, validator.validate(message, candidates))
    }

    @Test
    fun `drops structurally invalid candidates`() {
        val message = ChatMessage(ChatRole.USER, "Любое сообщение")
        val candidates = listOf(
            MemoryCandidateDraft(
                targetLayer = MemoryLayer.SHORT_TERM,
                category = "goal",
                content = "Не должен пройти"
            ),
            MemoryCandidateDraft(
                targetLayer = MemoryLayer.LONG_TERM,
                category = "unknown",
                content = "Неизвестная категория"
            ),
            MemoryCandidateDraft(
                targetLayer = MemoryLayer.WORKING,
                category = "goal",
                content = "   "
            )
        )

        assertEquals(emptyList(), validator.validate(message, candidates))
    }

    @Test
    fun `edited candidate is validated structurally only`() {
        validator.validateEditedCandidate(
            MemoryCandidateDraft(
                targetLayer = MemoryLayer.LONG_TERM,
                category = "communication_style",
                content = "Отвечать более формально"
            )
        )
    }

    @Test
    fun `edited candidate fails on invalid layer`() {
        assertFailsWith<IllegalArgumentException> {
            validator.validateEditedCandidate(
                MemoryCandidateDraft(
                    targetLayer = MemoryLayer.SHORT_TERM,
                    category = "goal",
                    content = "Недопустимо"
                )
            )
        }
    }
}
