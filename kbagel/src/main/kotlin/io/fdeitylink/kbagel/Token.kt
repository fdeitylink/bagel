package io.fdeitylink.kbagel

internal interface TokenType<T>
        where T : TokenType<T>, T : Enum<T>

internal sealed class Token<T>
        where T : TokenType<T>, T : Enum<T> {
    abstract val type: T
    abstract val lexeme: String
    abstract val line: Int
}

internal class SingleCharToken(
        override val type: SingleCharToken.Type,
        override val line: Int
) : Token<SingleCharToken.Type>() {
    override val lexeme = type.char.toString()

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

internal class MultiCharToken(
        override val type: MultiCharToken.Type,
        override val line: Int
) : Token<MultiCharToken.Type>() {
    override val lexeme = type.chars

    enum class Type(val chars: String) : TokenType<MultiCharToken.Type> {
        BANG_EQUAL("!="),
        EQUAL_EQUAL("=="),
        GREATER_EQUAL(">="),
        LESS_EQUAL("<=")
    }
}

internal class LiteralToken<out T : Any>
private constructor(
        override val type: LiteralToken.Type,
        override val lexeme: String,
        val value: T,
        override val line: Int
) : Token<LiteralToken.Type>() {
    companion object {
        operator fun invoke(lexeme: String, value: String, line: Int): LiteralToken<String> {
            require(isValidStringLiteral(lexeme)) { "'$lexeme' is not a valid string literal" }

            require(value == lexeme.substring(1, lexeme.lastIndex))
            { "value does not match lexeme (value: $value, lexeme: $lexeme)" }

            return LiteralToken(LiteralToken.Type.STRING, lexeme, value, line)
        }

        operator fun invoke(lexeme: String, value: Double, line: Int): LiteralToken<Double> {
            require(isValidNumberLiteral(lexeme)) { "'$lexeme' is not a valid number literal" }

            require(value == lexeme.toDouble()) { "value does not match lexeme (value: $value, lexeme: $lexeme)" }

            return LiteralToken(LiteralToken.Type.NUMBER, lexeme, value, line)
        }
    }

    enum class Type : TokenType<LiteralToken.Type> {
        STRING, NUMBER
    }
}

internal class IdentifierToken(
        override val lexeme: String,
        override val line: Int
) : Token<IdentifierToken.Type>() {
    override val type = IdentifierToken.Type.IDENTIFIER

    init {
        require(isValidIdentifier(lexeme)) { "'$lexeme' is not a valid identifier literal" }
    }

    enum class Type : TokenType<IdentifierToken.Type> {
        IDENTIFIER
    }
}

internal class KeywordToken(
        override val type: KeywordToken.Type,
        override val line: Int
) : Token<KeywordToken.Type>() {
    override val lexeme = type.chars

    enum class Type : TokenType<KeywordToken.Type> {
        AND, BREAK, CLASS, ELSE,
        FALSE, FOR, FUN, IF, NIL,
        OR, PRINT, RETURN, SUPER,
        THIS, TRUE, VAR, WHILE, XOR;

        val chars = name.toLowerCase()

        companion object {
            val keywords = enumValues<KeywordToken.Type>().associateBy(KeywordToken.Type::chars)
        }
    }
}

internal class EOFToken(
        override val line: Int
) : Token<EOFToken.Type>() {
    override val type = EOFToken.Type.EOF
    override val lexeme = ""

    enum class Type : TokenType<EOFToken.Type> {
        EOF
    }
}