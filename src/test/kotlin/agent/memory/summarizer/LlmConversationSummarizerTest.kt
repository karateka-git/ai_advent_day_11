package agent.memory.summarizer

import agent.lifecycle.AgentLifecycleListener
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import llm.core.LanguageModel
import llm.core.model.ChatMessage
import llm.core.model.ChatRole
import llm.core.model.LanguageModelInfo
import llm.core.model.LanguageModelResponse

class LlmConversationSummarizerTest {
    @Test
    fun `summarize sends dedicated summary prompt to language model and notifies listener`() {
        val languageModel = RecordingLanguageModel("Краткое summary")
        val lifecycleListener = RecordingAgentLifecycleListener()
        val summarizer = LlmConversationSummarizer(
            languageModel = languageModel,
            lifecycleListener = lifecycleListener
        )
        val firstUserMessage = "Меня зовут Илья."
        val assistantMessage = "Приятно познакомиться."
        val secondUserMessage = "Мне важна экономия токенов."

        val summary = summarizer.summarize(
            listOf(
                ChatMessage(role = ChatRole.USER, content = firstUserMessage),
                ChatMessage(role = ChatRole.ASSISTANT, content = assistantMessage),
                ChatMessage(role = ChatRole.USER, content = secondUserMessage)
            )
        )

        assertEquals("Краткое summary", summary)
        assertEquals(1, lifecycleListener.contextCompressionStartedCount)
        assertEquals(1, lifecycleListener.contextCompressionFinishedCount)
        assertEquals(0, lifecycleListener.modelWarmupStartedCount)
        assertEquals(0, lifecycleListener.modelWarmupFinishedCount)
        assertEquals(2, languageModel.recordedMessages.size)
        assertEquals(ChatRole.SYSTEM, languageModel.recordedMessages[0].role)
        assertEquals(ChatRole.USER, languageModel.recordedMessages[1].role)
        assertContains(languageModel.recordedMessages[0].content, "summary")
        assertContains(languageModel.recordedMessages[1].content, ChatRole.USER.displayName)
        assertContains(languageModel.recordedMessages[1].content, ChatRole.ASSISTANT.displayName)
        assertContains(languageModel.recordedMessages[1].content, firstUserMessage)
        assertContains(languageModel.recordedMessages[1].content, assistantMessage)
        assertContains(languageModel.recordedMessages[1].content, secondUserMessage)
    }
}

private class RecordingAgentLifecycleListener : AgentLifecycleListener {
    var modelWarmupStartedCount: Int = 0
        private set
    var modelWarmupFinishedCount: Int = 0
        private set
    var contextCompressionStartedCount: Int = 0
        private set
    var contextCompressionFinishedCount: Int = 0
        private set

    override fun onModelWarmupStarted() {
        modelWarmupStartedCount++
    }

    override fun onModelWarmupFinished() {
        modelWarmupFinishedCount++
    }

    override fun onContextCompressionStarted() {
        contextCompressionStartedCount++
    }

    override fun onContextCompressionFinished() {
        contextCompressionFinishedCount++
    }
}

private class RecordingLanguageModel(
    private val responseContent: String
) : LanguageModel {
    var recordedMessages: List<ChatMessage> = emptyList()
        private set

    override val info = LanguageModelInfo(
        name = "RecordingLanguageModel",
        model = "recording-model"
    )

    override val tokenCounter = null

    override fun complete(messages: List<ChatMessage>): LanguageModelResponse {
        recordedMessages = messages
        return LanguageModelResponse(content = responseContent)
    }
}
