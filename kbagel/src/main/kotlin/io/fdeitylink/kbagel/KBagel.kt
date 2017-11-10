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
    private val reporter = Reporter()

    @JvmStatic
    fun main(args: Array<String>) = when {
        args.size > 1 -> println("Usage: kbagel [script]")
        args.size == 1 -> runFile(args[0])
        else -> runPrompt()
    }

    @Throws(IOException::class)
    private fun runFile(path: String) {
        run(Files.lines(Paths.get(path), Charset.defaultCharset()).use { it.collect(Collectors.joining("\n")) })
        if (reporter.hadError) {
            //Kill the session
            exitProcess(65)
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
                        reporter.hadError = false
                    }
                }
            }

    private fun run(source: String) {
        val expr = Parser(Scanner(source, reporter).tokens, reporter).parsed
        if (reporter.hadError) {
            return
        }

        expr?.let(AstPrinter::print).also(::println)
    }

    private class Reporter : ErrorReporter() {
        //The setter visibility modifier allows the KBagel object to access the variable
        @Suppress("RedundantVisibilityModifier", "RedundantSetter")
        override var hadError = false
            public set

        override fun report(line: Int, location: String, lazyMessage: () -> Any) {
            System.err.println("[line $line] Error $location: ${lazyMessage()}")
            hadError = true
        }
    }
}