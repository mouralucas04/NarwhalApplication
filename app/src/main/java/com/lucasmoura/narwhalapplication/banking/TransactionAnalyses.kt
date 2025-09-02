package com.lucasmoura.narwhalapplication.banking

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionCategory(val title: String) {
    FOOD_AND_DINING("Food & Dining"),
    SHOPPING("Shopping"),
    TRANSPORTATION("Transportation"),
    ENTERTAINMENT("Entertainment"),
    GROCERIES("Groceries"),
    HEALTH("Health"),
    UTILITIES("Utilities"),
    HOME_IMPROVEMENT("Home Improvement");

    companion object {
        fun fromString(value: String): TransactionCategory? =
            entries.find { it.title.equals(value, ignoreCase = true) }

        fun availableCategories(): String =
            entries.joinToString(", ") { it.title }
    }
}

@Serializable
data class Transaction(
    val merchant: String,
    val amount: Double,
    val category: TransactionCategory,
    val date: LocalDateTime
)

val transactionAnalysisPrompt = """
Today is 2025-05-22.
Available categories for transactions: ${TransactionCategory.availableCategories()}
"""

val sampleTransactions = listOf(
    Transaction("Starbucks", 5.99, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 22, 8, 30, 0, 0)),
    Transaction("Amazon", 129.99, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 22, 10, 15, 0, 0)),
    Transaction(
        "Shell Gas Station",
        45.50,
        TransactionCategory.TRANSPORTATION,
        LocalDateTime(2025, 5, 21, 18, 45, 0, 0)
    ),
    Transaction("Netflix", 15.99, TransactionCategory.ENTERTAINMENT, LocalDateTime(2025, 5, 21, 12, 0, 0, 0)),
    Transaction("AMC Theaters", 32.50, TransactionCategory.ENTERTAINMENT, LocalDateTime(2025, 5, 20, 19, 30, 0, 0)),
    Transaction("Whole Foods", 89.75, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 20, 16, 20, 0, 0)),
    Transaction("Target", 67.32, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 20, 14, 30, 0, 0)),
    Transaction("CVS Pharmacy", 23.45, TransactionCategory.HEALTH, LocalDateTime(2025, 5, 19, 11, 25, 0, 0)),
    Transaction("Subway", 12.49, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 19, 13, 15, 0, 0)),
    Transaction("Spotify Premium", 9.99, TransactionCategory.ENTERTAINMENT, LocalDateTime(2025, 5, 19, 14, 15, 0, 0)),
    Transaction("AT&T", 85.00, TransactionCategory.UTILITIES, LocalDateTime(2025, 5, 18, 9, 0, 0, 0)),
    Transaction("Home Depot", 156.78, TransactionCategory.HOME_IMPROVEMENT, LocalDateTime(2025, 5, 18, 15, 45, 0, 0)),
    Transaction("Amazon", 129.99, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 17, 10, 15, 0, 0)),
    Transaction("Starbucks", 5.99, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 17, 8, 30, 0, 0)),
    Transaction("Whole Foods", 89.75, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 16, 16, 20, 0, 0)),
    Transaction("CVS Pharmacy", 23.45, TransactionCategory.HEALTH, LocalDateTime(2025, 5, 15, 11, 25, 0, 0)),
    Transaction("AT&T", 85.00, TransactionCategory.UTILITIES, LocalDateTime(2025, 5, 14, 9, 0, 0, 0)),
    Transaction("Xbox Game Pass", 14.99, TransactionCategory.ENTERTAINMENT, LocalDateTime(2025, 5, 14, 16, 45, 0, 0)),
    Transaction("Aldi", 76.45, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 13, 17, 30, 0, 0)),
    Transaction("Chipotle", 15.75, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 13, 12, 45, 0, 0)),
    Transaction("Best Buy", 299.99, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 12, 14, 20, 0, 0)),
    Transaction("Olive Garden", 89.50, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 12, 19, 15, 0, 0)),
    Transaction("Whole Foods", 112.34, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 11, 10, 30, 0, 0)),
    Transaction("Old Navy", 45.99, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 11, 13, 45, 0, 0)),
    Transaction("Panera Bread", 18.25, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 10, 11, 30, 0, 0)),
    Transaction("Costco", 245.67, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 10, 15, 20, 0, 0)),
    Transaction("Five Guys", 22.50, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 9, 18, 30, 0, 0)),
    Transaction("Macy's", 156.78, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 9, 14, 15, 0, 0)),
    Transaction("Hulu Plus", 12.99, TransactionCategory.ENTERTAINMENT, LocalDateTime(2025, 5, 8, 20, 0, 0, 0)),
    Transaction("Whole Foods", 94.23, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 8, 16, 45, 0, 0)),
    Transaction("Texas Roadhouse", 78.90, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 8, 19, 30, 0, 0)),
    Transaction("Walmart", 167.89, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 7, 11, 20, 0, 0)),
    Transaction("Chick-fil-A", 14.75, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 7, 12, 30, 0, 0)),
    Transaction("Aldi", 82.45, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 6, 15, 45, 0, 0)),
    Transaction("TJ Maxx", 67.90, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 6, 13, 20, 0, 0)),
    Transaction("P.F. Chang's", 95.40, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 5, 19, 15, 0, 0)),
    Transaction("Whole Foods", 78.34, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 4, 14, 30, 0, 0)),
    Transaction("H&M", 89.99, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 3, 16, 20, 0, 0)),
    Transaction("Red Lobster", 112.45, TransactionCategory.FOOD_AND_DINING, LocalDateTime(2025, 5, 2, 18, 45, 0, 0)),
    Transaction("Whole Foods", 67.23, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 2, 11, 30, 0, 0)),
    Transaction("Marshalls", 123.45, TransactionCategory.SHOPPING, LocalDateTime(2025, 5, 1, 15, 20, 0, 0)),
    Transaction(
        "Buffalo Wild Wings",
        45.67,
        TransactionCategory.FOOD_AND_DINING,
        LocalDateTime(2025, 5, 1, 19, 30, 0, 0)
    ),
    Transaction("Aldi", 145.78, TransactionCategory.GROCERIES, LocalDateTime(2025, 5, 1, 10, 15, 0, 0))
)

