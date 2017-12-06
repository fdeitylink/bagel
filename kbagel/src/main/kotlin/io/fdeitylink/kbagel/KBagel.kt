package io.fdeitylink.kbagel

import java.util.stream.Collectors

import java.nio.file.Paths
import java.nio.file.Files

import java.nio.charset.Charset

import java.io.BufferedReader
import java.io.InputStreamReader

import java.io.IOException

import kotlin.system.exitProcess

internal object KBagel {
    private const val scanParseErrorCode = 65
    private const val runtimeErrorCode = 70

    private val interpreter = Interpreter(Reporter)

    @JvmStatic
    fun main(args: Array<String>) = when {
        args.size > 1 -> println("Usage: kbagel [script]")
        args.size == 1 -> runFile(args[0])
        else -> runPrompt()
    }

    @Throws(IOException::class)
    private fun runFile(path: String) {
        run(Files.lines(Paths.get(path), Charset.defaultCharset()).use { it.collect(Collectors.joining("\n")) })
        if (Reporter.hadScanParseError) {
            exitProcess(scanParseErrorCode)
        }
        if (Reporter.hadRuntimeError) {
            exitProcess(runtimeErrorCode)
        }
    }

    @Throws(IOException::class)
    private fun runPrompt() =
            InputStreamReader(System.`in`).use {
                BufferedReader(it).use {
                    while (true) {
                        print("> ")
                        run(it.readLine())
                        //Even if the user made an error, it shouldn't kill the REPL session
                        Reporter.hadScanParseError = false
                    }
                }
            }

    private fun run(source: String) {
        val expr = Parser(Scanner(source, Reporter).tokens, Reporter).parsed
        if (Reporter.hadScanParseError) {
            return
        }

        expr?.let(interpreter::interpret).also(::println)
    }

    private object Reporter : ErrorReporter() {
        //The setter visibility modifiers allows the KBagel object to access the member variables
        @Suppress("RedundantVisibilityModifier", "RedundantSetter")
        override var hadScanParseError = false
            public set

        override var hadRuntimeError = false

        override fun report(line: Int, message: String, location: String) {
            System.err.println("[line $line] Error $location: $message")
            hadScanParseError = true
        }

        override fun report(err: BagelRuntimeError) {
            System.err.println("${err.message}\n[line ${err.token.line}]")
            hadRuntimeError = true
        }
    }
}