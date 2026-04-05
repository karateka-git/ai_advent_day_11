package ui.cli

/**
 * Структурированная причина, по которой встроенная CLI-команда не может быть выполнена.
 */
sealed interface InvalidCliCommandReason {
    /**
     * Команда распознана, но пользователь передал неполную или неверную форму.
     */
    data class Usage(
        val usage: String
    ) : InvalidCliCommandReason

    /**
     * Для memory edit указано неподдерживаемое поле.
     */
    data class PendingEditUnsupportedField(
        val allowedFields: List<String> = listOf("text", "layer", "category")
    ) : InvalidCliCommandReason
}
