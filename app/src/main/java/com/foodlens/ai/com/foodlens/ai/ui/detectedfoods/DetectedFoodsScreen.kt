package com.foodlens.ai.ui.detectedfoods

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodlens.ai.ui.theme.BackgroundDark
import com.foodlens.ai.ui.theme.Gold
import com.foodlens.ai.ui.theme.SurfaceDark
import com.foodlens.ai.ui.theme.SurfaceDarkElevated
import com.foodlens.ai.ui.theme.TextMuted
import com.foodlens.ai.ui.theme.TextPrimary
import com.foodlens.ai.ui.theme.TextSecondary


// ============================================================
// DETECTED FOOD DATA
// ============================================================

data class DetectedFood(
    val name: String,
    val confidence: Int,

    // ------------------------------------------------------------
    // Existing nutrition fields
    // ------------------------------------------------------------

    // NutritionRepository reference values are per 100 g unless the method says otherwise.
    val calories: Int,
    val protein: Double,
    val carbohydrates: Double,
    val fat: Double,

    // ------------------------------------------------------------
    // New dynamic portion field
    //
    // Default value keeps the existing MainActivity compatible.
    // It will be populated when the dynamic nutrition pipeline
    // is connected.
    // ------------------------------------------------------------

    val estimatedMassGrams: Double = 0.0,

    // ------------------------------------------------------------
    // Indicates whether nutrition data exists for this food.
    // ------------------------------------------------------------

    val nutritionAvailable: Boolean = true,

    // ------------------------------------------------------------
    // Indicates how the nutrition value was calculated.
    // ------------------------------------------------------------

    val nutritionMethod: String =
        "reference_per_100g"
)


// ============================================================
// DETECTED FOODS SCREEN
// ============================================================

