package com.foodlens.ai.ui.result

import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView


// ============================================================
// DETECTED FOOD DATA
// ============================================================

data class DetectedFood(
    val name: String,
    val confidence: String
)


// ============================================================
// RESULT SCREEN
// ============================================================

@Composable
fun ResultScreen(
    imageUri: Uri?,
    capturedBitmap: Bitmap?,
    analysisResult: String?,
    onBackClick: () -> Unit,
    onViewDetectedFoodsClick: () -> Unit
) {

    BackHandler {
        onBackClick()
    }

    val resultText =
        analysisResult ?: "ANALYZING..."

    val isAnalyzing =
        resultText == "ANALYZING..." ||
                resultText == "LOADING IMAGE..."

    val isSuccess =
        resultText.contains(
            "FOODSEG INFERENCE SUCCESS",
            ignoreCase = true
        )

    val detectedFoods =
        if (isSuccess) {
            extractDetectedFoods(resultText)
        } else {
            emptyList()
        }

    val segmentation =
        extractResultValue(
            resultText,
            "SEGMENTATION:"
        )


    // ============================================================
    // MAIN BACKGROUND
    // ============================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D),
                        Color(0xFF151515),
                        Color(0xFF0D0D0D)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
        ) {

            Spacer(
                modifier = Modifier.height(28.dp)
            )


            // ====================================================
            // TITLE
            // ====================================================

            Text(
                text = "FOOD ANALYSIS",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                color = Color(0xFFD4AF37)
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Text(
                text = "AI-powered food detection",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color(0xFF888888)
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // ====================================================
            // IMAGE PREVIEW
            // ====================================================

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),

                shape = RoundedCornerShape(25.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF171717)
                ),

                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    when {

                        // ----------------------------------------
                        // CAPTURED CAMERA IMAGE
                        // ----------------------------------------

                        capturedBitmap != null -> {

                            AndroidView(
                                factory = { context ->

                                    ImageView(context).apply {

                                        scaleType =
                                            ImageView.ScaleType.CENTER_CROP

                                        setImageBitmap(
                                            capturedBitmap
                                        )
                                    }
                                },

                                modifier =
                                    Modifier.fillMaxSize()
                            )
                        }


                        // ----------------------------------------
                        // GALLERY IMAGE
                        // ----------------------------------------

                        imageUri != null -> {

                            AndroidView(
                                factory = { context ->

                                    ImageView(context).apply {

                                        scaleType =
                                            ImageView.ScaleType.CENTER_CROP

                                        setImageURI(
                                            imageUri
                                        )
                                    }
                                },

                                modifier =
                                    Modifier.fillMaxSize()
                            )
                        }


                        // ----------------------------------------
                        // NO IMAGE
                        // ----------------------------------------

                        else -> {

                            Text(
                                text = "No image available",
                                color = Color(0xFF888888)
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(15.dp)
            )


            // ====================================================
            // ANALYZING STATE
            // ====================================================

            if (isAnalyzing) {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color(0xFF171717)
                        )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(25.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator(
                            color =
                                Color(0xFFD4AF37),

                            strokeWidth =
                                3.dp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(15.dp)
                        )

                        Text(
                            text =
                                resultText,

                            fontSize =
                                15.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color(0xFFD4AF37)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(
                            text =
                                "FoodLens AI is processing the selected meal...",

                            modifier =
                                Modifier.fillMaxWidth(),

                            textAlign =
                                TextAlign.Center,

                            fontSize =
                                13.sp,

                            lineHeight =
                                20.sp,

                            color =
                                Color(0xFF999999)
                        )
                    }
                }


            } else if (isSuccess) {


                // ====================================================
                // ANALYSIS COMPLETE
                // ====================================================

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color(0xFF171717)
                        )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    text =
                                        "ANALYSIS COMPLETE",

                                    fontSize =
                                        14.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    letterSpacing =
                                        1.2.sp,

                                    color =
                                        Color(0xFFD4AF37)
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )

                                Text(
                                    text =
                                        "${detectedFoods.size} food items detected",

                                    fontSize =
                                        19.sp,

                                    fontWeight =
                                        FontWeight.ExtraBold,

                                    color =
                                        Color.White
                                )
                            }

                            Text(
                                text = "✓",

                                fontSize = 28.sp,

                                fontWeight =
                                    FontWeight.ExtraBold,

                                color =
                                    Color(0xFF4CAF50)
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )


                        // ====================================================
                        // SEGMENTATION STATUS
                        // ====================================================

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(
                                text =
                                    "Segmentation",

                                fontSize =
                                    13.sp,

                                color =
                                    Color(0xFF888888)
                            )

                            Text(
                                text =
                                    segmentation.ifEmpty {
                                        "READY"
                                    },

                                fontSize =
                                    13.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                // ====================================================
                // VIEW DETECTED FOODS
                // ====================================================

                Button(
                    onClick = {
                        onViewDetectedFoodsClick()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    shape =
                        RoundedCornerShape(17.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFFD4AF37),

                            contentColor =
                                Color(0xFF0D0D0D)
                        )
                ) {

                    Text(
                        text =
                            "⌄   VIEW DETECTED FOODS",

                        fontSize =
                            15.sp,

                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }


            } else {


                // ====================================================
                // ERROR STATE
                // ====================================================

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color(0xFF171717)
                        )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {

                        Text(
                            text =
                                "ANALYSIS ERROR",

                            fontSize =
                                14.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.3.sp,

                            color =
                                Color(0xFFEF5350)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(
                            text =
                                resultText,

                            modifier =
                                Modifier.fillMaxWidth(),

                            fontSize =
                                14.sp,

                            lineHeight =
                                21.sp,

                            color =
                                Color(0xFFB0B0B0)
                        )
                    }
                }
            }


            // ====================================================
            // PUSH BUTTONS TO BOTTOM
            // ====================================================

            Spacer(
                modifier =
                    Modifier.weight(1f)
            )


            // ====================================================
            // BACK TO SCAN
            // ====================================================

            Button(
                onClick =
                    onBackClick,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape =
                    RoundedCornerShape(16.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF1E1E1E),

                        contentColor =
                            Color(0xFFD4AF37)
                    )
            ) {

                Text(
                    text =
                        "‹   BACK TO SCAN",

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )


            // ====================================================
            // FOOTER
            // ====================================================

            Text(
                text =
                    "FoodLens AI • NCNN FoodSeg",

                modifier =
                    Modifier.fillMaxWidth(),

                textAlign =
                    TextAlign.Center,

                fontSize =
                    11.sp,

                color =
                    Color(0xFF555555)
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )
        }
    }
}


