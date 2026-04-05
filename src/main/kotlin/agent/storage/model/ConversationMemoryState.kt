package agent.storage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Persisted-снимок layered memory, который сериализуется в JSON-файл модели.
 *
 * @property shortTerm persisted short-term слой с сырым журналом и derived-представлением.
 * @property working persisted рабочая память.
 * @property longTerm persisted долговременная память.
 * @property pending persisted очередь кандидатов на подтверждение.
 */
@Serializable
data class ConversationMemoryState(
    val shortTerm: StoredShortTermMemory = StoredShortTermMemory(),
    val working: StoredWorkingMemory = StoredWorkingMemory(),
    val longTerm: StoredLongTermMemory = StoredLongTermMemory(),
    val pending: StoredPendingMemoryState = StoredPendingMemoryState()
)

/**
 * Persisted-форма заметки рабочего или долговременного слоя памяти.
 *
 * @property category доменная категория заметки.
 * @property content нормализованный текст заметки.
 */
@Serializable
data class StoredMemoryNote(
    val category: String,
    val content: String
)

/**
 * Persisted short-term слой.
 *
 * @property rawMessages полный сырой журнал сессии.
 * @property derivedMessages представление short-term, вычисленное активной стратегией.
 * @property strategyState persisted strategy-specific состояние short-term стратегии.
 */
@Serializable
data class StoredShortTermMemory(
    val rawMessages: List<StoredMessage> = emptyList(),
    val derivedMessages: List<StoredMessage> = emptyList(),
    val strategyState: StoredStrategyState? = null
)

/**
 * Persisted рабочая память.
 *
 * @property notes список заметок текущей задачи.
 */
@Serializable
data class StoredWorkingMemory(
    val notes: List<StoredMemoryNote> = emptyList()
)

/**
 * Persisted долговременная память.
 *
 * @property notes список устойчивых заметок о пользователе и проекте.
 */
@Serializable
data class StoredLongTermMemory(
    val notes: List<StoredMemoryNote> = emptyList()
)

/**
 * Persisted очередь кандидатов на сохранение в durable memory.
 *
 * @property candidates кандидаты, ожидающие решения пользователя.
 * @property nextId следующий числовой идентификатор для нового кандидата.
 */
@Serializable
data class StoredPendingMemoryState(
    val candidates: List<StoredPendingMemoryCandidate> = emptyList(),
    val nextId: Long = 1
)

/**
 * Persisted кандидат на сохранение в память.
 *
 * @property id стабильный идентификатор кандидата.
 * @property targetLayer целевой слой памяти.
 * @property category доменная категория кандидата.
 * @property content текстовое содержимое кандидата.
 * @property sourceRole роль сообщения, из которого извлечён кандидат.
 * @property sourceMessage исходный текст сообщения.
 */
@Serializable
data class StoredPendingMemoryCandidate(
    val id: String,
    val targetLayer: String,
    val category: String,
    val content: String,
    val sourceRole: String,
    val sourceMessage: String
)

/**
 * Базовый контракт для strategy-specific persisted state.
 */
@Serializable
sealed interface StoredStrategyState

/**
 * Persisted state стратегии rolling summary.
 */
@Serializable
@SerialName("summary_compression")
data class StoredSummaryStrategyState(
    val summary: StoredSummary? = null,
    val coveredMessagesCount: Int = 0
) : StoredStrategyState

/**
 * Persisted state стратегии Sticky Facts.
 */
@Serializable
@SerialName("sticky_facts")
data class StoredStickyFactsStrategyState(
    val facts: Map<String, String> = emptyMap(),
    val coveredMessagesCount: Int = 0
) : StoredStrategyState

/**
 * Persisted state стратегии Branching.
 */
@Serializable
@SerialName("branching")
data class StoredBranchingStrategyState(
    val activeBranchName: String? = null,
    val latestCheckpointName: String? = null,
    val checkpoints: List<StoredBranchCheckpoint> = emptyList(),
    val branches: List<StoredBranchConversation> = emptyList()
) : StoredStrategyState

/**
 * Persisted checkpoint branch-aware short-term состояния.
 *
 * @property name имя checkpoint.
 * @property messages сообщения, зафиксированные в checkpoint.
 */
@Serializable
data class StoredBranchCheckpoint(
    val name: String,
    val messages: List<StoredMessage> = emptyList()
)

/**
 * Persisted ветка short-term диалога для branching-стратегии.
 *
 * @property name имя ветки.
 * @property sourceCheckpointName checkpoint, от которого ответвилась ветка.
 * @property messages сообщения активной ветки.
 */
@Serializable
data class StoredBranchConversation(
    val name: String,
    val sourceCheckpointName: String? = null,
    val messages: List<StoredMessage> = emptyList()
)
