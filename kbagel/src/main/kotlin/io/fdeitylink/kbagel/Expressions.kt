package io.fdeitylink.kbagel

internal sealed class Expr {
    fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)

    abstract class Visitor<R> {
        @Suppress("UNCHECKED_CAST")
        fun visit(e: Expr) =
                Visitor::class.java.declaredMethods
                        .first { it.parameterTypes.first() == e::class.java }
                        .invoke(this, e) as R

        abstract fun visit(u: Unary): R

        abstract fun visit(b: Binary): R

        abstract fun visit(l: Literal): R

        abstract fun visit(g: Grouping): R
    }

    data class Unary(val op: Token<*>, val operand: Expr) : Expr()

    data class Binary(val lOperand: Expr, val op: Token<*>, val rOperand: Expr) : Expr()

    data class Literal(val value: Any) : Expr()

    data class Grouping(val expr: Expr) : Expr()
}