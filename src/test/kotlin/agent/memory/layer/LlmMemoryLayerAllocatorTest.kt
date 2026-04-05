package agent.memory.layer

import agent.memory.model.MemoryLayer
import agent.memory.model.MemoryNote
import agent.memory.model.MemoryState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import llm.core.LanguageModel
import llm.core.model.ChatMessage
import llm.core.model.ChatRole
import llm.core.model.LanguageModelInfo
import llm.core.model.LanguageModelResponse

class LlmMemoryLayerAllocatorTest {
    @Test
    fun `system prompt is built from shared category definitions`() {
        val prompt = LlmMemoryLayerAllocatorPromptBuilder().buildSystemPrompt()

        MemoryLayerCategories.definitionsFor(MemoryLayer.WORKING).forEach { definition ->
            assertTrue(prompt.contains("- ${definition.id}: ${definition.description}"))
        }
        MemoryLayerCategories.definitionsFor(MemoryLayer.LONG_TERM).forEach { definition ->
            assertTrue(prompt.contains("- ${definition.id}: ${definition.description}"))
        }
    }

    @Test
    fun `extractor parses json response into memory notes`() {
        val extractor = LlmConversationMemoryLayerAllocationExtractor(
            languageModel = FakeLanguageModel(
                """
                {
                  "working": [
                    { "category": "goal", "content": "Telegram-бот для записи на урок" }
                  ],
                  "longTerm": [
                    { "category": "communication_style", "content": "Отвечать кратко и на русском" }
                  ]
                }
                """.trimIndent()
            )
        )

        val extraction = extractor.extract(
            state = MemoryState(),
            message = ChatMessage(role = ChatRole.USER, content = "Цель проекта и стиль общения")
        )

        assertEquals(
            listOf(MemoryNote(category = "goal", content = "Telegram-бот для записи на урок")),
            extraction.workingNotes
        )
        assertEquals(
            listOf(MemoryNote(category = "communication_style", content = "Отвечать кратко и на русском")),
            extraction.longTermNotes
        )
    }

    @Test
    fun `allocator returns candidates instead of immediately merged memory`() {
        val allocator = LlmMemoryLayerAllocator(
            extractor = object : LlmMemoryLayerAllocationExtractor {
                override fun extract(state: MemoryState, message: ChatMessage): LlmMemoryLayerExtraction =
                    LlmMemoryLayerExtraction(
                        workingNotes = listOf(MemoryNote(category = "goal", content = "Подготовить production-версию")),
                        longTermNotes = listOf(MemoryNote(category = "communication_style", content = "Отвечать подробно"))
                    )
            }
        )

        val candidates = allocator.extractCandidates(
            state = MemoryState(),
            message = ChatMessage(role = ChatRole.USER, content = "Обновляю цель и стиль общения")
        )

        assertEquals(
            listOf(MemoryLayer.WORKING, MemoryLayer.LONG_TERM),
            candidates.map { it.targetLayer }
        )
        assertEquals(
            listOf("goal", "communication_style"),
            candidates.map { it.category }
        )
    }
}

private class FakeLanguageModel(
    private val responseContent: String
) : LanguageModel {
    override val info: LanguageModelInfo = LanguageModelInfo(
        name = "FakeLanguageModel",
        model = "fake-model"
    )

    override val tokenCounter = null

    override fun complete(messages: List<ChatMessage>): LanguageModelResponse =
        LanguageModelResponse(content = responseContent)
}
