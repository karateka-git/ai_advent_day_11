package agent.memory.layer

import agent.memory.model.MemoryLayer

/**
 * Описание допустимой категории заметки для одного из durable memory слоёв.
 *
 * @property id машинное имя категории.
 * @property layer слой памяти, к которому относится категория.
 * @property description короткое пояснение смысла категории.
 */
data class MemoryLayerCategoryDefinition(
    val id: String,
    val layer: MemoryLayer,
    val description: String
)

/**
 * Единый источник истины для допустимых категорий working и long-term памяти.
 */
object MemoryLayerCategories {
    private val categoryDefinitions: List<MemoryLayerCategoryDefinition> = listOf(
        MemoryLayerCategoryDefinition(
            id = "goal",
            layer = MemoryLayer.WORKING,
            description = "цель или ожидаемый результат текущей задачи"
        ),
        MemoryLayerCategoryDefinition(
            id = "constraint",
            layer = MemoryLayer.WORKING,
            description = "ограничение, требование или запрет для текущей задачи"
        ),
        MemoryLayerCategoryDefinition(
            id = "deadline",
            layer = MemoryLayer.WORKING,
            description = "срок, дата или временное ограничение текущей задачи"
        ),
        MemoryLayerCategoryDefinition(
            id = "budget",
            layer = MemoryLayer.WORKING,
            description = "бюджет или лимит ресурсов"
        ),
        MemoryLayerCategoryDefinition(
            id = "integration",
            layer = MemoryLayer.WORKING,
            description = "внешняя система, сервис или интеграция, нужная для задачи"
        ),
        MemoryLayerCategoryDefinition(
            id = "decision",
            layer = MemoryLayer.WORKING,
            description = "текущее принятое решение по задаче"
        ),
        MemoryLayerCategoryDefinition(
            id = "open_question",
            layer = MemoryLayer.WORKING,
            description = "открытый вопрос или нерешённая часть текущей задачи"
        ),
        MemoryLayerCategoryDefinition(
            id = "communication_style",
            layer = MemoryLayer.LONG_TERM,
            description = "устойчивое предпочтение по стилю общения"
        ),
        MemoryLayerCategoryDefinition(
            id = "persistent_preference",
            layer = MemoryLayer.LONG_TERM,
            description = "постоянное пользовательское предпочтение"
        ),
        MemoryLayerCategoryDefinition(
            id = "architectural_agreement",
            layer = MemoryLayer.LONG_TERM,
            description = "устойчивая архитектурная договорённость"
        ),
        MemoryLayerCategoryDefinition(
            id = "reusable_knowledge",
            layer = MemoryLayer.LONG_TERM,
            description = "повторно полезное знание о пользователе или проекте"
        )
    )

    val workingCategories: Set<String> =
        definitionsFor(MemoryLayer.WORKING).mapTo(linkedSetOf(), MemoryLayerCategoryDefinition::id)

    val longTermCategories: Set<String> =
        definitionsFor(MemoryLayer.LONG_TERM).mapTo(linkedSetOf(), MemoryLayerCategoryDefinition::id)

    fun definitionsFor(layer: MemoryLayer): List<MemoryLayerCategoryDefinition> =
        categoryDefinitions.filter { it.layer == layer }

    fun allowedCategoriesFor(layer: MemoryLayer): Set<String> =
        when (layer) {
            MemoryLayer.SHORT_TERM -> emptySet()
            MemoryLayer.WORKING -> workingCategories
            MemoryLayer.LONG_TERM -> longTermCategories
        }

    fun isCategoryAllowed(layer: MemoryLayer, category: String): Boolean =
        category in allowedCategoriesFor(layer)

    fun formatForPrompt(layer: MemoryLayer): String =
        definitionsFor(layer).joinToString(separator = "\n") { definition ->
            "- ${definition.id}: ${definition.description}"
        }
}
