package io.fdeitylink.kbagel

internal interface TokenType <E> where E : TokenType<E>, E : Enum<E>

internal sealed class Token <E> where E : TokenType<E>, E : Enum<E> {
    abstract val type: E
    abstract val lexeme: String
    abstract val line: Int
}

internal data class SingleCharToken
private constructor(
    override val type: SingleCharToken.Type,
    override val lexeme: String,
    override val line: Int
) : Token<SingleCharToken.Type>() {
    constructor(type: SingleCharToken.Type, line: Int) : this(type, type.char.toString(), line)

    enum class Type(val char: Char) : TokenType<SingleCharToken.Type> {
        LEFT_PAREN('('), RIGHT_PAREN(')'),
        LEFT_BRACE('{'), RIGHT_BRACE('}'),
        COMMA(','), DOT('.'),
        MINUS('-'), PLUS('+'),
        SEMICOLON(';'),
        FORWARD_SLASH('/'), ASTERISK('*'),

        BANG('!'),
        EQUAL('='),
        GREATER('>'),
        LESS('<')
    }
}

internal data class MultiCharToken
private constructor(
    override val type: MultiCharToken.Type,
    override val lexeme: String,
    override val line: Int
) : Token<MultiCharToken.Type>() {
    constructor(type: MultiCharToken.Type, line: Int) : this(type, type.chars, line)

    enum class Type(val chars: String) : TokenType<MultiCharToken.Type> {
        BANG_EQUAL("!="),
        EQUAL_EQUAL("=="),
        GREATER_EQUAL(">="),
        LESS_EQUAL("<=");
    }
}

//TODO: Can a generic param be forced to be either String or Double and be used for literal?
internal data class LiteralToken(
    override val type: LiteralToken.Type,
    override val lexeme: String,
    val literal: Any,
    override val line: Int
) : Token<LiteralToken.Type>() {
    init {
        when (type) {
            LiteralToken.Type.STRING -> {
                require(isValidStringLiteral(lexeme)) { "$lexeme is not a valid string literal" }
                require(literal == lexeme.trim('\"'))
                { "The given literal does not match the lexeme (literal: $literal, lexeme: $lexeme)" }
                require(literal is String) { "literal is not String (type: ${literal::class})" }
            }

            LiteralToken.Type.NUMBER -> {
                require(isValidNumberLiteral(lexeme)) { "$lexeme is not a valid number literal" }
                require(literal == lexeme.toDouble())
                { "The given literal does not match the lexeme (literal: $literal, lexeme: $lexeme)" }
                require(literal is Double) { "literal is not a Double (type: ${literal::class})" }
            }
        }
    }

    enum class Type : TokenType<LiteralToken.Type> {
        STRING, NUMBER
    }
}

internal data class IdentifierToken
private constructor(
    override val type: IdentifierToken.Type,
    override val lexeme: String,
    override val line: Int
) : Token<IdentifierToken.Type>() {
    constructor(lexeme: String, line: Int) : this(IdentifierToken.Type.IDENTIFIER, lexeme, line)

    init {
        require(isValidIdentifierLiteral(lexeme)) { "$lexeme is not a valid identifier literal" }
    }

    enum class Type : TokenType<IdentifierToken.Type> {
        IDENTIFIER
    }
}

internal data class KeywordToken
private constructor(
    override val type: KeywordToken.Type,
    override val lexeme: String,
    override val line: Int
) : Token<KeywordToken.Type>() {
    constructor(type: KeywordToken.Type, line: Int) : this(type, type.chars, line)

    enum class Type : TokenType<KeywordToken.Type> {
        AND, CLASS, ELSE, FALSE,
        FUN, FOR, IF, NIL, OR,
        PRINT, RETURN, SUPER,
        THIS, TRUE, VAR, WHILE;

        val chars = name.toLowerCase()

        companion object {
            val keywords = enumValues<KeywordToken.Type>().associateBy(KeywordToken.Type::chars)
        }
    }
}

internal data class EOFToken
private constructor(
    override val type: EOFToken.Type,
    override val lexeme: String,
    override val line: Int
) : Token<EOFToken.Type>() {
    constructor(line: Int) : this(EOFToken.Type.EOF, "", line)

    enum class Type : TokenType<EOFToken.Type> {
        EOF
    }
}