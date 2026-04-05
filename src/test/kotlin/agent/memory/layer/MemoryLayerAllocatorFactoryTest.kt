package agent.memory.layer

import agent.memory.model.MemoryState
import java.net.http.HttpClient
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import llm.core.model.ChatMessage
import llm.core.model.ChatRole

class MemoryLayerAllocatorFactoryTest {
    @Test
    fun `returns rule-based allocator when hugging face token is absent`() {
        val allocator = MemoryLayerAllocatorFactory.create(
            config = Properties(),
            httpClient = HttpClient.newHttpClient()
        )

        assertIs<RuleBasedMemoryLayerAllocator>(allocator)
    }

    @Test
    fun `returns fallback allocator when hugging face token is configured`() {
        val allocator = MemoryLayerAllocatorFactory.create(
            config = Properties().apply {
                setProperty("HF_API_TOKEN", "hf-test-token")
            },
            httpClient = HttpClient.newHttpClient()
        )

        assertIs<FallbackMemoryLayerAllocator>(allocator)
    }

    @Test
    fun `fallback allocator uses reserve allocator when primary fails`() {
        val allocator = FallbackMemoryLayerAllocator(
            primary = object : MemoryLayerAllocator {
                override fun extractCandidates(state: MemoryState, message: ChatMessage) =
                    error("primary failed")
            },
            fallback = RuleBasedMemoryLayerAllocator()
        )

        val candidates = allocator.extractCandidates(
            state = MemoryState(),
            message = ChatMessage(
                role = ChatRole.USER,
                content = "Цель задачи - сделать MVP"
            )
        )

        assertEquals("goal", candidates.first().category)
    }
}
