package io.fdeitylink.kbagel

internal abstract class ErrorReporter {
    abstract var hadScanParseError: Boolean
        protected set

    abstract var hadRuntimeError: Boolean
        protected set

    /**
     * Reports an error with a particular token. Sets [hadScanParseError] to `true`.
     *
     * @param token The problematic token
     * @param message An additional message that describes the error
     */
    open fun report(token: Token<*>, message: String) = when (token.type) {
        EOFToken.Type.EOF -> report(token.line, message, " at end")
        else -> report(token.line, message, " at '${token.lexeme}'")
    }

    /**
     * Reports a given error message. Sets [hadScanParseError] to `true`.
     *
     * @param line The line number in the source file of the error
     * @param location The part of the source code that is problematic
     * @param message An additional message that describes the error
     */
    abstract fun report(line: Int, message: String, location: String = "")

    /**
     * Reports a given [BagelRuntimeError]. Sets [hadRuntimeError] to `true`.
     */
    abstract fun report(err: BagelRuntimeError)
}