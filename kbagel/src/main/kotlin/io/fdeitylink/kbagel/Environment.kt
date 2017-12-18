package io.fdeitylink.kbagel

@Suppress("NOTHING_TO_INLINE")
internal class Environment(private val parent: Environment? = null) {
    private val map = mutableMapOf<String, Any?>()

    operator fun get(name: IdentifierToken): Any? = when {
        name.lexeme in map -> map[name.lexeme]
        parent != null -> parent[name]
        else -> throw BagelRuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    inline operator fun set(name: IdentifierToken, value: Any?) = def(name.lexeme, value)

    inline operator fun set(name: String, value: Any?) = def(name, value)

    inline fun def(name: IdentifierToken, value: Any?) = def(name.lexeme, value)

    inline fun def(name: String, value: Any?) {
        map[name] = value
    }

    fun assign(name: IdentifierToken, value: Any?): Unit = when {
        name.lexeme in map -> map[name.lexeme] = value
        parent != null -> parent.assign(name, value)
        else -> throw BagelRuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}