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
                checkIsNumber(u.token, operand)
                -(operand as Double)
            }
            Expr.Unary.Op.NOT -> !operand.isTruthy
        }
    }

    override fun visit(b: Expr.Binary): Any? {
        infix fun Any?.equals(o: Any?) = if (this == null && o == null) true else this?.equals(o) ?: false

        val l = eval(b.lOperand)
        val r = eval(b.rOperand)

        return when (b.op) {
            Expr.Binary.Op.ADD -> {
                //TODO: Reduce verbosity of this section
                if (l is Double && r is Double) {
                    l + r
                }
                else if (l is String && r is String) {
                    l + r
                }
                //TODO: Trim ".0" from double when appending
                else if (l is String && r is Double) {
                    l + r
                }
                else if (l is Double && r is String) {
                    l.toString() + r
                }
                else {
                    throw LoxRuntimeError(b.token, "Operands must be two numbers or two strings (l: $l, r: $r)")
                }
            }
            Expr.Binary.Op.SUBTRACT -> {
                checkIsNumber(l, b.token, r)
                l as Double - r as Double
            }

            Expr.Binary.Op.MULTIPLY -> {
                checkIsNumber(l, b.token, r)
                l as Double * r as Double
            }
            Expr.Binary.Op.DIVIDE -> {
                checkIsNumber(l, b.token, r)
                l as Double / r as Double
            }

            Expr.Binary.Op.CHECK_EQUAL -> l equals r
            Expr.Binary.Op.CHECK_NOT_EQUAL -> !(l equals r)

            Expr.Binary.Op.CHECK_GREATER -> {
                checkIsNumber(l, b.token, r)
                l as Double > r as Double
            }
            Expr.Binary.Op.CHECK_GREATER_EQUAL -> {
                checkIsNumber(l, b.token, r)
                l as Double >= r as Double
            }

        //Without the parentheses, a compiler error occurs as the '<' denotes a type parameter, but Double expects none
            Expr.Binary.Op.CHECK_LESS -> {
                checkIsNumber(l, b.token, r)
                (l as Double) < r as Double
            }
            Expr.Binary.Op.CHECK_LESS_EQUAL -> {
                checkIsNumber(l, b.token, r)
                l as Double <= r as Double
            }

            Expr.Binary.Op.COMMA -> r
        }
    }

    override fun visit(t: Expr.Ternary): Any? = if (eval(t.cond).isTruthy) eval(t.thenBranch) else eval(t.elseBranch)

    override fun visit(l: Expr.Literal<*>) = l.value

    override fun visit(g: Expr.Grouping) = eval(g.expr)

    private fun eval(expr: Expr) = expr.accept(this)

    private fun checkIsNumber(token: Token<*>, operand: Any?) {
        if (operand !is Double) {
            throw LoxRuntimeError(token, "Operand is not a number (operand: $operand)")
        }
    }

    private fun checkIsNumber(l: Any?, token: Token<*>, r: Any?) {
        if (l !is Double) {
            throw LoxRuntimeError(token, "Left operand is not a number (l: $l)")
        }
        if (r !is Double) {
            throw LoxRuntimeError(token, "Right operand is not a number (r: $r)")
        }
    }

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