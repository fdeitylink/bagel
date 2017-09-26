package io.fdeitylink.kbagel

internal sealed class Expr {
    fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)

    abstract class Visitor<R> {
        @Suppress("UNCHECKED_CAST")
        fun visit(e: Expr) =
                Visitor::class.java.declaredMethods
                        .first { "visit" == it.name && it.parameterTypes.first() == e::class.java }
                        .invoke(this, e) as R

        protected abstract fun visit(u: Unary): R

        protected abstract fun visit(b: Binary): R

        protected abstract fun visit(t: Ternary): R

        protected abstract fun visit(l: Literal): R

        protected abstract fun visit(g: Grouping): R
    }

    data class Unary(val op: UnaryOperation, val operand: Expr) : Expr()

    data class Binary(val lOperand: Expr, val op: BinaryOperation, val rOperand: Expr) : Expr()

    data class Ternary(val cond: Expr, val thenBranch: Expr, val elseBranch: Expr) : Expr()

    //TODO: Validate by forcing value to be null, String, or Double?
    data class Literal(val value: Any?) : Expr()

    data class Grouping(val expr: Expr) : Expr()
}