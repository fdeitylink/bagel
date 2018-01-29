package io.fdeitylink.kbagel

internal sealed class Expr {
    fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)

    interface Visitor<out R> {
        @Suppress("UNCHECKED_CAST")
        fun visit(e: Expr) =
                Visitor::class.java.declaredMethods
                        .first { it.parameterTypes.first() == e::class.java }
                        .invoke(this, e) as R

        //TODO: Make these methods protected again once it's supported in Kotlin (IIRC Java 9 supports it)
        fun visit(u: Unary): R

        fun visit(b: Binary): R

        fun visit(t: Ternary): R

        fun visit(l: Literal<*>): R

        fun visit(g: Grouping): R

        fun visit(v: Var): R

        fun visit(a: Assign): R
    }

    class Unary(
            val token: Token<SingleCharToken.Type>,
            val operand: Expr
    ) : Expr() {
        init {
            require(token.type in Unary.Op.operators) { "${token.type} has no corresponding unary operator" }
        }

        val op = Unary.Op.operators[token.type]!!

        enum class Op(override val tokenType: SingleCharToken.Type) : Operation<Unary.Op> {
            MINUS(SingleCharToken.Type.MINUS),
            NOT(SingleCharToken.Type.BANG);

            companion object {
                val operators = Operation.operators<Unary.Op>()
            }
        }
    }

    class Binary(
            val lOperand: Expr,
            val token: Token<*>,
            val rOperand: Expr
    ) : Expr() {
        init {
            require(token.type in Binary.Op.operators) { "${token.type} has no corresponding binary operator" }
        }

        val op = Binary.Op.operators[token.type]!!

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

    class Ternary(val cond: Expr, val thenBranch: Expr, val elseBranch: Expr) : Expr()

    class Literal<out T> private constructor(val value: T) : Expr() {
        companion object {
            operator fun invoke() = Literal(null)

            operator fun invoke(value: Boolean) = Literal(value)

            operator fun invoke(value: String) = Literal(value)

            operator fun invoke(value: Double) = Literal(value)
        }
    }

    class Grouping(val expr: Expr) : Expr()

    class Var(val name: IdentifierToken) : Expr()

    class Assign(val name: IdentifierToken, val value: Expr) : Expr()
}