package agent.lifecycle

class ConsoleAgentLifecycleListener(
    private val loadingIndicator: LoadingIndicator
) : AgentLifecycleListener {
    override fun onModelWarmupStarted() {
        loadingIndicator.start("Подготовка модели")
    }

    override fun onModelWarmupFinished() {
        loadingIndicator.stop()
    }

    override fun onContextCompressionStarted() {
        loadingIndicator.start("Сжимаем контекст")
    }

    override fun onContextCompressionFinished() {
        loadingIndicator.stop()
    }
}
