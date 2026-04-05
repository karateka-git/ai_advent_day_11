package agent.memory.layer

import agent.memory.model.MemoryCandidateDraft
import agent.memory.model.MemoryLayer
import llm.core.model.ChatRole

/**
 * Определяет, какие кандидаты сохраняются автоматически, а какие требуют подтверждения пользователя.
 */
interface MemoryConfirmationPolicy {
    /**
     * Делит кандидатов на автосохранение и pending-подтверждение.
     */
    fun classify(sourceRole: ChatRole, candidates: List<MemoryCandidateDraft>): MemoryConfirmationDecision
}

/**
 * Результат решения confirmation policy.
 */
data class MemoryConfirmationDecision(
    val autoApply: List<MemoryCandidateDraft> = emptyList(),
    val pending: List<MemoryCandidateDraft> = emptyList()
)

/**
 * Базовая policy: long-term требует подтверждения, часть рабочих категорий тоже.
 */
class DefaultMemoryConfirmationPolicy : MemoryConfirmationPolicy {
    override fun classify(sourceRole: ChatRole, candidates: List<MemoryCandidateDraft>): MemoryConfirmationDecision {
        val autoApply = mutableListOf<MemoryCandidateDraft>()
        val pending = mutableListOf<MemoryCandidateDraft>()

        candidates.forEach { candidate ->
            if (sourceRole != ChatRole.USER) {
                pending += candidate
            } else if (candidate.targetLayer == MemoryLayer.LONG_TERM || candidate.category in pendingWorkingCategories) {
                pending += candidate
            } else {
                autoApply += candidate
            }
        }

        return MemoryConfirmationDecision(
            autoApply = autoApply,
            pending = pending
        )
    }

    private companion object {
        val pendingWorkingCategories = setOf("deadline", "budget", "decision", "open_question")
    }
}
