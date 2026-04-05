package agent.memory.strategy.branching

import agent.memory.core.MemoryStateRefreshMode
import agent.memory.core.MemoryStrategy
import agent.memory.model.BranchConversationState
import agent.memory.model.BranchingStrategyState
import agent.memory.model.MemoryState
import agent.memory.model.ShortTermMemory
import agent.memory.strategy.MemoryStrategyType
import llm.core.model.ChatMessage

/**
 * Стратегия ветвления, которая хранит несколько независимых продолжений диалога
 * и отправляет в модель сообщения активной ветки.
 *
 * Переопределяет `refreshState`, потому что должна синхронизировать сообщения активной ветки с общим состоянием памяти.
 */
class BranchingMemoryStrategy : MemoryStrategy {
    override val type: MemoryStrategyType = MemoryStrategyType.BRANCHING

    override fun effectiveContext(state: MemoryState): List<ChatMessage> =
        state.shortTerm.derivedMessages.toList()

    override fun refreshState(
        state: MemoryState,
        mode: MemoryStateRefreshMode
    ): MemoryState {
        val branchingState = branchingState(state)
        val branches =
            if (branchingState.branches.isEmpty()) {
                listOf(
                    BranchConversationState(
                        name = BranchingStrategyState.DEFAULT_BRANCH_NAME,
                        messages = state.shortTerm.rawMessages
                    )
                )
            } else {
                branchingState.branches.map { branch ->
                    if (branch.name == branchingState.activeBranchName) {
                        branch.copy(messages = state.shortTerm.rawMessages)
                    } else {
                        branch
                    }
                }
            }

        return state.copy(
            shortTerm = ShortTermMemory(
                rawMessages = state.shortTerm.rawMessages,
                derivedMessages = state.shortTerm.rawMessages,
                strategyState = branchingState.copy(
                    activeBranchName = branchingState.activeBranchName.ifBlank { BranchingStrategyState.DEFAULT_BRANCH_NAME },
                    branches = branches
                )
            )
        )
    }

    private fun branchingState(state: MemoryState): BranchingStrategyState =
        (state.shortTerm.strategyState as? BranchingStrategyState)
            ?.takeIf { it.strategyType == type }
            ?: BranchingStrategyState(
                branches = listOf(
                    BranchConversationState(
                        name = BranchingStrategyState.DEFAULT_BRANCH_NAME,
                        messages = state.shortTerm.rawMessages
                    )
                )
            )
}
