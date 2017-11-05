package io.fdeitylink.kbagel

internal class AstPrinter : Expr.Visitor<String>() {
    fun print(expr: Expr) = expr.accept(this)

    override fun visit(u: Expr.Unary) = parenthesize(u.op.tokenType.char.toString(), u.operand)

    override fun visit(b: Expr.Binary): String {
        val tokenType = b.op.tokenType
        val lexeme = when (tokenType) {
            is SingleCharToken.Type -> tokenType.char.toString()
            is MultiCharToken.Type -> tokenType.chars
            is KeywordToken.Type -> tokenType.chars
            else -> throw Error()
        }

        return parenthesize(lexeme, b.lOperand, b.rOperand)
    }

    override fun visit(t: Expr.Ternary) = parenthesize("ternary", t.cond, t.thenBranch, t.elseBranch)

    override fun visit(l: Expr.Literal) = if (null == l.value) "nil" else l.value.toString()

    override fun visit(g: Expr.Grouping) = parenthesize("group", g.expr)

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()

        builder.append('(').append(name)
        exprs.forEach { builder.append(' ').append(it.accept(this)) }
        builder.append(')')

        return builder.toString()
    }
}