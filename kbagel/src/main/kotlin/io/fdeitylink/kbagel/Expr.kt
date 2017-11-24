package io.fdeitylink.kbagel

internal sealed class Expr {
    fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)

    abstract class Visitor<out R> {
        @Suppress("UNCHECKED_CAST")
        fun visit(e: Expr) =
                Visitor::class.java.declaredMethods
                        .first { it.parameterTypes.first() == e::class.java }
                        .invoke(this, e) as R

        protected abstract fun visit(u: Unary): R

        protected abstract fun visit(b: Binary): R

        protected abstract fun visit(t: Ternary): R

        protected abstract fun visit(l: Literal<*>): R

        protected abstract fun visit(g: Grouping): R
    }

    data class Unary(
            val token: Token<SingleCharToken.Type>,
            val operand: Expr
    ) : Expr() {
        val op = Unary.Op.operators[token.type]!!

        init {
            require(token.type in Unary.Op.operators) { "${token.type} has no corresponding unary operator" }
        }

        enum class Op(override val tokenType: SingleCharToken.Type) : Operation<Unary.Op> {
            MINUS(SingleCharToken.Type.MINUS),
            NOT(SingleCharToken.Type.BANG);

            companion object {
                val operators = Operation.operators<Unary.Op>()
            }
        }
    }

    data class Binary(
            val lOperand: Expr,
            val token: Token<*>,
            val rOperand: Expr
    ) : Expr() {
        val op = Binary.Op.operators[token.type]!!

        init {
            require(token.type in Binary.Op.operators) { "${token.type} has no corresponding binary operator" }
        }

        enum class Op(override val tokenType: TokenType<*>) : Operation<Binary.Op> {
            SUBTRACT(SingleCharToken.Type.MINUS), ADD(SingleCharToken.Type.PLUS),
            DIVIDE(SingleCharToken.Type.FORWARD_SLASH), MULTIPLY(SingleCharToken.Type.ASTERISK),

            COMMA(SingleCharToken.Type.COMMA),

            CHECK_EQUAL(MultiCharToken.Type.EQUAL_EQUAL), CHECK_NOT_EQUAL(MultiCharToken.Type.BANG_EQUAL),
            CHECK_GREATER(SingleCharToken.Type.GREATER), CHECK_GREATER_EQUAL(MultiCharToken.Type.GREATER_EQUAL),
            CHECK_LESS(SingleCharToken.Type.LESS), CHECK_LESS_EQUAL(MultiCharToken.Type.LESS_EQUAL);

            companion object {
                val operators = Operation.operators<Binary.Op>()
            }
        }
    }

    data class Ternary(val cond: Expr, val thenBranch: Expr, val elseBranch: Expr) : Expr()

    //See comment at top of Token.kt for the reasoning behind using private data class constructors
    @Suppress("DataClassPrivateConstructor")
    data class Literal<out T> private constructor(val value: T) : Expr() {
        companion object {
            operator fun invoke() = Literal(null)

            operator fun invoke(value: Boolean) = Literal(value)

            operator fun invoke(value: String) = Literal(value)

            operator fun invoke(value: Double) = Literal(value)
        }
    }

    data class Grouping(val expr: Expr) : Expr()
}