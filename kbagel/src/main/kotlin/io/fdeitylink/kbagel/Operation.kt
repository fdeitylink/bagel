package io.fdeitylink.kbagel

internal interface Operation<O>
        where O : Operation<O>, O : Enum<O> {
    val tokenType: TokenType<*>

    companion object {
        /*
         * TODO:
         * Make this method protected when Kotlin supports calling it from companion objects of Operation implementers
         * ("Using non-JVM static members protected in the superclass companion is unsupported yet")
         */
        inline fun <reified O> operators()
                where O : Operation<O>, O : Enum<O> =
                enumValues<O>().associateBy(Operation<O>::tokenType)
    }
}