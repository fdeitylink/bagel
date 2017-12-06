package io.fdeitylink.kbagel

internal class BagelRuntimeError(val token: Token<*>, override val message: String) : RuntimeException()