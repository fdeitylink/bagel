package io.fdeitylink.kbagel

internal abstract class ErrorReporter {
    abstract var hadError: Boolean
        protected set

    /**
     * Reports an error with a particular token. Sets [hadError] to `true`.
     *
     * @param token The problematic token
     * @param lazyMessage An additional message that describes the error
     */
    open fun report(token: Token<*>, lazyMessage: () -> Any) = when (token.type) {
        EOFToken.Type.EOF -> report(token.line, " at end", lazyMessage)
        else -> report(token.line, " at '${token.lexeme}'", lazyMessage)
    }

    /**
     * Reports a given error message. Sets [hadError] to `true`.
     *
     * @param line The line number in the source file of the error
     * @param location The part of the source code that is problematic
     * @param lazyMessage An additional message that describes the error
     */
    abstract fun report(line: Int, location: String = "", lazyMessage: () -> Any)
}