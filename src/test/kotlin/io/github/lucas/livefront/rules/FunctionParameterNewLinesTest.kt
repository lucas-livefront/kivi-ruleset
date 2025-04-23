package io.github.lucas.livefront.rules

import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class FunctionParameterNewLinesTest(private val env: KotlinCoreEnvironment) {
    @Nested
    inner class Constructors {
        private val rule = FunctionParameterNewLines(
            TestConfig(
                "maxArgumentsOnOneLine" to 2,
                "blacklist" to listOf("BlackListedPerson")
            )
        )

        @Test
        fun `reports call when argument number on one line exceeds max`() {
            val code = """
                data class Person(val name: String, val email: String?, val age: Int)
            
                fun main() {
                    Person("Mike", "mike@gmail.com", 25)
                }
            """.trimIndent()

            rule.compileAndLintWithContext(env, code) shouldHaveSize 1
        }

        @Test
        fun `doesn't report call when argument number on separate lines exceeds max`() {
            val code = """
                data class Person(val name: String, val email: String?, val age: Int)
            
                fun main() {
                    Person(
                        "Mike",
                        "mike@gmail.com",
                        25,
                    )
                }
            """.trimIndent()

            rule.compileAndLintWithContext(env, code) shouldHaveSize 0
        }

        @Test
        fun `doesn't report black listed call when argument number on one line exceeds max`() {
            val code = """
                data class BlackListedPerson(val name: String, val email: String?, val age: Int)
            
                fun main() {
                    BlackListedPerson("Mike", "mike@gmail.com", 25)
                }
            """.trimIndent()

            rule.compileAndLintWithContext(env, code) shouldHaveSize 0
        }

        @Nested
        inner class FunctionCalls {
            private val rule = FunctionParameterNewLines(
                TestConfig(
                    "maxArgumentsOnOneLine" to 2,
                    "blacklist" to listOf("doSomethingBlackListed")
                )
            )

            @Test
            fun `reports call when argument number on one line exceeds max`() {
                val code = """
                    fun doSomething(a: String, b: String, c: String)

                    fun main() {
                        doSomething("a", "b", "c")
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 1
            }

            @Test
            fun `doesn't report call when argument number on one lime matches max`() {
                val code = """
                    fun doSomething(a: String, b: String)

                    fun main() {
                        doSomething("a", "b")
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }

            @Test
            fun `doesn't report call when argument number on one line is less than max`() {
                val code = """
                    fun doSomething(a: String)

                    fun main() {
                        doSomething("a")
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }


            @Test
            fun `doesn't report call when argument number on separate lines exceeds max`() {
                val code = """
                    fun doSomething(a: String, b: String, c: String)

                    fun main() {
                        doSomething(
                            "a",
                            "b",
                            "c",
                        )
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }

            @Test
            fun `doesn't report black listed call when argument number on one line exceeds max`() {
                val code = """
                    fun doSomethingBlackListed(a: String, b: String, c: String)

                    fun main() {
                        doSomethingBlackListed("a", "b", "c")
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }

            @Test
            fun `doesn't report call when argument number on one line matches max and has trailing lambda`() {
                val code = """
                    fun doSomething(a: String, b: String, block: () -> Unit)

                    fun main() {
                        doSomething("a", "b") { println("c") }
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }

            @Test
            fun `reports call when argument number on one line exceeds max and has trailing lambda`() {
                val code = """
                    fun doSomething(a: String, b: String, c: String, block: () -> Unit)

                    fun main() {
                        doSomething("a", "b", "c") { println("d") }
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 1
            }

            @Test
            fun `reports call when argument number on one line exceeds max and middle argument is a lambda`() {
                val code = """
                    fun doSomething(a: String, block: () -> Unit, c: String)

                    fun main() {
                        doSomething("a", { println("b") }, "c")
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 1
            }

            @Test
            fun `reports call when argument number on one line exceeds max and last argument is a lambda`() {
                val code = """
                    fun doSomething(a: String, b: String, block: () -> Unit)

                    fun main() {
                        doSomething("a", "b", { println("b") })
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 1
            }

            @Test
            fun `doesn't report call when argument number on separate lines exceeds max and has trailing lambda`() {
                val code = """
                    fun doSomething(a: String, b: String, c: String, block: () -> Unit)

                    fun main() {
                        doSomething(
                            "a", 
                            "b",
                            "c",
                        ) { 
                            println("d") 
                        }
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }

            @Test
            fun `doesn't report call when argument number on one line matches max and one argument is a function call`() {
                val code = """
                    fun doSomething(a: String, b: String)
                    fun innerFunction(): String

                    fun main() {
                        doSomething("a", innerFunction())
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }

            @Test
            fun `doesn't report call when argument number on one line matches max and one argument is a function call with trailing lambda`() {
                val code = """
                    fun doSomething(a: String, b: String)
                    fun innerFunction(lambda: () -> Unit): String

                    fun main() {
                        doSomething("a", innerFunction { "b" })
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }

            @Test
            fun `doesn't report call when argument number on one line matches max and one argument is a function call with parameter and trailing lambda`() {
                val code = """
                    fun doSomething(a: String, b: String, lambda: () -> Unit)
                    fun innerFunction(a: String, lambda: () -> Unit): String

                    fun main() {
                        doSomething("a", innerFunction("a") { "b" })
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }

            @Test
            fun `doesn't report call when argument number on one line matches max and both arguments are function calls and one has a trailing lambda`() {
                val code = """
                    fun doSomething(a: String, b: String, lambda: () -> Unit)
                    fun innerFunctionOne(a: String, lambda: () -> Unit): String
                    fun innerFunctionTwo(a: String): String

                    fun main() {
                        doSomething(innerFunctionOne("a") { "b" }, innerFunction("c"))
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }

            @Test
            fun `doesn't report call when argument number on one line matches max and one argument is a function call with trailing lambda, with trailing lambda`() {
                val code = """
                    fun doSomething(a: String, b: String, lambda: () -> Unit)
                    fun innerFunction(lambda: () -> Unit): String

                    fun main() {
                        doSomething("a", innerFunction { "b" }) { "c" }
                    }
                """.trimIndent()

                rule.compileAndLintWithContext(env, code) shouldHaveSize 0
            }
        }
    }
}
