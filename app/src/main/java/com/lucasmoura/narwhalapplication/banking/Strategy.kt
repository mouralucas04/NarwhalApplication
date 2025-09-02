package com.lucasmoura.narwhalapplication.banking

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.agents.ext.tool.AskUser
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.structure.json.JsonStructureLanguage
import ai.koog.prompt.structure.json.JsonStructuredData
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject


val classifiedBankRequestSchema: LLMParams.Schema.JSON = LLMParams.Schema.JSON.Simple(
    name = "classifiedBankRequestSchema",
    schema = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put("properties", buildJsonObject {
            put("requestType", buildJsonObject {
                put("type", JsonPrimitive("string"))
                put("enum", buildJsonArray {
                    add(JsonPrimitive("Transfer"))
                    add(JsonPrimitive("Analytics"))
                })
            })
            put("userRequest", buildJsonObject {
                put("type", JsonPrimitive("string"))
                put(
                    "description",
                    JsonPrimitive("Actual request to be performed by the banking application")
                )
            })
        })
        put("required", buildJsonArray {
            add(JsonPrimitive("requestType"))
            add(JsonPrimitive("userRequest"))
        })
    }
)

val customStrategy = strategy<String, String>("banking assistant") {

    // Subgraph for classifying user requests

    val classifyRequest by subgraph<String, ClassifiedBankRequest>(
        tools = listOf(AskUser)
    ) {
        // Use structured output to ensure proper classification
        val requestClassificationNode by nodeLLMRequestStructured<ClassifiedBankRequest>(
            fixingModel = OpenAIModels.CostOptimized.GPT4oMini,
            structure = JsonStructuredData<ClassifiedBankRequest>(
                id = "ClassifiedBankRequest",
                serializer = ClassifiedBankRequest.serializer(),
                structureLanguage = JsonStructureLanguage(),
                examples = listOf(
                    ClassifiedBankRequest(
                        requestType = RequestType.Transfer,
                        userRequest = "Send 25 euros to Daniel for dinner at the restaurant."
                    ),
                    ClassifiedBankRequest(
                        requestType = RequestType.Analytics,
                        userRequest = "Provide transaction overview for the last month"
                    )
                ),
                jsonSchema = classifiedBankRequestSchema,
            ),
            retries = 2,
        )

        val callLLM by nodeLLMRequest()
        val callAskUserTool by nodeExecuteTool()

        // Define the flow
        edge(nodeStart forwardTo requestClassificationNode)

        edge(
            requestClassificationNode forwardTo nodeFinish
                    onCondition {
                println("Result: $it")
                println("Raw: ${it.getOrThrow().raw}")
                it.isSuccess
            }
                    transformed {
                it.getOrThrow().structure
            }
        )

        edge(
            requestClassificationNode forwardTo callLLM
                    onCondition { it.isFailure }
                    transformed { "Failed to understand the user's intent" }
        )

        edge(callLLM forwardTo callAskUserTool onToolCall { true })

        edge(
            callLLM forwardTo callLLM onAssistantMessage { true }
                    transformed { "Please call `${AskUser.name}` tool instead of chatting" }
        )

        edge(
            callAskUserTool forwardTo requestClassificationNode
                    transformed {
                        it.result.toString()
                    }
        )
    }

    // Subgraph for handling money transfers
    val transferMoney by subgraphWithTask<ClassifiedBankRequest>(
        tools = MoneyTransferTools().asTools() + AskUser,
        llmModel = OpenAIModels.Chat.GPT4o  // Use more capable model for transfers
    ) { request ->
        """
        $bankingAssistantSystemPrompt
        Specifically, you need to help with the following request:
        ${request.userRequest}
        """.trimIndent()
    }

    // Subgraph for transaction analysis
    val transactionAnalysis by subgraphWithTask<ClassifiedBankRequest>(
        tools = TransactionAnalysisTools().asTools() + AskUser,
    ) { request ->
        """
        $transactionAnalysisPrompt
        Specifically, you need to help with the following request:
        ${request.userRequest}
        """.trimIndent()
    }

    // Connect the subgraphs
    edge(nodeStart forwardTo classifyRequest)

    edge(
        classifyRequest forwardTo transferMoney
                onCondition { it.requestType == RequestType.Transfer })

    edge(
        classifyRequest forwardTo transactionAnalysis
                onCondition { it.requestType == RequestType.Analytics })

    // Route results to finish node
    edge(transferMoney forwardTo nodeFinish transformed { it.result })
    edge(transactionAnalysis forwardTo nodeFinish transformed { it.result })
}