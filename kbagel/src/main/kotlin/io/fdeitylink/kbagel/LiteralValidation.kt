package io.fdeitylink.kbagel

@Suppress("NOTHING_TO_INLINE")
internal inline fun isDigit(c: Char) = c in '0'..'9'

@Suppress("NOTHING_TO_INLINE")
internal inline fun isAlpha(c: Char) = c in ('a'..'z') + ('A'..'Z') + '_'

@Suppress("NOTHING_TO_INLINE")
internal inline fun isAlphaNumeric(c: Char) = isDigit(c) || isAlpha(c)

@Suppress("NOTHING_TO_INLINE")
internal inline fun isValidStringLiteral(lexeme: String) = Regex("^\"[^\"]*\"$").matches(lexeme)

@Suppress("NOTHING_TO_INLINE")
internal inline fun isValidNumberLiteral(lexeme: String) = Regex("^\\d+(?:\\.\\d+)?\$").matches(lexeme)

@Suppress("NOTHING_TO_INLINE")
internal inline fun isValidIdentifierLiteral(lexeme: String) =
        Regex("^[a-zA-Z_][a-zA-Z_0-9]*\$").matches(lexeme) && null == KeywordToken.Type.keywords[lexeme]