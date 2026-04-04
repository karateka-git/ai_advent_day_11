package agent.memory.persistence

import agent.memory.model.BranchCheckpointState
import agent.memory.model.BranchConversationState
import agent.memory.model.BranchingStrategyState
import agent.memory.model.ConversationSummary
import agent.memory.model.StickyFactsStrategyState
import agent.memory.model.StrategyState
import agent.memory.model.SummaryStrategyState
import agent.storage.mapper.ConversationMapper
import agent.storage.model.StoredBranchCheckpoint
import agent.storage.model.StoredBranchConversation
import agent.storage.model.StoredBranchingStrategyState
import agent.storage.model.StoredStickyFactsStrategyState
import agent.storage.model.StoredStrategyState
import agent.storage.model.StoredSummaryStrategyState
import agent.storage.model.StoredSummary

/**
 * Преобразует strategy-specific runtime state в persisted state и обратно.
 */
class StrategyStateMapper(
    private val messageMapper: ConversationMapper
) {
    /**
     * Преобразует strategy-specific persisted state в runtime-state.
     */
    fun toRuntime(storedStrategyState: StoredStrategyState?): StrategyState? {
        if (storedStrategyState == null) {
            return null
        }

        return when (storedStrategyState) {
            is StoredSummaryStrategyState -> SummaryStrategyState(
                summary = storedStrategyState.summary?.toRuntimeSummary(),
                coveredMessagesCount = storedStrategyState.coveredMessagesCount
            )
            is StoredStickyFactsStrategyState -> StickyFactsStrategyState(
                facts = storedStrategyState.facts,
                coveredMessagesCount = storedStrategyState.coveredMessagesCount
            )
            is StoredBranchingStrategyState -> BranchingStrategyState(
                activeBranchName = storedStrategyState.activeBranchName ?: BranchingStrategyState.DEFAULT_BRANCH_NAME,
                latestCheckpointName = storedStrategyState.latestCheckpointName,
                checkpoints = storedStrategyState.checkpoints.map { checkpoint ->
                    BranchCheckpointState(
                        name = checkpoint.name,
                        messages = checkpoint.messages.map(messageMapper::fromStoredMessage)
                    )
                },
                branches = storedStrategyState.branches.map { branch ->
                    BranchConversationState(
                        name = branch.name,
                        sourceCheckpointName = branch.sourceCheckpointName,
                        messages = branch.messages.map(messageMapper::fromStoredMessage)
                    )
                }
            )
        }
    }

    /**
     * Преобразует runtime strategy-specific state в persisted state.
     */
    fun toStored(strategyState: StrategyState?): StoredStrategyState? =
        when (strategyState) {
            null -> null
            is StickyFactsStrategyState -> StoredStickyFactsStrategyState(
                facts = strategyState.facts,
                coveredMessagesCount = strategyState.coveredMessagesCount
            )
            is BranchingStrategyState -> StoredBranchingStrategyState(
                activeBranchName = strategyState.activeBranchName,
                latestCheckpointName = strategyState.latestCheckpointName,
                checkpoints = strategyState.checkpoints.map { checkpoint ->
                    StoredBranchCheckpoint(
                        name = checkpoint.name,
                        messages = checkpoint.messages.map(messageMapper::toStoredMessage)
                    )
                },
                branches = strategyState.branches.map { branch ->
                    StoredBranchConversation(
                        name = branch.name,
                        sourceCheckpointName = branch.sourceCheckpointName,
                        messages = branch.messages.map(messageMapper::toStoredMessage)
                    )
                }
            )
            is SummaryStrategyState -> StoredSummaryStrategyState(
                summary = strategyState.summary?.toStoredSummary(),
                coveredMessagesCount = strategyState.coveredMessagesCount
            )
        }

    private fun StoredSummary.toRuntimeSummary(): ConversationSummary =
        ConversationSummary(
            content = content,
            coveredMessagesCount = coveredMessagesCount
        )

    private fun ConversationSummary.toStoredSummary(): StoredSummary =
        StoredSummary(
            content = content,
            coveredMessagesCount = coveredMessagesCount
        )
}
