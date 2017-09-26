package io.fdeitylink.kbagel

internal interface Operation<O>
where O : Operation<O>, O : Enum<O> {
    //TODO: Store Token instead of TokenType? (more information available without casting)
    val tokenType: TokenType<*>
}

internal enum class UnaryOperation(
    override val tokenType: SingleCharToken.Type
) : Operation<UnaryOperation> {
    NEGATE(SingleCharToken.Type.MINUS),
    NOT(SingleCharToken.Type.BANG);

    companion object {
        val operators = enumValues<UnaryOperation>().associateBy(UnaryOperation::tokenType)
    }
}

internal enum class BinaryOperation(
    override val tokenType: TokenType<*>
) : Operation<BinaryOperation> {
    MINUS(SingleCharToken.Type.MINUS), PLUS(SingleCharToken.Type.PLUS),
    DIVIDE(SingleCharToken.Type.FORWARD_SLASH), MULTIPLY(SingleCharToken.Type.ASTERISK),

    ASSIGN(SingleCharToken.Type.EQUAL),

    COMMA(SingleCharToken.Type.COMMA),

    CHECK_EQUAL(MultiCharToken.Type.EQUAL_EQUAL), CHECK_NOT_EQUAL(MultiCharToken.Type.BANG_EQUAL),
    CHECK_GREATER(SingleCharToken.Type.GREATER), CHECK_GREATER_EQUAL(MultiCharToken.Type.GREATER_EQUAL),
    CHECK_LESS(SingleCharToken.Type.LESS), CHECK_LESS_EQUAL(MultiCharToken.Type.LESS_EQUAL),

    AND(KeywordToken.Type.AND), OR(KeywordToken.Type.OR);

    companion object {
        val operators = enumValues<BinaryOperation>().associateBy(BinaryOperation::tokenType)
    }
}

//TODO: Put in a TernaryOperation enum class? (will only have one enum)