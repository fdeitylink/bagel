package io.fdeitylink.kbagel

internal interface OperationType<E> where E : OperationType<E>, E : Enum<E>

internal sealed class Operation<E> where E : OperationType<E>, E : Enum<E> {
    abstract val op : E
}

internal data class UnaryOperation(
    override val op: UnaryOperation.Type
) : Operation<UnaryOperation.Type>() {
    enum class Type {
        NEGATE,  NOT
    }
}

internal data class BinaryOperation(
    override val op: BinaryOperation.Type
) : Operation<BinaryOperation.Type>() {
    enum class Type {
        MINUS, PLUS,
        DIVIDE, MULTIPLY,

        ASSIGN,

        CHECK_EQUAL, CHECK_NOT_EQUAL,
        CHECK_GREATER, CHECK_GREATER_EQUAL,
        CHECK_LESS, CHECK_LESS_EQUAL,

        AND, OR
    }
}