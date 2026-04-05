package agent.memory.layer

import agent.memory.model.MemoryCandidateDraft
import agent.memory.model.MemoryLayer
import llm.core.model.ChatMessage
import llm.core.model.ChatRole

/**
 * Выполняет только структурную проверку кандидатов перед автосохранением или отправкой в pending.
 *
 * Этот validator намеренно не принимает смысловых решений за пользователя:
 * он не пытается угадать, "достаточно ли хорош" текст кандидата для конкретной категории,
 * а лишь отбрасывает заведомо некорректные данные.
 */
class MemoryCandidateValidator {
    /**
     * Возвращает только структурно допустимых кандидатов.
     *
     * @param message исходное сообщение, из которого извлекались кандидаты.
     * @param candidates черновики заметок до валидации.
     * @return кандидаты, которые можно показать пользователю или передать дальше в confirmation policy.
     */
    fun validate(message: ChatMessage, candidates: List<MemoryCandidateDraft>): List<MemoryCandidateDraft> {
        if (message.role == ChatRole.SYSTEM) {
            return emptyList()
        }

        return candidates
            .filter(::isLayerSupported)
            .filter(::hasAllowedCategory)
            .filter(::hasMeaningfulContent)
    }

    /**
     * Проверяет, что после ручной правки кандидат остаётся структурно допустимым.
     */
    fun validateEditedCandidate(candidate: MemoryCandidateDraft) {
        require(isLayerSupported(candidate)) {
            "Pending-кандидат нельзя сохранить в краткосрочную память."
        }
        require(hasAllowedCategory(candidate)) {
            "Категория ${candidate.category} не подходит для слоя ${candidate.targetLayer.name.lowercase()}."
        }
        require(hasMeaningfulContent(candidate)) {
            "Текст кандидата не должен быть пустым."
        }
    }

    private fun isLayerSupported(candidate: MemoryCandidateDraft): Boolean =
        candidate.targetLayer != MemoryLayer.SHORT_TERM

    private fun hasAllowedCategory(candidate: MemoryCandidateDraft): Boolean =
        MemoryLayerCategories.isCategoryAllowed(candidate.targetLayer, candidate.category)

    private fun hasMeaningfulContent(candidate: MemoryCandidateDraft): Boolean =
        candidate.content.trim().isNotEmpty()
}