// ============================================================
// EXTRACT DETECTED FOODS
// ============================================================

private fun extractDetectedFoods(
    result: String
): List<DetectedFood> {

    val foodsByName =
        linkedMapOf<String, DetectedFood>()

    val lines =
        result.lines()

    val foodsIndex =
        lines.indexOfFirst {

            it.trim().equals(
                "FOODS:",
                ignoreCase = true
            )
        }

    if (foodsIndex == -1) {
        return emptyList()
    }

    for (
    index in foodsIndex + 1 until lines.size
    ) {

        val line =
            lines[index].trim()

        if (line.isEmpty()) {
            continue
        }

        if (
            line.startsWith(
                "SEGMENTATION:",
                ignoreCase = true
            ) ||
            line.startsWith(
                "OUT0:",
                ignoreCase = true
            ) ||
            line.startsWith(
                "OUT1:",
                ignoreCase = true
            )
        ) {
            break
        }

        val parts =
            line.split("|")

        if (parts.size >= 2) {

            val name =
                parts[0].trim()

            val confidence =
                parts[1].trim()

            if (name.isEmpty()) {
                continue
            }

            val confidenceValue =
                confidence
                    .toFloatOrNull()
                    ?: 0f

            val food =
                DetectedFood(
                    name =
                        name,

                    confidence =
                        confidence
                )

            val key =
                name.lowercase()

            val existingFood =
                foodsByName[key]

            // ------------------------------------------------
            // Keep highest-confidence detection
            // ------------------------------------------------

            if (
                existingFood == null ||
                confidenceValue >
                (
                        existingFood
                            .confidence
                            .toFloatOrNull()
                            ?: 0f
                        )
            ) {

                foodsByName[key] =
                    food
            }
        }
    }

    return foodsByName.values.toList()
}


// ============================================================
// EXTRACT RESULT VALUE
// ============================================================

private fun extractResultValue(
    result: String,
    key: String
): String {

    val line =
        result
            .lines()
            .firstOrNull {

                it.trim()
                    .startsWith(
                        key,
                        ignoreCase = true
                    )
            }

    return line
        ?.substringAfter(
            ":",
            ""
        )
        ?.trim()
        ?: ""
}