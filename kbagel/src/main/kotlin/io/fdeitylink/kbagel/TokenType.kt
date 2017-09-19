package io.fdeitylink.kbagel

internal enum class TokenType(chars: String? = null) {
    /*
     * Single-character tokens
     */
    LEFT_PAREN("("), RIGHT_PAREN(")"),
    LEFT_BRACE("{"), RIGHT_BRACE("}"),
    COMMA(","), DOT("."),
    MINUS("-"), PLUS("+"),
    SEMICOLON(";"),
    FORWARD_SLASH("/"), ASTERISK("*"),

    /*
     * One or more character tokens
     */
    BANG("!"), BANG_EQUAL("!="),
    EQUAL("="), EQUAL_EQUAL("=="),
    GREATER(">"), GREATER_EQUAL(">="),
    LESS("<"), LESS_EQUAL("<="),

    /*
     * Literals
     */
    IDENTIFIER(""), STRING(""), NUMBER(""),

    /*
     * Keywords
     */
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF("");

    val chars = chars ?: name.toLowerCase()

    //Empty: token with no proper representaiton; Length of 2 or more: Cannot be represented in one character
    val char = if (this.chars.isEmpty() || 1 < this.chars.length) '\u0000' else this.chars.first()

    companion object {
        val keywords = enumValues<TokenType>()
                            .filter { it.chars == it.name.toLowerCase() }
                            .associateBy(TokenType::chars)
    }
}