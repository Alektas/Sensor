package alektas.sensor.domain

/**
 * Disposable event that returns containing value only once.
 * After the first requesting returns 'null'.
 *
 * @param <T> entity that this event contain
 */
class DisposableContainer<T>(private val value: T) {
    private var isHandled = false

    /**
     * Disposable request for the entity that is in the container.
     *
     * @return entity at the first request or null after.
     */
    fun getValue(): T? {
        if (isHandled) return null
        isHandled = true
        return value
    }

    fun peek(): T = value
}