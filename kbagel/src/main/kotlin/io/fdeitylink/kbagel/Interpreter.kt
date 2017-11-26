package io.fdeitylink.kbagel

internal class Interpreter(private val reporter: ErrorReporter) : Expr.Visitor<Any?>() {
    fun interpret(e: Expr) =
            try {
                stringify(eval(e))
            }
            catch (err: LoxRuntimeError) {
                reporter.report(err)
                ""
            }

    override fun visit(u: Expr.Unary): Any? {
        val operand = eval(u.operand)

        return when (u.op) {
            Expr.Unary.Op.MINUS -> {
                if (operand !is Double) {
                    throw LoxRuntimeError(u.token, "Operand is not a number (operand: $operand)")
                }
                -operand
            }
            Expr.Unary.Op.NOT -> !operand.isTruthy
        }
    }

    override fun visit(b: Expr.Binary): Any? {
        infix fun Any?.equals(o: Any?) = if (this == null && o == null) true else this?.equals(o) ?: false

        val l = eval(b.lOperand)
        val r = eval(b.rOperand)

        fun checkOperandsAreNumbers() {
            if (l !is Double) {
                throw LoxRuntimeError(b.token, "Left operand is not a number (l: $l, r: $r)")
            }
            if (r !is Double) {
                throw LoxRuntimeError(b.token, "Right operand is not a number (l: $l, r: $r)")
            }
        }

        mapOf(
                Expr.Binary.Op.CHECK_GREATER to { x: Int -> x > 0 },
                Expr.Binary.Op.CHECK_GREATER_EQUAL to { x: Int -> x >= 0 },
                Expr.Binary.Op.CHECK_LESS to { x: Int -> x < 0 },
                Expr.Binary.Op.CHECK_LESS_EQUAL to { x: Int -> x <= 0 }
        )
                .entries
                .firstOrNull { (op) -> op == b.op }
                ?.let { (_, pred) ->
                    return@visit when {
                        l is String && r is String -> pred(l.compareTo(r))
                        l is Double && r is Double -> pred(l.compareTo(r))
                        else -> throw LoxRuntimeError(b.token, "Operands must be two numbers or two strings (l: $l, r: $r)")
                    }
                }

        return when (b.op) {
            Expr.Binary.Op.ADD -> {
                //TODO: Reduce verbosity of this section
                if (l is Double && r is Double) {
                    l + r
                }
                else if (l is String && r is String) {
                    l + r
                }
                else if (l is String && r is Double) {
                    l + r.toString().removeSuffix(".0")
                }
                else if (l is Double && r is String) {
                    l.toString().removeSuffix(".0") + r
                }
                else {
                    throw LoxRuntimeError(b.token, "Operands must be two numbers or two strings (l: $l, r: $r)")
                }
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
                l as Double / r as Double
            }

            Expr.Binary.Op.CHECK_EQUAL -> l equals r
            Expr.Binary.Op.CHECK_NOT_EQUAL -> !(l equals r)

            Expr.Binary.Op.COMMA -> r

            else -> throw Error()
        }
    }

    override fun visit(t: Expr.Ternary): Any? = if (eval(t.cond).isTruthy) eval(t.thenBranch) else eval(t.elseBranch)

    override fun visit(l: Expr.Literal<*>) = l.value

    override fun visit(g: Expr.Grouping) = eval(g.expr)

    private fun eval(expr: Expr) = expr.accept(this)

    private val Any?.isTruthy
        get() = when (this) {
            null -> false
            is Boolean -> this
            else -> true
        }

    private fun stringify(o: Any?) = when (o) {
        null -> "nil"
        is Double -> o.toString().removeSuffix(".0")
        else -> o.toString()
    }
}