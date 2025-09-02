package com.lucasmoura.narwhalapplication.banking

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.ext.agent.SerializableSubgraphResult
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("UserRequestType")
@Serializable
@LLMDescription("Type of user request: Transfer or Analytics")
enum class RequestType {
    Transfer,
    Analytics
}

@Serializable
@LLMDescription("The bank request that was classified by the agent.")
data class ClassifiedBankRequest(
    @property:LLMDescription("Type of request: Transfer or Analytics")
    val requestType: RequestType,
    @property:LLMDescription("Actual request to be performed by the banking application")
    val userRequest: String
) : SerializableSubgraphResult<ClassifiedBankRequest> {


    override fun getSerializer(): KSerializer<ClassifiedBankRequest> {
        println(serializer())
        return serializer()
    }
}