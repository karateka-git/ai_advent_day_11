import agent.core.AgentTokenStats
import agent.memory.model.MemoryLayer
import agent.memory.model.MemorySnapshot
import agent.memory.model.MemoryState
import agent.memory.model.PendingMemoryCandidate
import agent.memory.model.PendingMemoryState
import agent.memory.model.ShortTermMemory
import agent.memory.strategy.MemoryStrategyType
import app.output.AppEvent
import app.output.HelpCommandDescriptor
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import llm.core.model.ChatMessage
import llm.core.model.ChatRole
import llm.core.model.TokenUsage
import ui.cli.CliRenderer

class CliRendererTest {
    @Test
    fun `session start prints bordered help hint only`() {
        val output = captureStdout {
            CliRenderer().emit(AppEvent.SessionStarted)
        }

        assertTrue(output.contains("┌─ Старт"))
        assertTrue(output.contains("help"))
        assertFalse(output.contains("models"))
        assertFalse(output.contains("use <id>"))
    }

    @Test
    fun `general help prints bordered commands block`() {
        val output = captureStdout {
            CliRenderer().emit(
                AppEvent.CommandsAvailable(
                    title = "Commands",
                    commands = listOf(
                        HelpCommandDescriptor("help", "Show commands."),
                        HelpCommandDescriptor("memory", "Show memory.")
                    )
                )
            )
        }

        assertTrue(output.contains("┌─ Commands"))
        assertTrue(output.contains("│ help"))
        assertTrue(output.contains("│   Show commands."))
        assertTrue(output.contains("│ memory"))
    }

    @Test
    fun `models output is bordered`() {
        val output = captureStdout {
            CliRenderer().emit(
                AppEvent.ModelsAvailable(
                    options = emptyList(),
                    currentModelId = "timeweb"
                )
            )
        }

        assertTrue(output.contains("┌─ Доступные модели"))
    }

    @Test
    fun `auto pending notification is rendered as command result`() {
        val output = captureStdout {
            CliRenderer().emit(
                AppEvent.PendingMemoryAvailable(
                    pending = samplePending(),
                    reason = "Есть кандидаты на сохранение в память. Посмотри их командой memory pending."
                )
            )
        }

        assertTrue(output.contains("┌─ Результат команды"))
        assertTrue(output.contains("memory pending"))
        assertFalse(output.contains("p1"))
    }

    @Test
    fun `pending action result is bordered and does not print candidates list`() {
        val output = captureStdout {
            CliRenderer().emit(
                AppEvent.PendingMemoryActionCompleted(
                    message = "Approved pending candidates: p1",
                    pending = samplePending()
                )
            )
        }

        assertTrue(output.contains("┌─ Результат команды"))
        assertTrue(output.contains("Approved pending candidates: p1"))
        assertFalse(output.contains("memory pending info"))
    }

    @Test
    fun `manual pending view prints bordered block with help hint`() {
        val output = captureStdout {
            CliRenderer().emit(
                AppEvent.PendingMemoryAvailable(
                    pending = samplePending()
                )
            )
        }

        assertTrue(output.contains("┌─ Pending-память"))
        assertTrue(output.contains("p1"))
        assertTrue(output.contains("2 weeks"))
        assertTrue(output.contains("memory pending info"))
    }

    @Test
    fun `memory view is bordered and does not show raw short term or system derived message`() {
        val output = captureStdout {
            CliRenderer().emit(
                AppEvent.MemoryStateAvailable(
                    snapshot = MemorySnapshot(
                        state = MemoryState(
                            shortTerm = ShortTermMemory(
                                rawMessages = listOf(
                                    ChatMessage(
                                        role = ChatRole.SYSTEM,
                                        content = "Raw line 1\nRaw line 2"
                                    )
                                ),
                                derivedMessages = listOf(
                                    ChatMessage(
                                        role = ChatRole.SYSTEM,
                                        content = "Derived system"
                                    ),
                                    ChatMessage(
                                        role = ChatRole.USER,
                                        content = "Derived user"
                                    )
                                )
                            )
                        ),
                        shortTermStrategyType = MemoryStrategyType.NO_COMPRESSION
                    ),
                    selectedLayer = null
                )
            )
        }.replace("\r\n", "\n")

        assertTrue(output.contains("┌─ Память"))
        assertTrue(output.contains("Используемая стратегия: no_compression"))
        assertFalse(output.contains("Raw line 1"))
        assertFalse(output.contains("Derived system"))
        assertFalse(output.contains("система:"))
        assertTrue(output.contains("пользователь: Derived user"))
    }

    @Test
    fun `memory short hides system messages`() {
        val output = captureStdout {
            CliRenderer().emit(
                AppEvent.MemoryStateAvailable(
                    snapshot = MemorySnapshot(
                        state = MemoryState(
                            shortTerm = ShortTermMemory(
                                derivedMessages = listOf(
                                    ChatMessage(role = ChatRole.SYSTEM, content = "System line"),
                                    ChatMessage(role = ChatRole.USER, content = "User line"),
                                    ChatMessage(role = ChatRole.ASSISTANT, content = "Assistant line")
                                )
                            )
                        ),
                        shortTermStrategyType = MemoryStrategyType.NO_COMPRESSION
                    ),
                    selectedLayer = MemoryLayer.SHORT_TERM
                )
            )
        }

        assertFalse(output.contains("система:"))
        assertFalse(output.contains("System line"))
        assertTrue(output.contains("пользователь: User line"))
        assertTrue(output.contains("ассистент: Assistant line"))
    }

    @Test
    fun `user prompt is bordered`() {
        val output = captureStdout {
            CliRenderer().emit(AppEvent.UserInputPrompt(ChatRole.USER))
        }

        assertTrue(output.contains("┌─ Пользователь"))
        assertTrue(output.contains("│ "))
    }

    @Test
    fun `assistant response is bordered`() {
        val output = captureStdout {
            CliRenderer().emit(
                AppEvent.AssistantResponseAvailable(
                    role = ChatRole.ASSISTANT,
                    content = "Короткий ответ",
                    tokenStats = AgentTokenStats(
                        apiUsage = TokenUsage(
                            promptTokens = 10,
                            completionTokens = 5,
                            totalTokens = 15
                        )
                    )
                )
            )
        }

        assertTrue(output.contains("┌─ Ассистент"))
        assertTrue(output.contains("│ Короткий ответ"))
        assertTrue(output.contains("Токены ответа:"))
    }

    @Test
    fun `token preview is bordered`() {
        val output = captureStdout {
            CliRenderer().emit(
                AppEvent.TokenPreviewAvailable(
                    AgentTokenStats(
                        userPromptTokens = 12,
                        historyTokens = 34,
                        promptTokensLocal = 46
                    )
                )
            )
        }

        assertTrue(output.contains("┌─ Оценка перед запросом"))
        assertTrue(output.contains("текущее сообщение: 12"))
        assertTrue(output.contains("история диалога: 34"))
    }

    private fun samplePending(): PendingMemoryState =
        PendingMemoryState(
            candidates = listOf(
                PendingMemoryCandidate(
                    id = "p1",
                    targetLayer = MemoryLayer.WORKING,
                    category = "deadline",
                    content = "2 weeks",
                    sourceRole = ChatRole.USER,
                    sourceMessage = "Deadline is 2 weeks"
                )
            ),
            nextId = 2
        )

    private fun captureStdout(block: () -> Unit): String {
        val originalOut = System.out
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer, true, Charsets.UTF_8))
        return try {
            block()
            buffer.toString(Charsets.UTF_8)
        } finally {
            System.setOut(originalOut)
        }
    }
}
