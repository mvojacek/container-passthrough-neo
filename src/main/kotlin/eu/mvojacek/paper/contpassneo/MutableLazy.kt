package eu.mvojacek.paper.contpassneo

import java.io.Serializable
import kotlin.reflect.KProperty

interface MutableLazy<T> : Lazy<T> {
    override var value: T
}

fun <T> mutableLazy(initializer: () -> T): MutableLazy<T> = UnsafeMutableLazy(initializer)

operator fun <T> MutableLazy<T>.setValue(config: Config, property: KProperty<*>, value: T) {
    this.value = value
}

private class UnsafeMutableLazy<T>(initializer: () -> T) : MutableLazy<T>, Serializable {
    private var initializer: (() -> T)? = initializer
    private var _value: Any? = UNINITIALIZED_VALUE

    override var value: T
        get() {
            if (_value === UNINITIALIZED_VALUE) {
                _value = initializer!!()
                initializer = null
            }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }
        set(value: T) {
            _value = value
            initializer = null
        }


    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    companion object {
        @Suppress("ClassName")
        private object UNINITIALIZED_VALUE
    }
}