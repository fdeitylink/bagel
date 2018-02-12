package io.fdeitylink.kbagel

internal sealed class Stmt {
    fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)

    interface Visitor<out R> {
        @Suppress("UNCHECKED_CAST")
        fun visit(s: Stmt) =
                Visitor::class.java.declaredMethods
                        .first { it.parameterTypes.first() == s::class.java }
                        .invoke(this, s) as R

        //TODO: Make these methods protected again once it's supported in Kotlin (IIRC Java 9 supports it)
        fun visit(e: Expression): R

        fun visit(p: Print): R

        fun visit(v: Var): R

        fun visit(b: Block): R

        fun visit(i: If): R

        fun visit(w: While): R

        fun visit(b: Break): R
    }

    class Expression(val expr: Expr) : Stmt()

    class Print(val expr: Expr) : Stmt()

    class Var(val name: IdentifierToken, val initializer: Expr?) : Stmt()

    class Block(val stmts: List<Stmt>) : Stmt()

    class If(val cond: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()

    class While(val cond: Expr, val body: Stmt) : Stmt()

    class Break : Stmt()
}