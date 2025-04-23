package io.github.lucas.livefront

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import io.github.lucas.livefront.rules.FunctionParameterNewLines

class KiviRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "KiviRuleSet"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                FunctionParameterNewLines(config),
            ),
        )
    }
}
