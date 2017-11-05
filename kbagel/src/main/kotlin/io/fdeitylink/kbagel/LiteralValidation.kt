package io.fdeitylink.kbagel

internal fun isDigit(c: Char) = c in '0'..'9'

internal fun isAlpha(c: Char) = c in ('a'..'z') + ('A'..'Z') + '_'

internal fun isAlphaNumeric(c: Char) = isDigit(c) || isAlpha(c)

internal fun isValidStringLiteral(lexeme: String) = Regex("^\"[^\"]*\"$").matches(lexeme)

internal fun isValidNumberLiteral(lexeme: String) = Regex("^\\d+(?:\\.\\d+)?\$").matches(lexeme)

internal fun isValidIdentifierLiteral(lexeme: String) =
        Regex("^[a-zA-Z_][a-zA-Z_0-9]*\$").matches(lexeme) && null == KeywordToken.Type.keywords[lexeme]