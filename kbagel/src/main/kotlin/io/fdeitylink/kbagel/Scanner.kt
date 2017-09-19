package io.fdeitylink.kbagel

internal class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()

    private inline val isAtEnd get() = curr >= source.length

    private var start = 0
    private var curr = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd) {
            start = curr
            scanToken()
        }

        tokens += Token(TokenType.EOF, "", line)
        return tokens
    }

    private fun scanToken() {
        /*
         * If the current character matches the given token (which should be
         * a single character token), the current index is advanced and the
         * method returns true. Returns false otherwise.
         */
        fun match(token: TokenType): Boolean {
            if (isAtEnd || source[curr] != token.char) {
                return false
            }

            curr++
            return true
        }

        val c = advance()
        when (c) {
            /*
             * Single-character tokens
             */
            TokenType.LEFT_PAREN.char -> addToken(TokenType.LEFT_PAREN)
            TokenType.RIGHT_PAREN.char -> addToken(TokenType.RIGHT_PAREN)

            TokenType.LEFT_BRACE.char -> addToken(TokenType.LEFT_BRACE)
            TokenType.RIGHT_BRACE.char -> addToken(TokenType.RIGHT_BRACE)

            TokenType.COMMA.char -> addToken(TokenType.COMMA)
            TokenType.DOT.char -> addToken(TokenType.DOT)

            TokenType.MINUS.char -> addToken(TokenType.MINUS)
            TokenType.PLUS.char -> addToken(TokenType.PLUS)

            TokenType.SEMICOLON.char -> addToken(TokenType.SEMICOLON)

            TokenType.FORWARD_SLASH.char -> {
                when {
                    match(TokenType.FORWARD_SLASH) -> {
                        /*
                         * Once the next character is a newline, exit the loop.
                         * Then the next character matched will be a newline, which
                         * will cause the line counter to be incremented.
                         */
                        while ('\n' != peek() && !isAtEnd) {
                            advance()
                        }
                    }

                    match(TokenType.ASTERISK) -> {
                        //Peek the next two characters and check that we haven't met the end
                        while (!(TokenType.ASTERISK.char == peek() &&
                                TokenType.FORWARD_SLASH.char == peek(1)) &&
                                !isAtEnd) {
                            if ('\n' == peek()) {
                                line++
                            }

                            advance()
                        }

                        if (isAtEnd) {
                            report(line, "Unterminated block comment")
                            return
                        }

                        //Advance once for each character that markes the end of a block comment (* and /)
                        advance()
                        advance()
                    }

                    else -> addToken(TokenType.FORWARD_SLASH)
                }
            }

            TokenType.ASTERISK.char -> addToken(TokenType.ASTERISK)

            /*
             * One or more character tokens
             */
            TokenType.BANG.char -> addToken(if (match(TokenType.EQUAL)) TokenType.BANG_EQUAL else TokenType.BANG)
            TokenType.EQUAL.char -> addToken(if (match(TokenType.EQUAL)) TokenType.EQUAL_EQUAL else TokenType.EQUAL)

            TokenType.GREATER.char -> addToken(if (match(TokenType.EQUAL)) TokenType.GREATER_EQUAL else TokenType.GREATER)
            TokenType.LESS.char -> addToken(if (match(TokenType.EQUAL)) TokenType.LESS_EQUAL else TokenType.LESS)

            '\"' -> parseStringLiteral()

            '\n' -> line++

            else -> {
                if (!c.isWhitespace()) {
                    when {
                        isDigit(c) -> parseNumberLiteral()
                        isAlpha(c) -> parseIdentifier()
                        else -> report(line, "Unexpected token '$c'")
                    }
                }
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isDigit(c: Char) = c in '0'..'9'

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isAlpha(c: Char) = c in ('a'..'z') + ('A'..'Z') + '_'

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isAlphaNumeric(c: Char) = isDigit(c) || isAlpha(c)

    /**
     * Returns the character in [source] at [curr] + [numCharsAhead]
     * @param numCharsAhead The number of characters ahead of the current character to peek. Defaults to 0.
     */
    private fun peek(numCharsAhead: Int = 0) =
            if (curr + numCharsAhead >= source.length) '\u0000' else source[curr + numCharsAhead]

    @Suppress("NOTHING_TO_INLINE")
    private inline fun advance() = source[curr++]

    private fun addToken(type: TokenType, literal: Any? = null) {
        val lexeme = source.substring(start, curr)
        tokens += Token(type, lexeme, line, literal)
    }

    private fun parseStringLiteral() {
        while ('"' != peek() && !isAtEnd) {
            //Multi-line strings are permitted
            if ('\n' == peek()) {
                line++
            }

            advance()
        }

        if (isAtEnd) {
            report(line, "Unterminated string literal")
            return
        }

        //We've met the ending double quote
        advance()

        val str = source.substring(start + 1, curr - 1)
        addToken(TokenType.STRING, str)
    }

    private fun parseNumberLiteral() {
        while (isDigit(peek())) {
            advance()
        }

        if ('.' == peek() && isDigit(peek(1))) {
            advance()

            while (isDigit(peek())) {
                advance()
            }
        }

        val n = source.substring(start, curr).toDouble()
        addToken(TokenType.NUMBER, n)
    }

    private fun parseIdentifier() {
        while (isAlphaNumeric(peek())) {
            advance()
        }

        val ident = source.substring(start, curr)

        addToken(TokenType.keywords[ident] ?: TokenType.IDENTIFIER)
    }
}