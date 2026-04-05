package agent.memory.model

import llm.core.model.ChatMessage

/**
 * Тип слоя памяти ассистента.
 */
enum class MemoryLayer {
    SHORT_TERM,
    WORKING,
    LONG_TERM
}

/**
 * Краткая единица явно сохранённой рабочей или долговременной памяти.
 *
 * @property category доменная категория заметки, например `goal` или `communication_style`.
 * @property content нормализованное текстовое содержимое заметки.
 */
data class MemoryNote(
    val category: String,
    val content: String
)

/**
 * Краткосрочная память: сырой журнал текущей сессии и его представление,
 * вычисленное активной short-term стратегией.
 *
 * @property rawMessages полный журнал short-term переписки, который служит
 * источником истины для пересборки стратегии.
 * @property derivedMessages short-term представление, вычисленное активной
 * стратегией из сырого журнала.
 * @property strategyState strategy-specific состояние активной short-term стратегии.
 */
data class ShortTermMemory(
    val rawMessages: List<ChatMessage> = emptyList(),
    val derivedMessages: List<ChatMessage> = emptyList(),
    val strategyState: StrategyState? = null
)

/**
 * Рабочая память: данные текущей задачи.
 *
 * @property notes список заметок, полезных для выполнения текущей задачи.
 */
data class WorkingMemory(
    val notes: List<MemoryNote> = emptyList()
)

/**
 * Долговременная память: устойчивые предпочтения, договорённости и знания.
 *
 * @property notes список устойчивых заметок, полезных в будущих диалогах.
 */
data class LongTermMemory(
    val notes: List<MemoryNote> = emptyList()
)

/**
 * Полный in-memory снимок памяти ассистента с явным разделением по слоям.
 *
 * @property shortTerm краткосрочная память с сырым журналом и derived-представлением стратегии.
 * @property working рабочая память текущей задачи.
 * @property longTerm долговременная память пользователя и проекта.
 * @property pending кандидаты на сохранение, ожидающие подтверждения пользователя.
 */
data class MemoryState(
    val shortTerm: ShortTermMemory = ShortTermMemory(),
    val working: WorkingMemory = WorkingMemory(),
    val longTerm: LongTermMemory = LongTermMemory(),
    val pending: PendingMemoryState = PendingMemoryState()
)
