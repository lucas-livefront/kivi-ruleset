package io.github.lucas.livefront.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity.CodeSmell
import io.gitlab.arturbosch.detekt.api.internal.valueOrDefaultCommaSeparated
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * A rule that asserts that function calls, including constructors, have their parameters on new lines after the number
 * of specified parameters exceeds a maximum value. This rule also allows users to blacklist certain functions or
 * constructors, so that they are not considered for this rule.
 */
class FunctionParameterNewLines(config: Config) : Rule(config) {
    override val issue = Issue(
        id = javaClass.simpleName,
        severity = CodeSmell,
        description = DESCRIPTION,
        debt = Debt.FIVE_MINS,
    )

    /**
     * The the max number of arguments that are allowed on one line.
     */
    private val maxArgumentsOnOneLine: Int = valueOrDefault(
        key = "maxArgumentsOnOneLine",
        default = 2,
    )

    /**
     * A list of black listed function names, for which this rule should not apply.
     */
    private val blacklist: List<String> = valueOrDefaultCommaSeparated(
        key = "blacklist",
        default = emptyList(),
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val calleeName = expression.calleeExpression?.text
        // Count arguments excluding trailing lambdas.
        val argumentCount = expression
            .valueArguments
            .filterNot {
                isTrailingLambda(
                    argument = it,
                    expression = expression,
                )
            }
            .size

        if (calleeName != null && calleeName !in blacklist && argumentCount > maxArgumentsOnOneLine) {
            val argumentsOnSameLine = expression.valueArguments
                .mapNotNull { it.getArgumentExpression() }
                .map { it.getLineNumber() }
                .distinct()
                .size == 1

            if (argumentsOnSameLine) {
                val message = "Calls to '$calleeName' with $argumentCount arguments must split arguments onto " +
                    "separate lines."

                report(
                    finding = io.gitlab.arturbosch.detekt.api.CodeSmell(
                        issue = issue,
                        entity = Entity.from(expression),
                        message = message,
                    ),
                )
            }
        }
    }
}

/**
 * Get the line number that [PsiElement] occurs on. Returns -1 if no containing file was found,
 */
private fun PsiElement.getLineNumber(): Int {
    val document = containingFile.viewProvider.document ?: return -1
    return document.getLineNumber(textOffset)
}

/**
 * Determine whether the argument is a trailing lambda.
 */
private fun isTrailingLambda(
    argument: KtValueArgument,
    expression: KtCallExpression,
): Boolean {
    val lambda = argument.getArgumentExpression() as? KtLambdaExpression
    val closingParen = expression.valueArgumentList?.rightParenthesis

    return if (lambda != null && closingParen != null) {
        lambda.textOffset > closingParen.textOffset
    } else {
        false
    }
}

/**
 * A description explaining the utility of this rule.
 */
private const val DESCRIPTION: String = "When there are many parameters, each one should be on a new line, that " +
    "way it will be more readable."

