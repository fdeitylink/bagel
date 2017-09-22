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

private var hadError = false

fun main(args: Array<String>) {
    when {
        args.size > 1 -> println("Usage: kbagel [script]")
        args.size == 1 -> runFile(args[0])
        else -> runPrompt();
    }
}

internal fun report(line: Int, msg: String, where: String = "") {
    System.err.println("[line $line] Error $where: $msg")
    hadError = true
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
                    hadError = false //Even if they made an error, it shouldn't kill the session
                }
           }
        }

private fun run(source: String) = Scanner(source).scanTokens().forEach(::println)