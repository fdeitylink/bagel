/*
 * Private constructors for data classes offer no real protection because of the
 * copy method, but I'm using them here to make it harder to accidentally use the
 * full primary constructors as opposed to the secondary constructors, which pass
 * in specific values to the primary constructors that are either relied upon or
 * are the only sensible values. I'd replace the primary constructors with the
 * secondary ones, but then not all of the necessary fields would be used in the
 * generated methods.
 */
@file:Suppress("DataClassPrivateConstructor")

package io.fdeitylink.kbagel

internal interface TokenType<T>
        where T : TokenType<T>, T : Enum<T>

internal sealed class Token<T>
        where T : TokenType<T>, T : Enum<T> {
    abstract val type: T
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
        QUESTION_MARK('?'), COLON(':'),

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

internal data class LiteralToken<out T : Any>
private constructor(
        override val type: LiteralToken.Type,
        override val lexeme: String,
        val value: T,
        override val line: Int
) : Token<LiteralToken.Type>() {
    companion object {
        operator fun invoke(lexeme: String, value: String, line: Int): LiteralToken<String> {
            require(isValidStringLiteral(lexeme)) { "$lexeme is not a valid string literal" }

            require(value == lexeme.substring(1, lexeme.lastIndex))
            { "value does not match lexeme (value: $value, lexeme: $lexeme)" }

            return LiteralToken(LiteralToken.Type.STRING, lexeme, value, line)
        }

        operator fun invoke(lexeme: String, value: Double, line: Int): LiteralToken<Double> {
            require(isValidNumberLiteral(lexeme)) { "$lexeme is not a valid number literal" }

            require(value == lexeme.toDouble()) { "value does not match lexeme (value: $value, lexeme: $lexeme)" }

            return LiteralToken(LiteralToken.Type.NUMBER, lexeme, value, line)
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
        require(isValidIdentifier(lexeme)) { "$lexeme is not a valid identifier literal" }
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