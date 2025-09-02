package com.lucasmoura.narwhalapplication.banking

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.asTool
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.lucasmoura.narwhalapplication.BuildConfig
import kotlinx.coroutines.runBlocking

val bankingAssistantSystemPrompt = """
        |You are a banking assistant interacting with a user (userId=123).
        |Your goal is to understand the user's request and determine whether it can be fulfilled using the available tools.
        |If the task can be accomplished with the provided tools, proceed accordingly,
        |at the end of the conversation respond with: "Task completed successfully."
        |After an operation is succeded, respond with: "Operation completed successfully."
        |If it is not, respond with: "Operation failed."
        |If the task cannot be performed with the tools available, respond with: "Can't perform the task.
        |Always start the conversation with the user, by greeting him and asking what should you do."
    """.trimMargin()

val classifierSystemPrompt = """
You are a banking assistant.
You must ALWAYS choose one of the available tools to handle the request. 
Do NOT ask the user for extra details unless the tool requires it. 
Rules:
- If the request involves sending money, use the tool: transferMoney
- If the request involves analyzing past transactions, use the tool: analyzeTransactions
At the end of every successful operation, say: "Task completed successfully."
If no tool can handle the request, say: "Can't perform the task."
""".trimIndent()

val agentConfig = AIAgentConfig(
    prompt = prompt(id = "banking assistant") {
        system("$bankingAssistantSystemPrompt\n$transactionAnalysisPrompt")
    },
    model = OpenAIModels.Chat.GPT4o,
    maxAgentIterations = 50  // Allow for complex multi-step operations
)

fun main(){

    val openAiExecutor = simpleOpenAIExecutor(BuildConfig.API_KEY)

    val analysisAgent = AIAgent(
        executor = openAiExecutor,
        llmModel = OpenAIModels.Reasoning.GPT4oMini,
        systemPrompt = transactionAnalysisPrompt,
        temperature = 0.0,
        toolRegistry = ToolRegistry {
            tool(AskUser)
            tools(TransactionAnalysisTools().asTools())
        }
    )

    val transferAgent = AIAgent(
        executor = openAiExecutor,
        llmModel = OpenAIModels.Reasoning.GPT4oMini,
        systemPrompt = bankingAssistantSystemPrompt,
        temperature = 0.0,
        toolRegistry = ToolRegistry {
            tool(AskUser)
            tools(MoneyTransferTools().asTools())
        },
    )

    val classifierAgent = AIAgent(
        executor = openAiExecutor,
        llmModel = OpenAIModels.Chat.GPT4o,
        systemPrompt = classifierSystemPrompt,
        toolRegistry = ToolRegistry {
            tool(AskUser)

            tool(
                transferAgent.asTool(
                    agentName = "transferMoney",
                    agentDescription = "Transfers money and handles all related operations",
                    inputDescriptor = ToolParameterDescriptor(
                        name = "request",
                        description = "Transfer request from the user",
                        type = ToolParameterType.String
                    ),
                )
            )

            tool(
                analysisAgent.asTool(
                    agentName = "analyzeTransactions",
                    agentDescription = "Performs analytics on user transactions",
                    inputDescriptor = ToolParameterDescriptor(
                        name = "request",
                        description = "Transaction analytics request",
                        type = ToolParameterType.String
                    )
                )
            )
        }

    ){
        handleEvents {
            onBeforeNode { ctx ->
                println("Entered node name: ${ctx.node.name}")
                println("Node input: ${ctx.input}")
                println("Node Context: ${ctx.context}")
                println("Input type: ${ctx.inputType}")
                println("----------------------------------------------\n")
            }
            onToolCall { ctx ->
                println("Tool called: ${ctx.tool.name}")
                println("Tool Args: ${ctx.toolArgs}")
                println("Tool Call ID: ${ctx.toolCallId}")
                println("----------------------------------------------\n")
            }
            onStrategyStarted { ctx ->
                println("Strategy started: ${ctx.strategy.name}")
                println("Strategy Input Type: ${ctx.strategy.inputType}")
                println("----------------------------------------------\n")
            }
            onStrategyFinished { ctx ->
                println("Strategy finished: ${ctx.strategy.name}")
                println("Strategy Input Type: ${ctx.strategy.inputType}")
                println("Strategy Result Type: ${ctx.resultType}")
                println("----------------------------------------------\n")
            }
        }
    }

    val agent = AIAgent<String, String>(
        promptExecutor = openAiExecutor,
        strategy = customStrategy,
        agentConfig = agentConfig,
        toolRegistry = toolRegistry,
    ){
        handleEvents {
            onBeforeNode { ctx ->
                println("Entered node name: ${ctx.node.name}")
                println("Node input: ${ctx.input}")
                println("Node Context: ${ctx.context}")
                println("Input type: ${ctx.inputType}")
                println("----------------------------------------------\n")
            }
            onToolCall { ctx ->
                println("Tool called: ${ctx.tool.name}")
                println("Tool Args: ${ctx.toolArgs}")
                println("Tool Call ID: ${ctx.toolCallId}")
                println("----------------------------------------------\n")
            }
            onStrategyStarted { ctx ->
                println("Strategy started: ${ctx.strategy.name}")
                println("Strategy Input Type: ${ctx.strategy.inputType}")
                println("----------------------------------------------\n")
            }
            onStrategyFinished { ctx ->
                println("Strategy finished: ${ctx.strategy.name}")
                println("Strategy Input Type: ${ctx.strategy.inputType}")
                println("Strategy Result Type: ${ctx.resultType}")
                println("----------------------------------------------\n")
            }
        }
    }
//
//    println("Banking Assistant started")
//    val message = "I want to tranfer 200 euros to Daniel for the concert tickets."
//
//    // Other test messages you can try:
//    // - "Send 50 euros to Alice for the concert tickets"
//    // - "What's my current balance?"
//    // - "Transfer 100 euros to Bob for the shared vacation expenses"
//
//    runBlocking {
//        val result = transferAgent.run(message)
//        println(result)
//    }

//
//    println("Transaction Analysis Assistant started")
//    val analysisMessage = "How much have I spent on restaurants this month?"
//
//    // Other queries to try:
//    // - "What's my maximum check at a restaurant this month?"
//    // - "How much did I spend on groceries in the first week of May?"
//    // - "What's my total spending on entertainment in May?"
//    // - "Show me all transactions from last week"
//
//    runBlocking {
//        val result = analysisAgent.run(analysisMessage)
//        println(result)
//    }

    println("Banking Assistant started")
    val testMessage = "Transfer 100 to Bob for groceries"

// Test various scenarios:
// Transfer requests:
//   - "Send 50 euros to Alice for the concert tickets"
//   - "Transfer 100 to Bob for groceries"
//   - "What's my current balance?"
//
// Analytics requests:
//   - "How much have I spent on restaurants this month?"
//   - "What's my maximum check at a restaurant this month?"
//   - "How much did I spend on groceries in the first week of May?"
//   - "What's my total spending on entertainment in May?"

    runBlocking {
        val result = agent.run(testMessage)
        println("Result: $result")
    }

}