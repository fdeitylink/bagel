package io.fdeitylink.kbagel

internal class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token<*>>()

    private inline val isAtEnd get() = curr >= source.length

    private var start = 0
    private var curr = 0
    private var line = 1

    fun scanTokens(): List<Token<*>> {
        while (!isAtEnd) {
            start = curr
            scanToken()
        }

        tokens += EOFToken(line)
        return tokens
    }

    private fun scanToken() {
        /**
         * If the character pointed to by [curr] matches the given [token],
         * [curr] is advanced and the method returns true. Returns false
         * otherwise. Useful for checking tokens like '!=' that start with a
         * character that is also a token.
         */
        fun match(token: SingleCharToken.Type): Boolean {
            if (isAtEnd || source[curr] != token.char) {
                return false
            }

            curr++
            return true
        }

        fun comparison(baseComparison: SingleCharToken.Type, compoundComparison: MultiCharToken.Type) {
            tokens += if (match(SingleCharToken.Type.EQUAL)) {
                MultiCharToken(compoundComparison, line)
            }
            else {
                SingleCharToken(baseComparison, line)
            }
        }

        val c = advance()
        when (c) {
            SingleCharToken.Type.LEFT_PAREN.char -> tokens += SingleCharToken(SingleCharToken.Type.LEFT_PAREN, line)
            SingleCharToken.Type.RIGHT_PAREN.char -> tokens += SingleCharToken(SingleCharToken.Type.RIGHT_PAREN, line)

            SingleCharToken.Type.LEFT_BRACE.char -> tokens += SingleCharToken(SingleCharToken.Type.LEFT_BRACE, line)
            SingleCharToken.Type.RIGHT_BRACE.char -> tokens += SingleCharToken(SingleCharToken.Type.RIGHT_BRACE, line)

            SingleCharToken.Type.COMMA.char -> tokens += SingleCharToken(SingleCharToken.Type.COMMA, line)
            SingleCharToken.Type.DOT.char -> tokens += SingleCharToken(SingleCharToken.Type.DOT, line)

            SingleCharToken.Type.MINUS.char -> tokens += SingleCharToken(SingleCharToken.Type.MINUS, line)
            SingleCharToken.Type.PLUS.char -> tokens += SingleCharToken(SingleCharToken.Type.PLUS, line)

            SingleCharToken.Type.SEMICOLON.char -> tokens += SingleCharToken(SingleCharToken.Type.SEMICOLON, line)

            SingleCharToken.Type.FORWARD_SLASH.char -> {
                when {
                    match(SingleCharToken.Type.FORWARD_SLASH) -> {
                        /*
                         * Once the next character is a newline, exit the loop.
                         * The next time this method is called, c will be the
                         * newline character, which will cause the line counter
                         * to be incremented.
                         */
                        while (!isAtEnd && Characters.NEWLINE != peek()) {
                            advance()
                        }
                    }

                    match(SingleCharToken.Type.ASTERISK) -> {
                        //Peek the next two characters and check that we haven't met the end
                        while (!isAtEnd &&
                               !(SingleCharToken.Type.ASTERISK.char == peek() &&
                                 SingleCharToken.Type.FORWARD_SLASH.char == peek(1))) {
                            if (Characters.NEWLINE == peek()) {
                                line++
                            }

                            advance()
                        }

                        if (isAtEnd) {
                            KBagel.report(line) { "Unterminated block comment" }
                            return
                        }

                        //Advance once for each character that marks the end of a block comment (* and /)
                        advance()
                        advance()
                    }

                    else -> tokens += SingleCharToken(SingleCharToken.Type.FORWARD_SLASH, line)
                }
            }

            SingleCharToken.Type.ASTERISK.char -> tokens += SingleCharToken(SingleCharToken.Type.ASTERISK, line)

            SingleCharToken.Type.QUESTION_MARK.char -> tokens += SingleCharToken(SingleCharToken.Type.QUESTION_MARK, line)

            SingleCharToken.Type.COLON.char -> tokens += SingleCharToken(SingleCharToken.Type.COLON, line)

        /*
         * Tokens with one or more characters (so far just the equality and comparison operators)
         */
            SingleCharToken.Type.BANG.char -> comparison(SingleCharToken.Type.BANG, MultiCharToken.Type.BANG_EQUAL)

            SingleCharToken.Type.EQUAL.char -> comparison(SingleCharToken.Type.EQUAL, MultiCharToken.Type.EQUAL_EQUAL)

            SingleCharToken.Type.GREATER.char -> comparison(SingleCharToken.Type.GREATER, MultiCharToken.Type.GREATER_EQUAL)

            SingleCharToken.Type.LESS.char -> comparison(SingleCharToken.Type.LESS, MultiCharToken.Type.LESS_EQUAL)

            Characters.DOUBLE_QUOTE -> string()

            Characters.NEWLINE -> line++

            else -> when {
                c.isWhitespace() -> return
                isDigit(c) -> number()
                isAlpha(c) -> identifier()
                else -> KBagel.report(line) { "Unexpected token '$c'" }
            }
        }
    }

    /**
     * Returns the character in [source] at [curr] + [numCharsAhead]
     * @param numCharsAhead The number of characters ahead of the current character to peek. Defaults to 0.
     */
    private fun peek(numCharsAhead: Int = 0) =
            if (curr + numCharsAhead >= source.length) '\u0000' else source[curr + numCharsAhead]

    private fun advance() = if (isAtEnd) '\u0000' else source[curr++]

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getLexeme() = source.substring(start, curr)

    private fun string() {
        while (!isAtEnd && Characters.DOUBLE_QUOTE != peek()) {
            //Multi-line strings are permitted
            if (Characters.NEWLINE == peek()) {
                line++
            }

            advance()
        }

        if (isAtEnd) {
            KBagel.report(line) { "Unterminated string literal" }
            return
        }

        //We've met the ending double quote
        advance()

        val str = source.substring(start + 1, curr - 1)
        tokens += StringLiteralToken(getLexeme(), str, line)
        //tokens += LiteralToken(LiteralToken.Type.STRING, getLexeme(), str, line)
    }

    private fun number() {
        while (isDigit(peek())) {
            advance()
        }

        if (Characters.DOT == peek() && isDigit(peek(1))) {
            advance()

            while (isDigit(peek())) {
                advance()
            }
        }

        val n = source.substring(start, curr).toDouble()
        tokens += NumberLiteralToken(getLexeme(), n, line)
        //tokens += LiteralToken(LiteralToken.Type.NUMBER, getLexeme(), n, line)
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) {
            advance()
        }

        val ident = source.substring(start, curr)
        val keyword = KeywordToken.Type.keywords[ident]
        tokens += if (null != keyword) KeywordToken(keyword, line) else IdentifierToken(getLexeme(), line)
    }
}