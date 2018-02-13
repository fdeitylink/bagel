package io.fdeitylink.kbagel

internal class Parser(private val tokens: List<Token<*>>, private val reporter: ErrorReporter) {
    private var curr = 0

    private val isAtEnd get() = EOFToken.Type.EOF == peek().type

    private var loopDepth = 0

    val parsed: List<Stmt> by lazy(LazyThreadSafetyMode.NONE) {
        val stmts = mutableListOf<Stmt>()
        while (!isAtEnd) {
            declaration()?.let(stmts::add)
        }

        stmts
    }

    private fun declaration(): Stmt? {
        return try {
            when {
                match(KeywordToken.Type.VAR) -> varDeclaration()
                else -> statement()
            }
        }
        catch (except: ParseError) {
            synchronize()
            null
        }
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IdentifierToken.Type.IDENTIFIER) { "Expected variable name." } as IdentifierToken

        val initializer = if (match(SingleCharToken.Type.EQUAL)) expression() else null

        consume(SingleCharToken.Type.SEMICOLON) { "Expected ';' after variable declaration." }

        return Stmt.Var(name, initializer)
    }

    private fun statement() = when {
        match(KeywordToken.Type.PRINT) -> printStatement()
        match(SingleCharToken.Type.LEFT_BRACE) -> Stmt.Block(block())
        match(KeywordToken.Type.IF) -> ifStatement()
        match(KeywordToken.Type.WHILE) -> whileStatement()
        match(KeywordToken.Type.FOR) -> forStatement()
        match(KeywordToken.Type.BREAK) -> breakStatement()
        else -> expressionStatement()
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SingleCharToken.Type.SEMICOLON) { "Expected `;` after value." }
        return Stmt.Print(value)
    }

    private fun block(): List<Stmt> {
        val stmts = mutableListOf<Stmt>()

        while (!check(SingleCharToken.Type.RIGHT_BRACE) && !isAtEnd) {
            declaration()?.let(stmts::add)
        }

        consume(SingleCharToken.Type.RIGHT_BRACE) { "Expected '}' after block." }

        return stmts
    }

    private fun ifStatement(): Stmt {
        consume(SingleCharToken.Type.LEFT_PAREN) { "Expected '(' after 'if'." }
        val cond = expression()
        consume(SingleCharToken.Type.RIGHT_PAREN) { "Expected ')' after if condition." }

        val thenBranch = statement()
        val elseBranch = if (match(KeywordToken.Type.ELSE)) statement() else null

        return Stmt.If(cond, thenBranch, elseBranch)
    }

    private fun whileStatement(): Stmt {
        consume(SingleCharToken.Type.LEFT_PAREN) { "Expected '(' after 'while'." }

        val cond = expression()
        consume(SingleCharToken.Type.RIGHT_PAREN) { "Expected ')' after while condition." }

        try {
            loopDepth++

            val body = statement()

            return Stmt.While(cond, body)
        }
        finally {
            loopDepth--
        }
    }

    private fun forStatement(): Stmt {
        consume(SingleCharToken.Type.LEFT_PAREN) { "Expected '(' after 'for'." }

        val initializer = when {
            match(SingleCharToken.Type.SEMICOLON) -> null
            match(KeywordToken.Type.VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        val cond = if (!check(SingleCharToken.Type.SEMICOLON)) expression() else Expr.Literal(true)
        consume(SingleCharToken.Type.SEMICOLON) { "Expected ';' after loop condition." }

        val inc = if (!check(SingleCharToken.Type.RIGHT_PAREN)) expression() else null
        consume(SingleCharToken.Type.RIGHT_PAREN) { "Expected ')' after for clauses." }

        try {
            loopDepth++

            val body = statement()

            /*
             * Here, 'this' always refers to the current state of the loop body, whether it is
             * the initial form from above, includes an incrementing statement, a while loop,
             * or contains an initializer.
             */
            return body.run {
                (inc?.let { Stmt.Block(listOf(this, Stmt.Expression(inc))) } ?: this)
                        .run { Stmt.While(cond, this) }
                        .run { initializer?.let { Stmt.Block(listOf(initializer, this)) } ?: this }
            }
        }
        finally {
            loopDepth--
        }
    }

    private fun breakStatement(): Stmt {
        if (loopDepth == 0) {
            error(previous(), "Must be inside loop to use 'break'.")
        }

        consume(SingleCharToken.Type.SEMICOLON) { "Expected ';' after 'break'." }
        return Stmt.Break()
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SingleCharToken.Type.SEMICOLON) { "Expected ';' after expression." }
        return Stmt.Expression(expr)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun expression() = comma()

    private fun comma() = binaryLeftAssoc(::assignment, SingleCharToken.Type.COMMA)

    private fun assignment(): Expr {
        val expr = ternary()

        if (match(SingleCharToken.Type.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Var) {
                return Expr.Assign(expr.name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun ternary(): Expr {
        var expr = or()

        if (match(SingleCharToken.Type.QUESTION_MARK)) {
            val thenBranch = expression()
            consume(SingleCharToken.Type.COLON) { "Expected ':' after \"then branch\" of ternary expression." }
            val elseBranch = ternary()
            expr = Expr.Ternary(expr, thenBranch, elseBranch)
        }

        return expr
    }

    @Suppress("UNCHECKED_CAST")
    private fun or() =
            binaryLeftAssoc(::and, KeywordToken.Type.OR, binaryExprCtor = Expr::Logical as (Expr, Token<*>, Expr) -> Expr)

    @Suppress("UNCHECKED_CAST")
    private fun and() =
            binaryLeftAssoc(::xor, KeywordToken.Type.AND, binaryExprCtor = Expr::Logical as (Expr, Token<*>, Expr) -> Expr)

    @Suppress("UNCHECKED_CAST")
    private fun xor() =
            binaryLeftAssoc(::equality, KeywordToken.Type.XOR, binaryExprCtor = Expr::Logical as (Expr, Token<*>, Expr) -> Expr)

    private fun equality() =
            binaryLeftAssoc(::comparison, MultiCharToken.Type.EQUAL_EQUAL, MultiCharToken.Type.BANG_EQUAL)

    private fun comparison() =
            binaryLeftAssoc(::addition,
                            SingleCharToken.Type.GREATER, MultiCharToken.Type.GREATER_EQUAL,
                            SingleCharToken.Type.LESS, MultiCharToken.Type.LESS_EQUAL)

    private fun addition() = binaryLeftAssoc(::multiplication, SingleCharToken.Type.PLUS, SingleCharToken.Type.MINUS)

    private fun multiplication() =
            binaryLeftAssoc(::unary, SingleCharToken.Type.ASTERISK, SingleCharToken.Type.FORWARD_SLASH)

    private fun unary(): Expr {
        if (match(SingleCharToken.Type.BANG, SingleCharToken.Type.MINUS)) {
            val op = previous()
            val operand = unary()

            @Suppress("UNCHECKED_CAST")
            return Expr.Unary(op as Token<SingleCharToken.Type>, operand)
        }

        return primary()
    }

    @Suppress("UNCHECKED_CAST")
    private fun primary() = when {
        match(KeywordToken.Type.TRUE) -> Expr.Literal(true)
        match(KeywordToken.Type.FALSE) -> Expr.Literal(false)
        match(KeywordToken.Type.NIL) -> Expr.Literal()

        match(LiteralToken.Type.NUMBER) -> Expr.Literal((previous() as LiteralToken<Double>).value)
        match(LiteralToken.Type.STRING) -> Expr.Literal((previous() as LiteralToken<String>).value)

        match(IdentifierToken.Type.IDENTIFIER) -> Expr.Var(previous() as IdentifierToken)

        match(SingleCharToken.Type.LEFT_PAREN) -> {
            val expr = expression()
            consume(SingleCharToken.Type.RIGHT_PAREN) { "Expected ')' after expression." }
            Expr.Grouping(expr)
        }

        else -> throw error(peek(), "Expected expression.")
    }

    private fun binaryLeftAssoc(
            nextHighest: () -> Expr,
            vararg tokens: TokenType<*>,
            binaryExprCtor: (Expr, Token<*>, Expr) -> Expr = Expr::Binary
    ): Expr {
        var expr = nextHighest()

        while (match(*tokens)) {
            val op = previous()
            val r = nextHighest()
            expr = binaryExprCtor(expr, op, r)
        }

        return expr
    }

    private fun consume(type: TokenType<*>, lazyMessage: () -> Any) =
            if (check(type)) advance() else throw error(peek(), lazyMessage().toString())

    private fun error(token: Token<*>, message: String): ParseError {
        reporter.report(token, message)
        return ParseError(token, message)
    }

    private fun match(vararg tokens: TokenType<*>): Boolean {
        val match = tokens.any(::check)
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

    private class ParseError(val token: Token<*>, override val message: String) : Exception()
}