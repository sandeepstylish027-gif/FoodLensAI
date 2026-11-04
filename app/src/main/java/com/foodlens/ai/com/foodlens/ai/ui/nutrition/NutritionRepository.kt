package com.foodlens.ai.ui.nutrition

data class NutritionInfo(
    val calories: Int,
    val protein: Double,
    val carbohydrates: Double,
    val fat: Double
)

object NutritionRepository {

    // Approximate reference values per 100 g; beverages are approximate per 100 ml.
    // Actual values vary with food variety, preparation, recipe, and brand.
    private val nutritionData = mapOf(
        "rice" to NutritionInfo(130, 2.7, 28.2, 0.3),
        "chicken duck" to NutritionInfo(165, 31.0, 0.0, 3.6),
        "chicken" to NutritionInfo(165, 31.0, 0.0, 3.6),
        "duck" to NutritionInfo(201, 23.5, 0.0, 11.2),
        "onion" to NutritionInfo(40, 1.1, 9.3, 0.1),
        "spring onion" to NutritionInfo(32, 1.8, 7.3, 0.2),
        "cucumber" to NutritionInfo(15, 0.7, 3.6, 0.1),
        "lemon" to NutritionInfo(29, 1.1, 9.3, 0.3),
        "tomato" to NutritionInfo(18, 0.9, 3.9, 0.2),
        "potato" to NutritionInfo(77, 2.0, 17.5, 0.1),
        "carrot" to NutritionInfo(41, 0.9, 9.6, 0.2),
        "broccoli" to NutritionInfo(34, 2.8, 6.6, 0.4),
        "cabbage" to NutritionInfo(25, 1.3, 5.8, 0.1),
        "cauliflower" to NutritionInfo(25, 1.9, 5.0, 0.3),
        "spinach" to NutritionInfo(23, 2.9, 3.6, 0.4),
        "apple" to NutritionInfo(52, 0.3, 13.8, 0.2),
        "banana" to NutritionInfo(89, 1.1, 22.8, 0.3),
        "orange" to NutritionInfo(47, 0.9, 11.8, 0.1),
        "mango" to NutritionInfo(60, 0.8, 15.0, 0.4),
        "pineapple" to NutritionInfo(50, 0.5, 13.1, 0.1),
        "watermelon" to NutritionInfo(30, 0.6, 7.6, 0.2),
        "avocado" to NutritionInfo(160, 2.0, 8.5, 14.7),
        "peanut" to NutritionInfo(567, 25.8, 16.1, 49.2),
        "walnut" to NutritionInfo(654, 15.2, 13.7, 65.2),
        "almond" to NutritionInfo(579, 21.2, 21.6, 49.9),
        "cashew" to NutritionInfo(553, 18.2, 30.2, 43.9),
        "egg" to NutritionInfo(155, 13.0, 1.1, 11.0),
        "bread" to NutritionInfo(265, 9.0, 49.0, 3.2),
        "cheese butter" to NutritionInfo(717, 0.9, 0.1, 81.1),
        "cheese" to NutritionInfo(402, 25.0, 1.3, 33.0),
        "butter" to NutritionInfo(717, 0.9, 0.1, 81.1),
        "pizza" to NutritionInfo(266, 11.0, 33.0, 10.0),
        "pasta" to NutritionInfo(131, 5.0, 25.0, 1.1),
        "noodles" to NutritionInfo(138, 4.5, 25.0, 2.1),
        "hamburg" to NutritionInfo(295, 17.0, 24.0, 14.0),
        "french fries" to NutritionInfo(312, 3.4, 41.0, 15.0),
        "popcorn" to NutritionInfo(387, 12.9, 77.8, 4.5),
        "steak" to NutritionInfo(271, 25.0, 0.0, 19.0),
        "pork" to NutritionInfo(242, 27.3, 0.0, 14.0),
        "lamb" to NutritionInfo(294, 25.6, 0.0, 20.9),
        "fish" to NutritionInfo(206, 22.0, 0.0, 12.0),
        "shrimp" to NutritionInfo(99, 24.0, 0.2, 0.3),
        "tofu" to NutritionInfo(76, 8.0, 1.9, 4.8),
        "corn" to NutritionInfo(86, 3.3, 19.0, 1.4),
        "green beans" to NutritionInfo(31, 1.8, 7.0, 0.2),
        "lettuce" to NutritionInfo(15, 1.4, 2.9, 0.2),
        "pumpkin" to NutritionInfo(26, 1.0, 6.5, 0.1),
        "pepper" to NutritionInfo(31, 1.0, 6.0, 0.3),
        "okra" to NutritionInfo(33, 1.9, 7.5, 0.2),
        "mushroom" to NutritionInfo(22, 3.1, 3.3, 0.3),
        "soup" to NutritionInfo(50, 2.0, 7.0, 1.5),
        "salad" to NutritionInfo(33, 1.5, 6.0, 0.4),
        "pie" to NutritionInfo(265, 3.5, 34.0, 12.0),
        "sauce" to NutritionInfo(80, 1.5, 10.0, 3.5),
        "gravy" to NutritionInfo(50, 2.0, 5.0, 2.0),
        "curry" to NutritionInfo(120, 4.0, 10.0, 7.0),
        "sandwich" to NutritionInfo(250, 10.0, 30.0, 9.0),
        "cake" to NutritionInfo(350, 5.0, 50.0, 15.0),
        "cookie" to NutritionInfo(480, 6.0, 68.0, 21.0),
        "biscuit" to NutritionInfo(480, 6.0, 68.0, 21.0),
        "yogurt" to NutritionInfo(61, 3.5, 4.7, 3.3),
        "milk" to NutritionInfo(61, 3.2, 4.8, 3.3),
        "coffee" to NutritionInfo(2, 0.3, 0.0, 0.0),
        "tea" to NutritionInfo(1, 0.0, 0.3, 0.0),
        "ice cream" to NutritionInfo(207, 3.5, 24.0, 11.0)
    )

    // Explicit aliases only; avoids guessing a food's identity.
    private val aliases = mapOf(
        "green onion" to "spring onion",
        "scallion" to "spring onion",
        "scallions" to "spring onion",
        "hamburger" to "hamburg",
        "burger" to "hamburg",
        "fries" to "french fries",
        "chips" to "french fries",
        "plain yogurt" to "yogurt",
        "curd" to "yogurt",
        "icecream" to "ice cream",
        "tomatoes" to "tomato",
        "potatoes" to "potato",
        "carrots" to "carrot",
        "onions" to "onion",
        "mushrooms" to "mushroom",
        "eggs" to "egg",
        "apples" to "apple",
        "bananas" to "banana",
        "biscuits" to "biscuit",
        "cookies" to "cookie"
    )

    /**
     * Returns null when no reference entry matches.
     * Callers should treat null as "nutrition unavailable", not as zero nutrition.
     */
    fun findNutrition(foodName: String): NutritionInfo? {
        val normalizedName = normalizeName(foodName)
        val canonicalName = aliases[normalizedName] ?: normalizedName
        return nutritionData[canonicalName]
    }

    /**
     * Backwards-compatible lookup for existing callers.
     * Prefer findNutrition() when you need to distinguish missing data from true zero values.
     */
    fun getNutrition(foodName: String): NutritionInfo =
        findNutrition(foodName) ?: NutritionInfo(
            calories = 0,
            protein = 0.0,
            carbohydrates = 0.0,
            fat = 0.0
        )

    fun hasNutrition(foodName: String): Boolean =
        findNutrition(foodName) != null

    private fun normalizeName(name: String): String =
        name.trim()
            .lowercase()
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(Regex("\\s+"), " ")
}
