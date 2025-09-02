package com.lucasmoura.narwhalapplication.banking

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: Int,
    val name: String,
    val surname: String? = null,
    val phoneNumber: String
)

val contactList = listOf(
    Contact(100, "Alice", "Smith", "+1 415 555 1234"),
    Contact(101, "Bob", "Johnson", "+49 151 23456789"),
    Contact(102, "Charlie", "Williams", "+36 20 123 4567"),
    Contact(103, "Daniel", "Anderson", "+46 70 123 45 67"),
    Contact(104, "Daniel", "Garcia", "+34 612 345 678"),
)

val contactByID = contactList.associateBy(Contact::id)

@LLMDescription("Tools for money tranfer operations, getting contacts and getting own balance")
class MoneyTransferTools: ToolSet {

    @Tool
    @LLMDescription(
        """
        Returns the list of contacts for the given user.
        The user in this demo is always userId=123.
        """)
    fun getContacts(
        @LLMDescription("The unique identifier of the user whose contact list is requested.")
        userId: Int
    ): String = buildString {
        contactList.forEach { contact ->
            appendLine("ID: ${contact.id}, Name: ${contact.name}, Surname: ${contact.surname}, Phone Number: ${contact.phoneNumber}")
        }
    }.trimEnd()

    @Tool
    @LLMDescription("Returns the current balance (demo value).")
    fun getBalance(
        @LLMDescription("The unique identifier of the user.")
        userId: Int
    ): String = "Balance: 200.00 EUR"

    @Tool
    @LLMDescription("Returns the default user currency (demo value).")
    fun getDefaultCurrency(
        @LLMDescription("The unique identifier of the user.")
        userId: Int
    ): String = "EUR"

    @Tool
    @LLMDescription("Returns a demo FX rate between two ISO currencies (e.g. EUR→USD).")
    fun getExchangeRate(
        @LLMDescription("Base currency (e.g., EUR).") from: String,
        @LLMDescription("Target currency (e.g., USD).") to: String
    ): String = when (from.uppercase() to to.uppercase()) {
        "EUR" to "USD" -> "1.10"
        "EUR" to "GBP" -> "0.86"
        "GBP" to "EUR" -> "1.16"
        "USD" to "EUR" -> "0.90"
        else -> "No information about exchange rate available."
    }

    @Tool
    @LLMDescription(
        """
        Returns a ranked list of possible recipients for an ambiguous name.
        The agent should ask the user to pick one and then use the selected contact id.
        """
    )
    fun chooseRecipient(
        @LLMDescription("An ambiguous or partial contact name.") confusingRecipientName: String
    ): String {
        val matchingContacts = contactList.filter { c ->
            c.name.contains(confusingRecipientName, ignoreCase = true) ||
                    c.surname?.contains(confusingRecipientName, ignoreCase = true) ?: false
        }

        if (matchingContacts.isEmpty()) {
            return "No candidates found for '$confusingRecipientName'. Use getContacts and ask the user to choose."
        }

        return matchingContacts.mapIndexed { idx, c ->
            "${idx + 1}. ${c.id}: ${c.name} ${c.surname ?: ""} (${c.phoneNumber})"
        }.joinToString("\n")

    }

    @Tool
    @LLMDescription(
        """
        Sends money from the user to a contact.
        If confirmed=false, return "REQUIRES_CONFIRMATION" with a human-readable summary.
        The agent should confirm with the user before retrying with confirmed=true.
        """
    )
    fun sendMoney(
        @LLMDescription("Sender user id.") senderId: Int,
        @LLMDescription("Amount in sender's default currency.") amount: Double,
        @LLMDescription("Recipient contact id.") recipientId: Int,
        @LLMDescription("Short purpose/description.") purpose: String,
        @LLMDescription("Whether the user already confirmed this transfer.") confirmed: Boolean = false
    ): String {
        val recipient = contactByID[recipientId] ?: return "Invalid recipient."
        val summary = "Transfer €%.2f to %s %s (%s) for \"%s\"."
            .format(amount, recipient.name, recipient.surname ?: "", recipient.phoneNumber, purpose)

        if (!confirmed) {
            return "REQUIRES_CONFIRMATION: $summary"
        }

        // In a real system this is where you'd call a payment API.
        return "Money was sent. $summary"
    }

}

