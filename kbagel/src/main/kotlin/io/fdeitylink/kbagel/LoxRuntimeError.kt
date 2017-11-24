package io.fdeitylink.kbagel

internal class LoxRuntimeError(val token: Token<*>, override val message: String) : RuntimeException()