@Composable
fun DetectedFoodsScreen(
    detectedFoods: List<DetectedFood>,
    onBackClick: () -> Unit
) {
    val servingSizes = androidx.compose.runtime.remember(detectedFoods) {
        mutableStateMapOf<String, Double>().apply {
            detectedFoods.forEach { food ->
                val key = "${food.name.lowercase()}_${food.confidence}"
                this[key] = food.estimatedMassGrams.takeIf { it > 0.0 } ?: 150.0
            }
        }
    }

    val totalCalories = detectedFoods.filter { it.nutritionAvailable }.sumOf { food ->
        val key = "${food.name.lowercase()}_${food.confidence}"
        val serving = servingSizes[key] ?: 150.0
        val baseMass = if (
            food.nutritionMethod == "reference_per_100g_scaled_by_estimated_mass" &&
            food.estimatedMassGrams > 0.0
        ) food.estimatedMassGrams else 100.0
        food.calories * (100.0 / baseMass) * (serving / 100.0)
    }
    val totalProtein = detectedFoods.filter { it.nutritionAvailable }.sumOf { food ->
        val key = "${food.name.lowercase()}_${food.confidence}"
        val serving = servingSizes[key] ?: 150.0
        val baseMass = if (
            food.nutritionMethod == "reference_per_100g_scaled_by_estimated_mass" &&
            food.estimatedMassGrams > 0.0
        ) food.estimatedMassGrams else 100.0
        food.protein * (100.0 / baseMass) * (serving / 100.0)
    }
    val totalCarbohydrates = detectedFoods.filter { it.nutritionAvailable }.sumOf { food ->
        val key = "${food.name.lowercase()}_${food.confidence}"
        val serving = servingSizes[key] ?: 150.0
        val baseMass = if (
            food.nutritionMethod == "reference_per_100g_scaled_by_estimated_mass" &&
            food.estimatedMassGrams > 0.0
        ) food.estimatedMassGrams else 100.0
        food.carbohydrates * (100.0 / baseMass) * (serving / 100.0)
    }
    val totalFat = detectedFoods.filter { it.nutritionAvailable }.sumOf { food ->
        val key = "${food.name.lowercase()}_${food.confidence}"
        val serving = servingSizes[key] ?: 150.0
        val baseMass = if (
            food.nutritionMethod == "reference_per_100g_scaled_by_estimated_mass" &&
            food.estimatedMassGrams > 0.0
        ) food.estimatedMassGrams else 100.0
        food.fat * (100.0 / baseMass) * (serving / 100.0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(20.dp)
    ) {

        // ========================================================
        // TOP BAR
        // ========================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBackClick
            ) {

                Text(
                    text = "‹  BACK",
                    color = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "FOODLENS AI",
                color = Gold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // ========================================================
        // TITLE
        // ========================================================

        Text(
            text = "Detected Foods",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "${detectedFoods.size} food items detected",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ========================================================
        // EMPTY STATE
        // ========================================================

        if (detectedFoods.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "No food items detected",
                    color = TextMuted,
                    fontSize = 16.sp
                )
            }

        } else {

            // ====================================================
            // FOOD LIST
            // ====================================================

            LazyColumn(
                modifier = Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                item {
                    MealNutritionSummary(
                        calories = totalCalories,
                        protein = totalProtein,
                        carbohydrates = totalCarbohydrates,
                        fat = totalFat,
                        foodsWithNutrition = detectedFoods.count { it.nutritionAvailable },
                        totalFoods = detectedFoods.size
                    )
                }

                item {
                    MealBalanceAndRecommendations(
                        detectedFoods = detectedFoods,
                        totalProtein = totalProtein
                    )
                }

                items(
                    items = detectedFoods,
                    key = {
                        "${it.name}_${it.confidence}"
                    }
                ) { food ->
                    val key = "${food.name.lowercase()}_${food.confidence}"
                    FoodNutritionCard(
                        food = food,
                        servingGrams = servingSizes[key] ?: 150.0,
                        onServingGramsChange = { newValue ->
                            servingSizes[key] = newValue
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// FOOD NUTRITION CARD
// ============================================================

@Composable
private fun FoodNutritionCard(
    food: DetectedFood,
    servingGrams: Double,
    onServingGramsChange: (Double) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SurfaceDark,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {

        // ========================================================
        // FOOD HEADER
        // ========================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = SurfaceDarkElevated,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "🍽",
                    fontSize = 22.sp
                )
            }

            Spacer(
                modifier = Modifier.size(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = food.name,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        "Detection confidence: ${food.confidence}%",

                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )


        // ========================================================
        // ADJUSTABLE SERVING SIZE
        // NutritionRepository values are treated as values per 100 g.
        // The displayed values are recalculated whenever servingGrams changes.
        // ========================================================

        // Serving size is owned by the parent screen so meal totals
        // update immediately when the user changes a portion.
        val sourceMassGrams =
            if (
                food.nutritionMethod ==
                "reference_per_100g_scaled_by_estimated_mass" &&
                food.estimatedMassGrams > 0.0
            ) {
                food.estimatedMassGrams
            } else {
                100.0
            }

        val scaleTo100g =
            if (sourceMassGrams > 0.0) {
                100.0 / sourceMassGrams
            } else {
                1.0
            }

        val caloriesPer100g = food.calories * scaleTo100g
        val proteinPer100g = food.protein * scaleTo100g
        val carbsPer100g = food.carbohydrates * scaleTo100g
        val fatPer100g = food.fat * scaleTo100g

        val servingFactor = servingGrams / 100.0

        val displayedCalories =
            (caloriesPer100g * servingFactor).toInt()
        val displayedProtein =
            proteinPer100g * servingFactor
        val displayedCarbohydrates =
            carbsPer100g * servingFactor
        val displayedFat =
            fatPer100g * servingFactor

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = SurfaceDarkElevated,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "SERVING SIZE",
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Adjust estimate to recalculate nutrition",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            TextButton(
                onClick = {
                    onServingGramsChange((servingGrams - 25.0).coerceAtLeast(25.0))
                }
            ) {
                Text(
                    text = "−",
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${servingGrams.toInt()} g",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    onServingGramsChange((servingGrams + 25.0).coerceAtMost(1000.0))
                }
            ) {
                Text(
                    text = "+",
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ========================================================
        // NUTRITION TITLE
        // ========================================================

        Text(
            text = "NUTRITION",
            color = Gold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // ========================================================
        // NUTRITION AVAILABLE
        // ========================================================

        if (food.nutritionAvailable) {

            NutritionRow(
                label = "Calories",
                value = "$displayedCalories kcal"
            )

            NutritionRow(
                label = "Protein",
                value =
                    formatDecimal(
                        displayedProtein
                    ) + " g"
            )

            NutritionRow(
                label = "Carbohydrates",
                value =
                    formatDecimal(
                        displayedCarbohydrates
                    ) + " g"
            )

            NutritionRow(
                label = "Fat",
                value =
                    formatDecimal(
                        displayedFat
                    ) + " g"
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            // ====================================================
            // NUTRITION METHOD
            // ====================================================

            val methodText =
                if (
                    food.nutritionMethod ==
                    "reference_per_100g_scaled_by_estimated_mass"
                ) {

                    "Scaled from reference nutrition using serving estimate"

                } else {

                    "Reference values scaled to selected serving"
                }

            Text(
                text = methodText,

                modifier =
                    Modifier.fillMaxWidth(),

                color =
                    TextMuted,

                fontSize =
                    10.sp
            )

        } else {

            // ====================================================
            // NUTRITION UNAVAILABLE
            // ====================================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color =
                            SurfaceDarkElevated,

                        shape =
                            RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {

                Text(
                    text =
                        "NUTRITION DATA NOT AVAILABLE",

                    color =
                        Gold,

                    fontSize =
                        12.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text =
                        "A nutrition reference is not currently available for this detected food.",

                    color =
                        TextSecondary,

                    fontSize =
                        12.sp,

                    lineHeight =
                        18.sp
                )
            }
        }
    }
}



// ============================================================
// MEAL NUTRITION SUMMARY
// ============================================================

@Composable
private fun MealNutritionSummary(
    calories: Double,
    protein: Double,
    carbohydrates: Double,
    fat: Double,
    foodsWithNutrition: Int,
    totalFoods: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SurfaceDark,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {
        Text(
            text = "TOTAL MEAL NUTRITION",
            color = Gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "${calories.toInt()} kcal",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        NutritionRow(label = "Protein", value = "${formatDecimal(protein)} g")
        NutritionRow(label = "Carbohydrates", value = "${formatDecimal(carbohydrates)} g")
        NutritionRow(label = "Fat", value = "${formatDecimal(fat)} g")

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Nutrition available for $foodsWithNutrition of $totalFoods detected foods",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Totals update when serving sizes change. Values are estimates based on reference nutrition data.",
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}


// ============================================================
// RESPONSIVE MEAL BALANCE SCORE AND RECOMMENDATIONS
// Prototype guidance only; not a clinically validated score.
// ============================================================

@Composable
private fun MealBalanceAndRecommendations(
    detectedFoods: List<DetectedFood>,
    totalProtein: Double
) {
    val normalizedNames = detectedFoods.map { it.name.lowercase() }.distinct()

    val produceKeywords = listOf(
        "apple", "banana", "orange", "mango", "pineapple", "watermelon",
        "lemon", "avocado", "tomato", "potato", "carrot", "broccoli",
        "cabbage", "cauliflower", "cucumber", "lettuce", "green beans",
        "pumpkin", "pepper", "okra", "mushroom", "onion", "salad",
        "spring onion", "spinach", "vegetable"
    )
    val proteinKeywords = listOf(
        "chicken", "duck", "egg", "fish", "shrimp", "steak", "pork",
        "lamb", "tofu", "peanut", "almond", "cashew", "walnut", "bean"
    )
    val processedKeywords = listOf(
        "french fries", "pizza", "hamburg", "burger", "ice cream",
        "pie", "fried", "sausage", "bacon", "processed"
    )

    val hasProduce = normalizedNames.any { name ->
        produceKeywords.any { keyword -> name.contains(keyword) }
    }
    val hasProteinFood = normalizedNames.any { name ->
        proteinKeywords.any { keyword -> name.contains(keyword) }
    }
    val processedCount = normalizedNames.count { name ->
        processedKeywords.any { keyword -> name.contains(keyword) }
    }
    val nutritionCoverage = if (detectedFoods.isEmpty()) 0.0 else {
        detectedFoods.count { it.nutritionAvailable }.toDouble() / detectedFoods.size
    }

    val varietyPoints = (normalizedNames.size * 5).coerceAtMost(25)
    val coveragePoints = (nutritionCoverage * 20).toInt()
    val producePoints = if (hasProduce) 20 else 0
    val proteinPoints = when {
        totalProtein >= 15.0 -> 20
        totalProtein >= 7.0 -> 12
        hasProteinFood -> 8
        else -> 0
    }
    val processedPenalty = (processedCount * 8).coerceAtMost(16)
    val score = (varietyPoints + coveragePoints + producePoints + proteinPoints - processedPenalty)
        .coerceIn(0, 100)

    val scoreColor = when {
        score >= 75 -> Color(0xFF35C759) // Green
        score >= 50 -> Color(0xFFFFC247) // Amber
        else -> Color(0xFFFF5A5F) // Red
    }
    val category = when {
        score >= 75 -> "Good balance indicators"
        score >= 50 -> "Moderate balance indicators"
        else -> "Needs more balance"
    }
    val shortAdvice = when {
        score >= 75 -> "A good mix of indicators was found. Keep variety and portions in mind."
        score >= 50 -> "A few balance indicators are present. Review the suggestions below."
        else -> "This scan shows limited balance indicators. Consider a wider variety of foods."
    }

    val recommendations = mutableListOf<String>()
    if (!hasProduce) {
        recommendations.add("Consider adding a fruit or vegetable to increase food-group variety.")
    }
    if (!hasProteinFood || totalProtein < 15.0) {
        recommendations.add("Consider a suitable protein source or portion, based on your dietary needs.")
    }
    if (processedCount > 0) {
        recommendations.add("Balance processed or fried items with less-processed foods when possible.")
    }
    if (nutritionCoverage < 1.0) {
        recommendations.add("Nutrition references are missing for some detected foods; those foods are excluded from the totals.")
    }
    if (recommendations.isEmpty()) {
        recommendations.add("Several food-group indicators are present. Consider your overall daily diet and portion needs.")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SurfaceDark,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = scoreColor.copy(alpha = 0.55f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "MEAL BALANCE",
            color = Gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Score and category share flexible width to avoid clipping on small screens.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .background(
                        color = scoreColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$score",
                    color = scoreColor,
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    text = "OUT OF 100",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Column(
                modifier = Modifier.weight(1.6f)
            ) {
                Text(
                    text = category,
                    color = scoreColor,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = scoreColor,
                    trackColor = SurfaceDarkElevated
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = shortAdvice,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "PERSONALIZED SUGGESTIONS",
            color = Gold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(9.dp))

        recommendations.forEach { recommendation ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "•",
                    color = scoreColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = recommendation,
                    modifier = Modifier.weight(1f),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Prototype indicator based on detected food names, reference-data coverage, and estimated protein. It is not a medical assessment or a clinically validated nutrition score.",
            color = TextMuted,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
    }
}


// ============================================================
// NUTRITION ROW
// ============================================================

@Composable
private fun NutritionRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = label,

            color =
                TextSecondary,

            fontSize =
                14.sp,

            modifier =
                Modifier.weight(1f)
        )

        Text(
            text = value,

            color =
                TextPrimary,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.SemiBold
        )
    }
}


// ============================================================
// FORMAT MASS
// ============================================================

private fun formatMass(
    mass: Double
): String {

    return if (mass >= 100.0) {

        String.format(
            "%.1f g",
            mass
        )

    } else {

        String.format(
            "%.2f g",
            mass
        )
    }
}


// ============================================================
// FORMAT DECIMAL
// ============================================================

private fun formatDecimal(
    value: Double
): String {

    return String.format(
        "%.1f",
        value
    )
}