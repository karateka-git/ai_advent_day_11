package agent.storage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationMemoryState(
    val messages: List<StoredMessage> = emptyList(),
    val strategyState: StoredStrategyState? = null
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

@Serializable
data class StoredBranchCheckpoint(
    val name: String,
    val messages: List<StoredMessage> = emptyList()
)

@Serializable
data class StoredBranchConversation(
    val name: String,
    val sourceCheckpointName: String? = null,
    val messages: List<StoredMessage> = emptyList()
)
