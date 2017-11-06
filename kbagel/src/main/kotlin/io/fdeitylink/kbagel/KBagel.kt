package io.fdeitylink.kbagel

import java.util.stream.Collectors

import java.nio.file.Paths
import java.nio.file.Files

import java.nio.charset.Charset

import java.io.BufferedReader
import java.io.InputStreamReader

import java.io.IOException

import kotlin.system.exitProcess

import io.fdeitylink.util.use

internal object KBagel {
    private var hadError = false

    @JvmStatic
    fun main(args: Array<String>) = when {
        args.size > 1 -> println("Usage: kbagel [script]")
        args.size == 1 -> runFile(args[0])
        else -> runPrompt()
    }

    fun report(line: Int, location: String = "", lazyMessage: () -> Any) {
        System.err.println("[line $line] Error $location: ${lazyMessage()}")
        hadError = true
    }

    fun error(token: Token<*>, lazyMessage: () -> Any) = when (token.type) {
        EOFToken.Type.EOF -> report(token.line, " at end", lazyMessage)
        else -> report(token.line, "at '${token.lexeme}'", lazyMessage)
    }

    @Throws(IOException::class)
    private fun runFile(path: String) {
        run(Files.lines(Paths.get(path), Charset.defaultCharset()).use { it.collect(Collectors.joining("\n")) })
        if (hadError) {
            exitProcess(65) //Kill the session
        }
    }

    @Throws(IOException::class)
    private fun runPrompt() =
            InputStreamReader(System.`in`).use {
                BufferedReader(it).use {
                    while (true) {
                        print("> ")
                        run(it.readLine())
                        //Even if they made an error, it shouldn't kill the REPL session
                        hadError = false
                    }
                }
            }

    private fun run(source: String) {
        val expr = Parser(Scanner(source).scanTokens()).parse()
        if (hadError) {
            return
        }

        expr?.let(AstPrinter::print)
    }
}