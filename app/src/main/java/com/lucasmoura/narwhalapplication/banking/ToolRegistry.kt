package com.lucasmoura.narwhalapplication.banking

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.ProvideStringSubgraphResult
import ai.koog.agents.ext.tool.AskUser

val toolRegistry = ToolRegistry {
    tool(AskUser)  // Allow agents to ask for clarification
    tools(MoneyTransferTools().asTools())
    tools(TransactionAnalysisTools().asTools())
    tool(ProvideStringSubgraphResult)
}