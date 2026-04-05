package ui.cli

import app.output.HelpCommandDescriptor

/**
 * Единый каталог основных CLI-команд приложения.
 */
object GeneralCliCatalog {
    val helpCommands: List<HelpCommandDescriptor> = listOf(
        HelpCommandDescriptor(CliCommands.HELP, "Показать общий список команд."),
        HelpCommandDescriptor(CliCommands.MODELS, "Показать доступные модели."),
        HelpCommandDescriptor("${CliCommands.USE} <model_id>", "Переключить модель и выбрать стратегию памяти."),
        HelpCommandDescriptor(CliCommands.CLEAR, "Очистить текущий контекст диалога."),
        HelpCommandDescriptor(CliCommands.MEMORY, "Показать все слои памяти."),
        HelpCommandDescriptor("${CliCommands.MEMORY} short", "Показать краткосрочную память."),
        HelpCommandDescriptor("${CliCommands.MEMORY} working", "Показать рабочую память."),
        HelpCommandDescriptor("${CliCommands.MEMORY} long", "Показать долговременную память."),
        HelpCommandDescriptor(PendingMemoryCliCatalog.SHOW, "Показать pending-кандидаты на сохранение."),
        HelpCommandDescriptor(PendingMemoryCliCatalog.INFO, "Показать команды для работы с pending-памятью."),
        HelpCommandDescriptor(CliCommands.CHECKPOINT, "Создать checkpoint активной ветки."),
        HelpCommandDescriptor("${CliCommands.CHECKPOINT} <name>", "Создать именованный checkpoint."),
        HelpCommandDescriptor(CliCommands.BRANCHES, "Показать список веток."),
        HelpCommandDescriptor("${CliCommands.BRANCH} create <name>", "Создать новую ветку от последнего checkpoint."),
        HelpCommandDescriptor("${CliCommands.BRANCH} use <name>", "Переключиться на существующую ветку."),
        HelpCommandDescriptor(CliCommands.EXIT, "Завершить работу."),
        HelpCommandDescriptor(CliCommands.QUIT, "Завершить работу.")
    )
}
