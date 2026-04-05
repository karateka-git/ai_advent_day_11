package ui.cli

import agent.memory.model.MemoryLayer

/**
 * Разбирает строку пользовательского ввода в типизированную CLI-команду.
 */
class CliCommandParser {
    /**
     * Преобразует пользовательский ввод в одну из встроенных команд или обычный prompt.
     */
    fun parse(input: String): CliCommand {
        val normalizedInput = input.trim()
        val compactInput = normalizedInput.replace(Regex("\\s+"), " ")
        return when {
            normalizedInput.isEmpty() -> CliCommand.Empty
            compactInput.equals(CliCommands.HELP, ignoreCase = true) -> CliCommand.ShowHelp
            compactInput.equals(CliCommands.EXIT, ignoreCase = true) ||
                compactInput.equals(CliCommands.QUIT, ignoreCase = true) -> CliCommand.Exit

            compactInput.equals(CliCommands.CLEAR, ignoreCase = true) -> CliCommand.Clear
            compactInput.equals(CliCommands.MODELS, ignoreCase = true) -> CliCommand.ShowModels
            compactInput.equals(CliCommands.MEMORY, ignoreCase = true) -> CliCommand.ShowMemory(null)
            compactInput.equals("${CliCommands.MEMORY} short", ignoreCase = true) -> CliCommand.ShowMemory(MemoryLayer.SHORT_TERM)
            compactInput.equals("${CliCommands.MEMORY} working", ignoreCase = true) -> CliCommand.ShowMemory(MemoryLayer.WORKING)
            compactInput.equals("${CliCommands.MEMORY} long", ignoreCase = true) -> CliCommand.ShowMemory(MemoryLayer.LONG_TERM)
            compactInput.equals(PendingMemoryCliCatalog.SHOW, ignoreCase = true) -> CliCommand.ShowPendingMemory
            compactInput.equals(PendingMemoryCliCatalog.INFO, ignoreCase = true) -> CliCommand.ShowPendingMemoryCommands
            compactInput.equals("${CliCommands.MEMORY} approve", ignoreCase = true) -> CliCommand.ApprovePendingMemory(emptyList())
            compactInput.startsWith("${CliCommands.MEMORY} approve ", ignoreCase = true) ->
                CliCommand.ApprovePendingMemory(parseIds(compactInput.substringAfter("${CliCommands.MEMORY} approve ").trim()))
            compactInput.equals("${CliCommands.MEMORY} reject", ignoreCase = true) -> CliCommand.RejectPendingMemory(emptyList())
            compactInput.startsWith("${CliCommands.MEMORY} reject ", ignoreCase = true) ->
                CliCommand.RejectPendingMemory(parseIds(compactInput.substringAfter("${CliCommands.MEMORY} reject ").trim()))
            compactInput.equals("${CliCommands.MEMORY} edit", ignoreCase = true) ->
                CliCommand.InvalidCommand(
                    InvalidCliCommandReason.Usage("memory edit <id> text|layer|category <значение>")
                )
            compactInput.startsWith("${CliCommands.MEMORY} edit", ignoreCase = true) ->
                parsePendingEdit(compactInput.substringAfter("${CliCommands.MEMORY} edit").trim())
            compactInput.equals(CliCommands.CHECKPOINT, ignoreCase = true) ->
                CliCommand.CreateCheckpoint(null)
            compactInput.startsWith("${CliCommands.CHECKPOINT} ", ignoreCase = true) ->
                CliCommand.CreateCheckpoint(compactInput.substringAfter(' ').trim())
            compactInput.equals(CliCommands.BRANCH, ignoreCase = true) ->
                CliCommand.InvalidCommand(
                    InvalidCliCommandReason.Usage("branch create <name> или branch use <name>")
                )
            compactInput.equals("${CliCommands.BRANCH} create", ignoreCase = true) ->
                CliCommand.InvalidCommand(InvalidCliCommandReason.Usage("branch create <name>"))
            compactInput.equals(CliCommands.BRANCHES, ignoreCase = true) ->
                CliCommand.ShowBranches
            compactInput.startsWith("${CliCommands.BRANCH} create ", ignoreCase = true) ->
                CliCommand.CreateBranch(compactInput.substringAfter("${CliCommands.BRANCH} create ").trim())
            compactInput.equals("${CliCommands.BRANCH} use", ignoreCase = true) ->
                CliCommand.InvalidCommand(InvalidCliCommandReason.Usage("branch use <name>"))
            compactInput.startsWith("${CliCommands.BRANCH} use ", ignoreCase = true) ->
                CliCommand.SwitchBranch(compactInput.substringAfter("${CliCommands.BRANCH} use ").trim())
            compactInput.equals(CliCommands.USE, ignoreCase = true) ->
                CliCommand.InvalidCommand(InvalidCliCommandReason.Usage("use <model_id>"))
            compactInput.startsWith("${CliCommands.USE} ", ignoreCase = true) ->
                CliCommand.SwitchModel(compactInput.substringAfter(' ').trim())

            else -> CliCommand.UserPrompt(input)
        }
    }

    private fun parseIds(rawValue: String): List<String> =
        rawValue.split(' ')
            .map(String::trim)
            .filter(String::isNotEmpty)

    private fun parsePendingEdit(rawValue: String): CliCommand {
        val parts = rawValue.split(Regex("\\s+"), limit = 3)
        if (parts.size < 3) {
            return CliCommand.InvalidCommand(
                InvalidCliCommandReason.Usage("memory edit <id> text|layer|category <значение>")
            )
        }

        val candidateId = parts[0]
        val field = parts[1]
        val value = parts[2].trim()
        return when (field.lowercase()) {
            "text" -> CliCommand.EditPendingMemory(candidateId, "text", value)
            "layer" -> CliCommand.EditPendingMemory(candidateId, "layer", value)
            "category" -> CliCommand.EditPendingMemory(candidateId, "category", value)
            else -> CliCommand.InvalidCommand(InvalidCliCommandReason.PendingEditUnsupportedField())
        }
    }
}

/**
 * Типизированные команды CLI после разбора пользовательского ввода.
 */
sealed interface CliCommand {
    data object Empty : CliCommand
    data object ShowHelp : CliCommand
    data object Exit : CliCommand
    data object Clear : CliCommand
    data object ShowModels : CliCommand
    data class ShowMemory(val layer: MemoryLayer?) : CliCommand
    data object ShowPendingMemory : CliCommand
    data object ShowPendingMemoryCommands : CliCommand
    data class ApprovePendingMemory(val ids: List<String>) : CliCommand
    data class RejectPendingMemory(val ids: List<String>) : CliCommand
    data class EditPendingMemory(val id: String, val field: String, val value: String) : CliCommand
    data class InvalidCommand(val reason: InvalidCliCommandReason) : CliCommand
    data class CreateCheckpoint(val name: String?) : CliCommand
    data object ShowBranches : CliCommand
    data class CreateBranch(val name: String) : CliCommand
    data class SwitchBranch(val name: String) : CliCommand
    data class SwitchModel(val modelId: String) : CliCommand
    data class UserPrompt(val value: String) : CliCommand
}