@LLMDescription("Tools for analyzing transaction history")
class TransactionAnalysisTools : ToolSet {


    @Tool
    @LLMDescription(
        """
        Retrieves transactions filtered by userId, category, start date, and end date.
        All parameters are optional. If no parameters are provided, all transactions are returned.
        Dates should be in the format YYYY-MM-DD.
        """
    )
    fun getTransactions(
        @LLMDescription("The ID of the user whose transactions to retrieve.")
        userId: String? = null,
        @LLMDescription("The category to filter transactions by (e.g., 'Food & Dining').")
        category: String? = null,
        @LLMDescription("The start date to filter transactions by, in the format YYYY-MM-DD.")
        startDate: String? = null,
        @LLMDescription("The end date to filter transactions by, in the format YYYY-MM-DD.")
        endDate: String? = null
    ): String {
        var filteredTransactions = sampleTransactions

        // Validate userId (in production, this would query a real database)
        if (userId != null && userId != "123") {
            return "No transactions found for user $userId."
        }

        // Apply category filter
        category?.let { cat ->
            val categoryEnum = TransactionCategory.fromString(cat)
                ?: return "Invalid category: $cat. Available: ${TransactionCategory.availableCategories()}"
            filteredTransactions = filteredTransactions.filter { it.category == categoryEnum }
        }

        // Apply date range filters
        startDate?.let { date ->
            val startDateTime = parseDate(date, startOfDay = true)
            filteredTransactions = filteredTransactions.filter { it.date >= startDateTime }
        }

        endDate?.let { date ->
            val endDateTime = parseDate(date, startOfDay = false)
            filteredTransactions = filteredTransactions.filter { it.date <= endDateTime }
        }

        if (filteredTransactions.isEmpty()) {
            return "No transactions found matching the specified criteria."
        }

        return filteredTransactions.joinToString("\n") { transaction ->
            "${transaction.date}: ${transaction.merchant} - " +
                    "$${transaction.amount} (${transaction.category.title})"
        }
    }

    @Tool
    @LLMDescription("Calculates the sum of an array of double numbers.")
    fun sumArray(
        @LLMDescription("Comma-separated list of double numbers to sum (e.g., '1.5,2.3,4.7').")
        numbers: String
    ): String {
        val numbersList = numbers.split(",")
            .mapNotNull { it.trim().toDoubleOrNull() }
        val sum = numbersList.sum()
        return "Sum: $%.2f".format(sum)
    }


    private fun parseDate(dateStr: String, startOfDay: Boolean): LocalDateTime {
        val parts = dateStr.split("-").map { it.toInt() }
        require(parts.size == 3) { "Invalid date format. Use YYYY-MM-DD" }

        return if (startOfDay) {
            LocalDateTime(parts[0], parts[1], parts[2], 0, 0, 0, 0)
        } else {
            LocalDateTime(parts[0], parts[1], parts[2], 23, 59, 59, 999999999)
        }
    }


}
