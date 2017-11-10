package io.fdeitylink.kbagel

internal class Parser(private val tokens: List<Token<*>>, private val reporter: ErrorReporter) {
    private var curr = 0

    private val isAtEnd get() = EOFToken.Type.EOF == peek().type

    val parsed by lazy(LazyThreadSafetyMode.NONE) {
        try {
            expression()
        }
        catch (except: ParseException) {
            null
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun expression() = comma()

    private fun comma() = binaryLeftAssoc(Parser::ternary, SingleCharToken.Type.COMMA)

    private fun ternary(): Expr {
        var expr = equality()

        if (match(SingleCharToken.Type.QUESTION_MARK)) {
            val thenBranch = ternary()
            consume(SingleCharToken.Type.COLON) { "Expected ':' after \"then branch\" of ternary statement" }
            val elseBranch = ternary()
            expr = Expr.Ternary(expr, thenBranch, elseBranch)
        }

        return expr
    }

    private fun equality() =
            binaryLeftAssoc(Parser::comparison, MultiCharToken.Type.EQUAL_EQUAL, MultiCharToken.Type.BANG_EQUAL)

    private fun comparison() =
            binaryLeftAssoc(Parser::addition,
                            SingleCharToken.Type.GREATER, MultiCharToken.Type.GREATER_EQUAL,
                            SingleCharToken.Type.LESS, MultiCharToken.Type.LESS_EQUAL)

    private fun addition() =
            binaryLeftAssoc(Parser::multiplication, SingleCharToken.Type.PLUS, SingleCharToken.Type.MINUS)

    private fun multiplication() =
            binaryLeftAssoc(Parser::unary, SingleCharToken.Type.ASTERISK, SingleCharToken.Type.FORWARD_SLASH)

    private fun unary(): Expr {
        if (match(SingleCharToken.Type.BANG, SingleCharToken.Type.MINUS)) {
            val op = previous()
            val operand = unary()

            return Expr.Unary(UnaryOperation.operators[op.type]!!, operand)
        }

        return primary()
    }

    private fun primary() = when {
        match(KeywordToken.Type.TRUE) -> Expr.Literal(true)
        match(KeywordToken.Type.FALSE) -> Expr.Literal(false)
        match(KeywordToken.Type.NIL) -> Expr.Literal(null)

        match(NumberLiteralToken.Type.NUMBER, StringLiteralToken.Type.STRING) ->
            Expr.Literal((previous() as LiteralToken<*, *>).value)

        match(SingleCharToken.Type.LEFT_PAREN) -> {
            val expr = expression()
            consume(SingleCharToken.Type.RIGHT_PAREN) { "Expected ')' after expression." }
            Expr.Grouping(expr)
        }

        else -> throw error(peek()) { "Expected expression." }
    }

    private fun binaryLeftAssoc(nextHighest: Parser.() -> Expr, vararg tokens: TokenType<*>): Expr {
        var expr = nextHighest()

        while (match(*tokens)) {
            val op = previous()
            val rOperand = nextHighest()
            expr = Expr.Binary(expr, BinaryOperation.operators[op.type]!!, rOperand)
        }

        return expr
    }

    private fun consume(type: TokenType<*>, lazyMessage: () -> Any) =
            if (check(type)) advance() else throw error(peek(), lazyMessage)

    private fun error(token: Token<*>, lazyMessage: () -> Any): ParseException {
        reporter.report(token, lazyMessage)
        return ParseException(token, lazyMessage)
    }

    private fun match(vararg tokens: TokenType<*>): Boolean {
        val match = tokens.any { check(it) }
        if (match) {
            advance()
        }

        return match
    }

    private fun check(type: TokenType<*>) = if (isAtEnd) false else peek().type == type

    private fun advance(): Token<*> {
        if (!isAtEnd) {
            curr++
        }

        return previous()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun peek() = tokens[curr]

    @Suppress("NOTHING_TO_INLINE")
    private inline fun previous() = tokens[curr - 1]

    private fun synchronize() {
        advance()

        while (!isAtEnd) {
            if (SingleCharToken.Type.SEMICOLON == previous().type) {
                return
            }

            when (peek().type) {
                KeywordToken.Type.CLASS, KeywordToken.Type.FUN,
                KeywordToken.Type.VAR, KeywordToken.Type.FOR,
                KeywordToken.Type.IF, KeywordToken.Type.WHILE,
                KeywordToken.Type.PRINT, KeywordToken.Type.RETURN -> return
            }

            advance()
        }
    }

    private class ParseException(val token: Token<*>, lazyMessage: () -> Any) : Exception(lazyMessage().toString())
}