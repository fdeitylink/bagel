package io.fdeitylink.kbagel

internal class Interpreter(private val reporter: ErrorReporter) : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private var env = Environment()

    fun interpret(stmts: List<Stmt>) =
            try {
                stmts.forEach(::exec)
            }
            catch (err: BagelRuntimeError) {
                reporter.report(err)
            }

    override fun visit(u: Expr.Unary): Any? {
        val operand = eval(u.operand)

        return when (u.op) {
            Expr.Unary.Op.MINUS -> {
                if (operand !is Double) {
                    throw BagelRuntimeError(u.token, "Operand is not a number (operand: $operand)")
                }
                -operand
            }
            Expr.Unary.Op.NOT -> !operand.isTruthy
        }
    }

    override fun visit(b: Expr.Binary): Any? {
        infix fun Any?.eq(o: Any?) = if (this == null && o == null) true else this?.equals(o) ?: false

        val l = eval(b.lOperand)
        val r = eval(b.rOperand)

        fun checkOperandsAreNumbers() {
            if (l !is Double) {
                throw BagelRuntimeError(b.token, "Left operand is not a number (l: $l, r: $r)")
            }
            if (r !is Double) {
                throw BagelRuntimeError(b.token, "Right operand is not a number (l: $l, r: $r)")
            }
        }

        mapOf(
                Expr.Binary.Op.CHECK_GREATER to { diff: Int -> diff > 0 },
                Expr.Binary.Op.CHECK_GREATER_EQUAL to { diff: Int -> diff >= 0 },
                Expr.Binary.Op.CHECK_LESS to { diff: Int -> diff < 0 },
                Expr.Binary.Op.CHECK_LESS_EQUAL to { diff: Int -> diff <= 0 }
        )
                .entries
                .firstOrNull { (op) -> op == b.op }
                ?.let { (_, predicate) ->
                    return@visit when {
                        l is String && r is String -> predicate(l.compareTo(r))
                        l is Double && r is Double -> predicate(l.compareTo(r))
                        else -> throw BagelRuntimeError(b.token, "Operands must be two numbers or two strings (l: $l, r: $r)")
                    }
                }

        return when (b.op) {
            Expr.Binary.Op.ADD -> when {
                l is Double && r is Double -> l + r
                l is String && r is String -> l + r
                l is String && r is Double -> l + stringify(r)
                l is Double && r is String -> stringify(l) + r
                else -> throw BagelRuntimeError(b.token, "Operands must be numbers or strings (l: $l, r: $r)")
            }
            Expr.Binary.Op.SUBTRACT -> {
                checkOperandsAreNumbers()
                l as Double - r as Double
            }

            Expr.Binary.Op.MULTIPLY -> {
                checkOperandsAreNumbers()
                l as Double * r as Double
            }
            Expr.Binary.Op.DIVIDE -> {
                checkOperandsAreNumbers()
                if (r == 0.toDouble()) {
                    throw BagelRuntimeError(b.token, "Division by zero (right-hand operand is 0)")
                }
                l as Double / r as Double
            }

            Expr.Binary.Op.CHECK_EQUAL -> l eq r
            Expr.Binary.Op.CHECK_NOT_EQUAL -> !(l eq r)

            Expr.Binary.Op.COMMA -> r

            else -> throw Error()
        }
    }

    override fun visit(t: Expr.Ternary): Any? = if (eval(t.cond).isTruthy) eval(t.thenBranch) else eval(t.elseBranch)

    override fun visit(l: Expr.Literal<*>) = l.value

    override fun visit(g: Expr.Grouping) = eval(g.expr)

    override fun visit(v: Expr.Var) = env[v.name]

    override fun visit(a: Expr.Assign): Any? {
        val value = eval(a.value)
        env.assign(a.name, value)
        return value
    }

    override fun visit(e: Stmt.Expression) {
        eval(e.expr)
    }

    override fun visit(p: Stmt.Print) = println(stringify(eval(p.expr)))

    override fun visit(v: Stmt.Var) {
        env[v.name] = v.initializer?.let(::eval)
    }

    override fun visit(b: Stmt.Block) {
        val stmts = b.stmts
        val env = Environment(this.env)

        val prev = this.env

        try {
            this.env = env
            stmts.forEach(::exec)
        }
        finally {
            this.env = prev
        }
    }

    private fun exec(stmt: Stmt) = stmt.accept(this)

    private fun eval(expr: Expr) = expr.accept(this)

    private fun stringify(o: Any?) = when (o) {
        null -> "nil"
        is Double -> o.toString().removeSuffix(".0")
        else -> o.toString()
    }

    private val Any?.isTruthy
        get() = when (this) {
            null -> false
            is Boolean -> this
            else -> true
        }
}