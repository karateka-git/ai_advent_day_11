package agent.memory.strategy.slidingwindow

import agent.memory.core.MemoryStrategy
import agent.memory.core.MemoryStateRefreshMode
import agent.memory.model.MemoryState
import agent.memory.model.ShortTermMemory
import agent.memory.strategy.MemoryStrategyType
import llm.core.model.ChatMessage
import llm.core.model.ChatRole

/**
 * Стратегия памяти, которая оставляет в effective prompt только системные сообщения
 * и последние сообщения диалога из полной истории.
 *
 * Не переопределяет `refreshState`, потому что выбирает окно прямо из полной истории и не поддерживает отдельный state стратегии.
 */
class SlidingWindowMemoryStrategy(
    private val recentMessagesCount: Int
) : MemoryStrategy {
    init {
        require(recentMessagesCount > 0) {
            "Количество последних сообщений должно быть больше нуля."
        }
    }

    override val type: MemoryStrategyType = MemoryStrategyType.SLIDING_WINDOW

    override fun effectiveContext(state: MemoryState): List<ChatMessage> {
        return state.shortTerm.derivedMessages.toList()
    }

    override fun refreshState(
        state: MemoryState,
        mode: MemoryStateRefreshMode
    ): MemoryState {
        val rawMessages = state.shortTerm.rawMessages
        val systemMessages = rawMessages.filter { it.role == ChatRole.SYSTEM }
        val dialogMessages = rawMessages.filter { it.role != ChatRole.SYSTEM }

        return state.copy(
            shortTerm = ShortTermMemory(
                rawMessages = rawMessages,
                derivedMessages = systemMessages + dialogMessages.takeLast(recentMessagesCount),
                strategyState = null
            )
        )
    }
}


