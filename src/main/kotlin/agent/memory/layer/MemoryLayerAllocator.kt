package agent.memory.layer

import agent.memory.model.MemoryCandidateDraft
import agent.memory.model.MemoryLayer
import agent.memory.model.MemoryState
import llm.core.model.ChatMessage
import llm.core.model.ChatRole

/**
 * Явно извлекает кандидатов на сохранение в durable memory слои.
 */
interface MemoryLayerAllocator {
    /**
     * Извлекает из нового сообщения кандидатов для рабочей и долговременной памяти.
     *
     * @param state текущее состояние многослойной памяти.
     * @param message новое сообщение, которое нужно проанализировать.
     * @return черновики заметок, которые затем пройдут валидацию и confirmation policy.
     */
    fun extractCandidates(state: MemoryState, message: ChatMessage): List<MemoryCandidateDraft>
}

/**
 * Простая реализация распределителя памяти на основе правил.
 *
 * Извлекает отдельные смысловые фрагменты из сообщения, но не пишет их сразу в durable memory.
 */
class RuleBasedMemoryLayerAllocator : MemoryLayerAllocator {
    /**
     * Выделяет кандидатов для рабочей и долговременной памяти по набору строковых маркеров.
     *
     * @param state текущее состояние памяти.
     * @param message новое сообщение пользователя или ассистента.
     * @return черновики заметок с целевым memory layer.
     */
    override fun extractCandidates(state: MemoryState, message: ChatMessage): List<MemoryCandidateDraft> {
        if (message.role == ChatRole.SYSTEM) {
            return emptyList()
        }

        val segments = extractSegments(message.content)
        return buildList {
            maybeAdd(MemoryLayer.WORKING, "goal", segments, goalMarkers)
            maybeAdd(MemoryLayer.WORKING, "constraint", segments, constraintMarkers)
            maybeAdd(MemoryLayer.WORKING, "deadline", segments, deadlineMarkers)
            maybeAdd(MemoryLayer.WORKING, "budget", segments, budgetMarkers)
            maybeAdd(MemoryLayer.WORKING, "integration", segments, integrationMarkers)
            maybeAdd(MemoryLayer.WORKING, "decision", segments, decisionMarkers)
            maybeAdd(MemoryLayer.WORKING, "open_question", segments, openQuestionMarkers)
            maybeAdd(MemoryLayer.LONG_TERM, "communication_style", segments, communicationStyleMarkers)
            maybeAdd(MemoryLayer.LONG_TERM, "persistent_preference", segments, preferenceMarkers)
            maybeAdd(MemoryLayer.LONG_TERM, "architectural_agreement", segments, architectureMarkers)
            maybeAdd(MemoryLayer.LONG_TERM, "reusable_knowledge", segments, reusableKnowledgeMarkers)
        }
    }

    /**
     * Добавляет кандидаты по всем сегментам сообщения, которые совпали с маркерами выбранной категории.
     *
     * @param layer целевой слой памяти.
     * @param category категория заметки.
     * @param segments смысловые сегменты исходного сообщения.
     * @param markers список маркеров категории.
     */
    private fun MutableList<MemoryCandidateDraft>.maybeAdd(
        layer: MemoryLayer,
        category: String,
        segments: List<String>,
        markers: List<String>
    ) {
        segments
            .filter { segment ->
                val normalized = segment.lowercase()
                markers.any { marker -> normalized.contains(marker) }
            }
            .forEach { segment ->
                add(
                    MemoryCandidateDraft(
                        targetLayer = layer,
                        category = category,
                        content = segment
                    )
                )
            }
    }

    /**
     * Делит сообщение на смысловые сегменты, чтобы durable memory не хранила исходный текст целиком.
     *
     * @param content исходный текст сообщения.
     * @return очищенные непустые сегменты сообщения.
     */
    private fun extractSegments(content: String): List<String> =
        content
            .split(segmentsDelimiter)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .ifEmpty { listOf(content.trim()) }

    private companion object {
        val segmentsDelimiter = Regex("[.!?\\n]+")

        val goalMarkers = listOf(
            "цель",
            "задача",
            "mvp",
            "нужно сделать",
            "нужно реализовать",
            "нужно собрать",
            "нужно внедрить"
        )
        val constraintMarkers = listOf(
            "ограничени",
            "только",
            "без",
            "нельзя",
            "не делать",
            "должен"
        )
        val deadlineMarkers = listOf(
            "срок",
            "дедлайн",
            "недел",
            "дня",
            "дней",
            "до "
        )
        val budgetMarkers = listOf(
            "бюджет",
            "стоимост",
            "руб",
            "тысяч"
        )
        val integrationMarkers = listOf(
            "интеграц",
            "google sheets",
            "telegram api",
            "crm",
            "api"
        )
        val decisionMarkers = listOf(
            "решили",
            "решение",
            "выбираем",
            "будем",
            "оставляем",
            "убираем",
            "добавляем"
        )
        val openQuestionMarkers = listOf(
            "открытый вопрос",
            "открытые вопросы",
            "нужно решить",
            "непонятно",
            "вопрос",
            "как"
        )
        val communicationStyleMarkers = listOf(
            "отвечай",
            "пиши",
            "кратко",
            "подробно",
            "на русском",
            "стиль общения"
        )
        val preferenceMarkers = listOf(
            "предпочита",
            "люблю",
            "всегда",
            "не используй",
            "хочу"
        )
        val architectureMarkers = listOf(
            "архитектур",
            "business logic",
            "ui должен",
            "не завязывать",
            "договоренн",
            "в проекте используем"
        )
        val reusableKnowledgeMarkers = listOf(
            "проект",
            "ассистент",
            "пользователь",
            "полезно повторно",
            "устойчив"
        )
    }
}
