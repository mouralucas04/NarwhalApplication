package com.lucasmoura.narwhalapplication

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

val strategy = strategy<String, String>("Simple calculator And Person Searcher"){
    val nodeSendInput by nodeLLMRequest()
    val nodeExecuteTool by nodeExecuteTool()
    val nodeSendToolResult by nodeLLMSendToolResult()

    edge(nodeStart forwardTo nodeSendInput)

    edge(nodeSendInput forwardTo nodeFinish transformed { it } onAssistantMessage { true })

    edge(nodeSendInput forwardTo nodeExecuteTool onToolCall { true })

    edge(nodeExecuteTool forwardTo nodeSendToolResult)

    edge(nodeSendToolResult forwardTo nodeExecuteTool onToolCall { true })

    edge(nodeSendToolResult forwardTo nodeFinish transformed { it } onAssistantMessage { true })

}

//val agentConfig = AIAgentConfig.withSystemPrompt(
//    """
//            You are a simple calculator assistant.
//            You can add two numbers together using the calculator tool.
//            When the user provides input, extract the number they want to add.
//            The input might be in various formats like "add 5 and 7", "5+7", or just "5 7".
//            Extract the two numbers and use the calculator tool to add them.
//            Always respond with a clear, friendly message showing the calculation and result.
//        """.trimIndent()
//)

@LLMDescription("Tools for performing basic arithmetic operations")
class CalculatorTools: ToolSet {
    @Tool
    @LLMDescription("Add two number together and return their sum")
    fun add(
        @LLMDescription("First number to add (integer value)")
        num1: Int,
        @LLMDescription("Second number to add (integer value)")
        num2: Int
    ): String {
        val sum = num1 + num2
        return "The sum of $num1 and $num2 is: $sum."
    }

    @Tool
    @LLMDescription("Subtract two number together and return their subtraction")
    fun subtract(
        @LLMDescription("First number to subtract (integer value)")
        num1: Int,
        @LLMDescription("Second number to subtract (integer value)")
        num2: Int
    ): String {
        val sub = num1 - num2
        return "The subtraction of $num1 and $num2 is: $sub."
    }

    @Tool
    @LLMDescription("Divide two number together and return their division in Double type")
    fun division(
        @LLMDescription("First number to divide (integer value)")
        num1: Int,
        @LLMDescription("Second number to divide (integer value)")
        num2: Int
    ): String {
        val div = num1.toDouble() / num2.toDouble()
        return "The division of $num1 and $num2 is: $div."
    }

    @Tool
    @LLMDescription("Mutiply two number together and return their product")
    fun multiplication(
        @LLMDescription("First number to multiply (integer value)")
        num1: Int,
        @LLMDescription("Second number to multiply (integer value)")
        num2: Int
    ): String {
        val mult = num1 * num2
        return "The division of $num1 and $num2 is: $mult."
    }


}

@LLMDescription("""Use these if the user wants to find a person in a list. Tools which deals with a list of People. 
    |Each person has a name and age.""")
class PersonListWithAgeTools: ToolSet {

    private val people: List<Person> = listOf(
        Person("Alice", 23),
        Person("Lucas", 20),
        Person("John", 15)
    )

    @Tool
    @LLMDescription("""This function finds a person in the list by 
        their name and returns the person object. If it does not find the person, it returns null.""")
    fun findByName(
        @LLMDescription("Name of the person to find")
        name: String
    ): Person? {
        val person = people.find { it.name == name }
        return person
    }
}

@Serializable
data class Person(
    val name: String,
    val age: Int
)

val toolRegistry = ToolRegistry {
    tools(CalculatorTools())
    tools(PersonListWithAgeTools())
    tool(SayToUser)
}

val agentConfig = AIAgentConfig(
    prompt = Prompt.build("simple-calculator"){
        system("""
            You are a simple calculator assistant.
            You can do any operation (add, subtract, multiply and divide) with two numbers together using the calculator tool.
            When the user provides input, extract the number they want to add.
            The input might be in various formats like "add 5 and 7", "5 (operation) 7".
            Extract the two numbers and use the calculator tool to add them.
            Always respond with a clear, friendly message showing the calculation and result.
        """.trimIndent())
    },
    model = OpenAIModels.Chat.GPT4o,
    maxAgentIterations = 10
)


fun main(): Unit = runBlocking {


    val agent = AIAgent(
        executor = simpleOpenAIExecutor(BuildConfig.API_KEY),
        llmModel = OpenAIModels.Chat.GPT4o,
        toolRegistry = toolRegistry,
        strategy = strategy,
        systemPrompt = """
            You are a simple calculator assistant.
            You can add two numbers together using the calculator tool.
            When the user provides input, extract the number they want to add.
            The input might be in various formats like "add 5 and 7", "5+7", or just "5 7".
            Extract the two numbers and use the calculator tool to add them.
            Also, given the user has sent a name for search, you can find the person with
            this name with PersonListWithAgeTools.
            Always respond with a clear, friendly message showing the calculation and result.
        """.trimIndent(),
        maxIterations = 30,
        installFeatures = {
            handleEvents {
                onBeforeNode { bne ->
                    println("Before node: ${bne.node}")
                }
                onToolCall { tcc ->
                    println("Tool called: tool ${tcc.tool.name}, args: ${tcc.toolArgs}")
                }
            }

        }
    )

    println("Enter two numbers to do an operation")
    val userInput = readlnOrNull() ?: ""

    val agentResult = agent.run(userInput)
    println(agentResult)
}