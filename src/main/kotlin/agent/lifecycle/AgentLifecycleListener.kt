package agent.lifecycle

interface AgentLifecycleListener {
    /**
     * Вызывается перед прогревом локального токенизатора для выбранной модели.
     */
    fun onModelWarmupStarted()

    /**
     * Вызывается после завершения прогрева локального токенизатора.
     */
    fun onModelWarmupFinished()

    /**
     * Вызывается перед запуском LLM-сжатия истории диалога.
     */
    fun onContextCompressionStarted()

    /**
     * Вызывается после завершения LLM-сжатия истории диалога.
     */
    fun onContextCompressionFinished()
}
