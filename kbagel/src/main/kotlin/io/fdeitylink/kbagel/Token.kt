package io.fdeitylink.kbagel

internal data class Token(val type: TokenType, val lexeme: String, val line: Int, val literal: Any? = null) {
    override fun toString() = "$type $lexeme $literal"
}