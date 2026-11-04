package com.foodlens.ai.ui.scan

import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ScanScreen(
    onBackClick: () -> Unit = {},
    onAnalyzeClick: (Uri?, Bitmap?) -> Unit = { _, _ -> }
) {

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var capturedBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->

        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
        }
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                cameraLauncher.launch(null)
            }
        }

    BackHandler {
        onBackClick()
    }

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
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E1E1E),
                        contentColor = Color(0xFFD4AF37)
                    )
                ) {

                    Text(
                        text = "‹",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.size(14.dp)
                )

                Column {

                    Text(
                        text = "SCAN FOOD",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color(0xFFD4AF37)
                    )

                    Text(
                        text = "Capture or select your meal",
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
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
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF29210F),
                                    Color(0xFF171717)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    when {

                        capturedBitmap != null -> {

                            AndroidView(
                                factory = { context ->

                                    ImageView(context).apply {
                                        scaleType =
                                            ImageView.ScaleType.CENTER_CROP
                                    }
                                },
                                update = { imageView ->

                                    imageView.setImageBitmap(
                                        capturedBitmap
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        selectedImageUri != null -> {

                            AndroidView(
                                factory = { context ->

                                    ImageView(context).apply {
                                        scaleType =
                                            ImageView.ScaleType.CENTER_CROP
                                    }
                                },
                                update = { imageView ->

                                    imageView.setImageURI(
                                        selectedImageUri
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {

                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .background(
                                            Color(0xFFD4AF37),
                                            CircleShape
                                        ),
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Text(
                                        text = "⌾",
                                        fontSize = 40.sp,
                                        color = Color(0xFF0D0D0D)
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(16.dp)
                                )

                                Text(
                                    text = "Select your meal",
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(
                                    modifier = Modifier.height(7.dp)
                                )

                                Text(
                                    text =
                                        "Capture a photo or choose an image\nfrom your gallery.",
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            if (selectedImageUri != null || capturedBitmap != null) {

                Button(
                    onClick = {
                        onAnalyzeClick(
                            selectedImageUri,
                            capturedBitmap
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color(0xFF0D0D0D)
                    )
                ) {

                    Text(
                        text = "✦   ANALYZE FOOD",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(14.dp)
                )
            }

            Button(
                onClick = {
                    cameraPermissionLauncher.launch(
                        android.Manifest.permission.CAMERA
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (selectedImageUri == null &&
                            capturedBitmap == null
                        ) {
                            Color(0xFFD4AF37)
                        } else {
                            Color(0xFF1E1E1E)
                        },
                    contentColor =
                        if (selectedImageUri == null &&
                            capturedBitmap == null
                        ) {
                            Color(0xFF0D0D0D)
                        } else {
                            Color(0xFFD4AF37)
                        }
                )
            ) {

                Text(
                    text = "◉   OPEN CAMERA",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Button(
                onClick = {
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color(0xFFD4AF37)
                )
            ) {

                Text(
                    text = "□   CHOOSE FROM GALLERY",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            Text(
                text = "ANALYSIS PIPELINE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color(0xFFD4AF37)
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text =
                    "Detection  •  Classification  •  Segmentation\n" +
                            "Portion Estimation  •  Nutrition",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color(0xFF777777)
            )
        }
    }
}