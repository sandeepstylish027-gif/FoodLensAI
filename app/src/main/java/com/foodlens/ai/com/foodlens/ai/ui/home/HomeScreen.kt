package com.foodlens.ai.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onScanClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {

    var selectedItem by remember {
        mutableIntStateOf(0)
    }

    val projectPoints = listOf(
        "Detect multiple food items from a single meal image.",
        "Segment detected food regions for more detailed analysis.",
        "Estimate food portions from image information.",
        "Calculate calories, protein, carbohydrates and fat.",
        "Generate a simple health score for the analyzed meal.",
        "Provide smart nutrition insights and recommendations.",

    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D),
                        Color(0xFF111111),
                        Color(0xFF0D0D0D)
                    )
                )
            )
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 82.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                ) {

                    Text(
                        text = "FOODLENS AI",
                        fontSize = 29.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFFD4AF37)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "AI-powered food analysis",
                        fontSize = 14.sp,
                        color = Color(0xFFB8B8B8)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF171717)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF29210F),
                                        Color(0xFF171717)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {

                        Column {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(
                                            Color(0xFFD4AF37),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "◈",
                                        fontSize = 27.sp,
                                        color = Color(0xFF0D0D0D)
                                    )
                                }

                                Spacer(modifier = Modifier.size(14.dp))

                                Column {

                                    Text(
                                        text = "Analyze your food",
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "One scan. Complete insight.",
                                        fontSize = 13.sp,
                                        color = Color(0xFFD4AF37)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Capture or upload a meal image and FoodLens AI analyzes the food using a deep learning pipeline.",
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                color = Color(0xFFB8B8B8)
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            Button(
                                onClick = onScanClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD4AF37),
                                    contentColor = Color(0xFF0D0D0D)
                                )
                            ) {
                                Text(
                                    text = "⌾   SCAN FOOD",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SectionTitle("FOODLENS AI CAPABILITIES")
            }

            item {
                FeatureCard(
                    icon = "◉",
                    title = "Food Detection",
                    description = "Detect multiple food items from a single meal image."
                )
            }

            item {
                FeatureCard(
                    icon = "◈",
                    title = "Food Classification",
                    description = "Identify food categories using deep learning models."
                )
            }

            item {
                FeatureCard(
                    icon = "✦",
                    title = "Food Segmentation",
                    description = "Separate food regions for detailed image analysis."
                )
            }

            item {
                FeatureCard(
                    icon = "◎",
                    title = "Portion Estimation",
                    description = "Estimate the quantity of detected food from visual information."
                )
            }

            item {
                FeatureCard(
                    icon = "◆",
                    title = "Nutrition Analysis",
                    description = "Analyze calories, protein, carbohydrates, fat and other nutrition values."
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))

                SectionTitle("HOW FOODLENS AI WORKS")
            }

            item {
                PipelineCard()
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))

                SectionTitle("PROJECT HIGHLIGHTS")
            }

            items(projectPoints) { point ->

                ProjectPoint(
                    text = point
                )
            }

            item {
                Spacer(modifier = Modifier.height(15.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF171717)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "YOUR FOOD JOURNEY",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color(0xFFD4AF37)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Scan your meal → Understand your nutrition → Make informed food choices.",
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))
            }
        }

        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            containerColor = Color(0xFF151515)
        ) {

            NavigationBarItem(
                selected = selectedItem == 0,
                onClick = {
                    selectedItem = 0
                },
                icon = {
                    Text(
                        text = "⌂",
                        fontSize = 22.sp,
                        color = if (selectedItem == 0) {
                            Color(0xFFD4AF37)
                        } else {
                            Color(0xFF777777)
                        }
                    )
                },
                label = {
                    Text(
                        text = "Home"
                    )
                }
            )

            NavigationBarItem(
                selected = selectedItem == 1,
                onClick = {
                    selectedItem = 1
                    onScanClick()
                },
                icon = {
                    Text(
                        text = "⌾",
                        fontSize = 22.sp
                    )
                },
                label = {
                    Text(
                        text = "Scan"
                    )
                }
            )

            NavigationBarItem(
                selected = selectedItem == 2,
                onClick = {
                    selectedItem = 2
                    onHistoryClick()
                },
                icon = {
                    Text(
                        text = "◷",
                        fontSize = 22.sp
                    )
                },
                label = {
                    Text(
                        text = "History"
                    )
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 22.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        color = Color(0xFFD4AF37)
    )
}

@Composable
private fun FeatureCard(
    icon: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF171717)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        Color(0xFF29210F),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp,
                    color = Color(0xFFD4AF37)
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
private fun PipelineCard() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF171717)
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            PipelineStep("01", "Food Image")
            PipelineStep("02", "Detection")
            PipelineStep("03", "Classification")
            PipelineStep("04", "Segmentation")
            PipelineStep("05", "Portion Estimation")
            PipelineStep("06", "Nutrition & Health Insights")
        }
    }
}

@Composable
private fun PipelineStep(
    number: String,
    title: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(31.dp)
                .background(
                    Color(0xFF29210F),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD4AF37)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            color = Color(0xFFD0D0D0)
        )
    }
}

@Composable
private fun ProjectPoint(
    text: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 22.dp,
                vertical = 4.dp
            ),
        verticalAlignment = Alignment.Top
    ) {

        Text(
            text = "✦",
            fontSize = 13.sp,
            color = Color(0xFFD4AF37)
        )

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = Color(0xFFAAAAAA)
        )
    }
}