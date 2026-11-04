package com.foodlens.ai

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodlens.ai.ui.detectedfoods.DetectedFood
import com.foodlens.ai.ui.detectedfoods.DetectedFoodsScreen
import com.foodlens.ai.ui.home.HomeScreen
import com.foodlens.ai.ui.nutrition.NutritionRepository
import com.foodlens.ai.ui.result.ResultScreen
import com.foodlens.ai.ui.scan.ScanScreen
import com.foodlens.ai.ui.theme.FoodLensAITheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale


private enum class AppScreen {
    HOME,
    SCAN,
    RESULTS,
    DETECTED_FOODS,
    HISTORY
}


class MainActivity : ComponentActivity() {

    companion object {

        init {
            System.loadLibrary("foodlens_native")
        }
    }

    // ============================================================
    // NATIVE FUNCTIONS
    // ============================================================

    private external fun stringFromJNI(): String

    private external fun analyzeBitmap(
        bitmap: Bitmap,
        assetManager: AssetManager
    ): String


    // ============================================================
    // ACTIVITY
    // ============================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContent {

            FoodLensAITheme {

                var currentScreen by remember {
                    mutableStateOf(AppScreen.HOME)
                }

                var selectedImageUri by remember {
                    mutableStateOf<Uri?>(null)
                }

                var capturedBitmap by remember {
                    mutableStateOf<Bitmap?>(null)
                }

                var analysisResult by remember {
                    mutableStateOf<String?>(null)
                }

                var shouldAnalyze by remember {
                    mutableStateOf(false)
                }

                var detectedFoods by remember {
                    mutableStateOf<List<DetectedFood>>(emptyList())
                }


                // ==================================================
                // NATIVE MODEL LOAD TEST
                // ==================================================

                LaunchedEffect(Unit) {

                    try {

                        val result =
                            withContext(Dispatchers.IO) {
                                stringFromJNI()
                            }

                        Toast.makeText(
                            this@MainActivity,
                            result,
                            Toast.LENGTH_LONG
                        ).show()

                    } catch (e: Exception) {

                        Toast.makeText(
                            this@MainActivity,
                            "NCNN Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


                // ==================================================
                // ANALYZE IMAGE
                // ==================================================

                LaunchedEffect(
                    currentScreen,
                    shouldAnalyze
                ) {

                    if (
                        currentScreen != AppScreen.RESULTS ||
                        !shouldAnalyze
                    ) {
                        return@LaunchedEffect
                    }


                    var bitmapToAnalyze =
                        capturedBitmap


                    // ==================================================
                    // LOAD GALLERY IMAGE
                    // ==================================================

                    if (
                        bitmapToAnalyze == null &&
                        selectedImageUri != null
                    ) {

                        analysisResult =
                            "LOADING IMAGE..."


                        bitmapToAnalyze =
                            withContext(Dispatchers.IO) {

                                try {

                                    val inputStream:
                                            InputStream? =
                                        contentResolver
                                            .openInputStream(
                                                selectedImageUri!!
                                            )

                                    inputStream?.use { stream ->

                                        BitmapFactory
                                            .decodeStream(stream)
                                    }

                                } catch (
                                    _: Exception
                                ) {

                                    null
                                }
                            }


                        if (
                            bitmapToAnalyze != null
                        ) {

                            capturedBitmap =
                                bitmapToAnalyze
                        }
                    }


                    // ==================================================
                    // RUN COMPLETE IMAGE ANALYSIS
                    // ==================================================

                    if (
                        bitmapToAnalyze != null
                    ) {

                        analysisResult =
                            "ANALYZING..."


                        try {

                            val bitmap =
                                bitmapToAnalyze


                            // ==================================================
                            // STEP 1
                            // FOODSEG103 NCNN
                            // ==================================================

                            val segmentationResult =
                                withContext(
                                    Dispatchers.Default
                                ) {

                                    analyzeBitmap(
                                        bitmap,
                                        assets
                                    )
                                }


                            // ==================================================
                            // STEP 2
                            // DEPTH TFLITE
                            // ==================================================

                            val depthResult =
                                withContext(
                                    Dispatchers.Default
                                ) {

                                    DepthEstimator(
                                        assets
                                    ).run(bitmap)
                                }


                            // ==================================================
                            // BUILD COMPLETE RESULT
                            // ==================================================

                            analysisResult =
                                buildString {

                                    append(
                                        segmentationResult
                                    )

                                    append("\n\n")

                                    append(
                                        "DEPTH MODEL: READY\n"
                                    )

                                    append(
                                        "DEPTH INPUT: "
                                    )

                                    append(
                                        depthResult.inputWidth
                                    )

                                    append("x")

                                    append(
                                        depthResult.inputHeight
                                    )

                                    append("\n")

                                    append(
                                        "DEPTH OUTPUT: "
                                    )

                                    append(
                                        depthResult.outputHeight
                                    )

                                    append("x")

                                    append(
                                        depthResult.outputWidth
                                    )

                                    append("\n")

                                    append(
                                        "DEPTH MIN: "
                                    )

                                    append(
                                        "%.4f".format(
                                            Locale.US,
                                            depthResult.min
                                        )
                                    )

                                    append("\n")

                                    append(
                                        "DEPTH MAX: "
                                    )

                                    append(
                                        "%.4f".format(
                                            Locale.US,
                                            depthResult.max
                                        )
                                    )

                                    append("\n")

                                    append(
                                        "DEPTH MEAN: "
                                    )

                                    append(
                                        "%.4f".format(
                                            Locale.US,
                                            depthResult.mean
                                        )
                                    )
                                }


                            // ==================================================
                            // PARSE FOOD DATA
                            // ==================================================

                            detectedFoods =
                                parseDetectedFoods(
                                    segmentationResult
                                )


                            shouldAnalyze =
                                false


                            Toast.makeText(
                                this@MainActivity,
                                "Food + depth analysis completed",
                                Toast.LENGTH_SHORT
                            ).show()

                        } catch (
                            e: Exception
                        ) {

                            analysisResult =
                                "ANALYSIS ERROR: ${e.message}"


                            detectedFoods =
                                emptyList()


                            shouldAnalyze =
                                false


                            Toast.makeText(
                                this@MainActivity,
                                analysisResult,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {

                        analysisResult =
                            "BITMAP COULD NOT BE CREATED"

                        detectedFoods =
                            emptyList()

                        shouldAnalyze =
                            false

                        Toast.makeText(
                            this@MainActivity,
                            analysisResult,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


                // ==================================================
                // NAVIGATION
                // ==================================================

                when (currentScreen) {

                    AppScreen.HOME -> {

                        HomeScreen(

                            onScanClick = {

                                currentScreen =
                                    AppScreen.SCAN
                            },

                            onHistoryClick = {

                                currentScreen =
                                    AppScreen.HISTORY
                            }
                        )
                    }


                    AppScreen.SCAN -> {

                        ScanScreen(

                            onBackClick = {

                                currentScreen =
                                    AppScreen.HOME
                            },

                            onAnalyzeClick = {
                                    uri,
                                    bitmap ->

                                selectedImageUri =
                                    uri

                                capturedBitmap =
                                    bitmap

                                analysisResult =
                                    null

                                detectedFoods =
                                    emptyList()

                                shouldAnalyze =
                                    true

                                currentScreen =
                                    AppScreen.RESULTS
                            }
                        )
                    }


                    AppScreen.RESULTS -> {

                        ResultScreen(

                            imageUri =
                                selectedImageUri,

                            capturedBitmap =
                                capturedBitmap,

                            analysisResult =
                                analysisResult,

                            onBackClick = {

                                currentScreen =
                                    AppScreen.SCAN
                            },

                            onViewDetectedFoodsClick = {

                                currentScreen =
                                    AppScreen.DETECTED_FOODS
                            }
                        )
                    }


                    AppScreen.DETECTED_FOODS -> {

                        DetectedFoodsScreen(

                            detectedFoods =
                                detectedFoods,

                            onBackClick = {

                                currentScreen =
                                    AppScreen.RESULTS
                            }
                        )
                    }


                    AppScreen.HISTORY -> {

                        FoodHistoryScreen(

                            onBackClick = {

                                currentScreen =
                                    AppScreen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}


// ============================================================
// DEPTH RESULT
// ============================================================

private data class DepthResult(

    val inputWidth: Int,

    val inputHeight: Int,

    val outputWidth: Int,

    val outputHeight: Int,

    val min: Float,

    val max: Float,

    val mean: Float,

    val values: FloatArray
)


// ============================================================
// DEPTH TFLITE ESTIMATOR
// ============================================================

private class DepthEstimator(
    private val assetManager: AssetManager
) {

    companion object {

        private const val MODEL_NAME =
            "depth_model_recovered.tflite"

        // DPF RGB/depth resolution
        private const val INPUT_HEIGHT =
            336

        private const val INPUT_WIDTH =
            448

        // Verified TFLite depth output
        private const val OUTPUT_HEIGHT =
            160

        private const val OUTPUT_WIDTH =
            224

        private const val OUTPUT_CHANNELS =
            1
    }

    private val interpreter: Interpreter


    init {

        val modelBuffer =
            loadModel()

        val options =
            Interpreter.Options().apply {
                setNumThreads(4)
            }

        interpreter =
            Interpreter(
                modelBuffer,
                options
            )

        // ========================================================
        // DEPTH MODEL INPUT
        //
        // Verified TFLite model:
        //
        // Input  : [1, 336, 448, 3]
        // Output : [1, 160, 224, 1]
        // ========================================================

        interpreter.resizeInput(
            0,
            intArrayOf(
                1,
                INPUT_HEIGHT,
                INPUT_WIDTH,
                3
            )
        )

        interpreter.allocateTensors()
        val inputTensor = interpreter.getInputTensor(0)
        val outputTensor = interpreter.getOutputTensor(0)

        Log.d(
            "DepthEstimator",
            "INPUT shape=" +
                    inputTensor.shape().contentToString() +
                    " type=" +
                    inputTensor.dataType()
        )

        Log.d(
            "DepthEstimator",
            "OUTPUT shape=" +
                    outputTensor.shape().contentToString() +
                    " type=" +
                    outputTensor.dataType()
        )
    }


    // ============================================================
    // LOAD TFLITE MODEL
    // ============================================================

    private fun loadModel(): ByteBuffer {

        val fileDescriptor =
            assetManager.openFd(
                MODEL_NAME
            )

        val inputStream =
            fileDescriptor.createInputStream()

        val fileChannel =
            inputStream.channel

        val startOffset =
            fileDescriptor.startOffset

        val declaredLength =
            fileDescriptor.declaredLength

        return fileChannel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }


    // ============================================================
    // RUN DEPTH MODEL
    // ============================================================

    fun run(
        bitmap: Bitmap
    ): DepthResult {

        // ========================================================
        // RESIZE RGB IMAGE
        //
        // TFLite model input:
        //
        // height = 336
        // width  = 448
        // channels = 3
        // ========================================================

        val inputBitmap =
            Bitmap.createScaledBitmap(
                bitmap,
                INPUT_WIDTH,
                INPUT_HEIGHT,
                true
            )


        // ========================================================
        // RGB FLOAT32 INPUT
        //
        // Verified PC TFLite preprocessing:
        //
        // RGB values are converted directly to:
        //
        // R / 255.0
        // G / 255.0
        // B / 255.0
        //
        // DO NOT apply ImageNet mean/std normalization here.
        // ========================================================

        val inputBuffer =
            ByteBuffer.allocateDirect(
                INPUT_HEIGHT *
                        INPUT_WIDTH *
                        3 *
                        4
            )

        inputBuffer.order(
            ByteOrder.nativeOrder()
        )


        val pixels =
            IntArray(
                INPUT_WIDTH *
                        INPUT_HEIGHT
            )


        inputBitmap.getPixels(
            pixels,
            0,
            INPUT_WIDTH,
            0,
            0,
            INPUT_WIDTH,
            INPUT_HEIGHT
        )


        // ========================================================
        // WRITE RGB VALUES TO TFLITE INPUT
        // ========================================================

        for (pixel in pixels) {

            val red =
                ((pixel shr 16) and 0xFF) /
                        255.0f

            val green =
                ((pixel shr 8) and 0xFF) /
                        255.0f

            val blue =
                (pixel and 0xFF) /
                        255.0f


            inputBuffer.putFloat(
                red
            )

            inputBuffer.putFloat(
                green
            )

            inputBuffer.putFloat(
                blue
            )
        }


        // ========================================================
        // DEBUG INPUT BUFFER
        // ========================================================

        val debugBuffer =
            inputBuffer.duplicate()

        debugBuffer.order(
            ByteOrder.nativeOrder()
        )

        debugBuffer.rewind()


        var debugMin =
            Float.MAX_VALUE

        var debugMax =
            -Float.MAX_VALUE

        var debugSum =
            0.0


        while (
            debugBuffer.hasRemaining()
        ) {

            val value =
                debugBuffer.float


            if (value < debugMin) {

                debugMin =
                    value
            }


            if (value > debugMax) {

                debugMax =
                    value
            }


            debugSum +=
                value
        }


        val debugCount =
            INPUT_HEIGHT *
                    INPUT_WIDTH *
                    3


        Log.d(
            "DepthEstimator",
            "Android input min=" +
                    debugMin +
                    " max=" +
                    debugMax +
                    " mean=" +
                    (
                            debugSum /
                                    debugCount
                            )
        )


        // ========================================================
        // RESET INPUT BUFFER
        // ========================================================

        inputBuffer.rewind()


        Log.d(
            "DepthEstimator",
            "Input buffer position=" +
                    inputBuffer.position() +
                    " limit=" +
                    inputBuffer.limit() +
                    " capacity=" +
                    inputBuffer.capacity()
        )


        // ========================================================
        // GET ACTUAL OUTPUT SHAPE
        // ========================================================

        val outputTensor =
            interpreter.getOutputTensor(
                0
            )


        val outputShape =
            outputTensor.shape()


        if (outputShape.size != 4) {

            throw IllegalStateException(
                "Unexpected depth output shape: " +
                        outputShape.contentToString()
            )
        }


        val outputBatch =
            outputShape[0]

        val outputHeight =
            outputShape[1]

        val outputWidth =
            outputShape[2]

        val outputChannels =
            outputShape[3]


        if (outputBatch != 1) {

            throw IllegalStateException(
                "Unexpected depth batch size: " +
                        outputBatch
            )
        }


        if (outputChannels != 1) {

            throw IllegalStateException(
                "Unexpected depth channels: " +
                        outputChannels
            )
        }


        // ========================================================
        // OUTPUT BUFFER
        //
        // Expected:
        //
        // [1, 160, 224, 1]
        // ========================================================

        val output =
            Array(outputBatch) {

                Array(outputHeight) {

                    Array(outputWidth) {

                        FloatArray(
                            outputChannels
                        )
                    }
                }
            }


        // ========================================================
        // RUN TFLITE
        // ========================================================

        val startTime =
            System.currentTimeMillis()


        interpreter.run(
            inputBuffer,
            output
        )


        val inferenceTime =
            System.currentTimeMillis() -
                    startTime


        Log.d(
            "DepthEstimator",
            "Depth inference time: " +
                    inferenceTime +
                    " ms"
        )


        // ========================================================
        // FLATTEN RAW DEPTH OUTPUT
        // ========================================================

        val rawDepth =
            FloatArray(
                outputHeight *
                        outputWidth
            )


        var index =
            0


        for (y in 0 until outputHeight) {

            for (x in 0 until outputWidth) {

                rawDepth[index++] =
                    output[0][y][x][0]
            }
        }


        // ========================================================
        // RAW DEPTH STATISTICS
        // ========================================================

        var rawMin =
            Float.POSITIVE_INFINITY

        var rawMax =
            Float.NEGATIVE_INFINITY

        var rawSum =
            0.0


        for (value in rawDepth) {

            if (value < rawMin) {

                rawMin =
                    value
            }


            if (value > rawMax) {

                rawMax =
                    value
            }


            rawSum +=
                value
        }


        val rawMean =
            if (rawDepth.isNotEmpty()) {

                (
                        rawSum /
                                rawDepth.size
                        ).toFloat()

            } else {

                0.0f
            }


        Log.d(
            "DepthEstimator",
            "Raw depth min: " +
                    rawMin
        )


        Log.d(
            "DepthEstimator",
            "Raw depth max: " +
                    rawMax
        )


        Log.d(
            "DepthEstimator",
            "Raw depth mean: " +
                    rawMean
        )


        // ========================================================
        // RESIZE DEPTH
        //
        // TFLite output:
        //
        // [160, 224]
        //
        // DPF requires:
        //
        // [336, 448]
        //
        // Bilinear interpolation.
        // ========================================================

        val resizedDepth =
            resizeDepthBilinear(
                rawDepth,
                outputHeight,
                outputWidth,
                INPUT_HEIGHT,
                INPUT_WIDTH
            )


        // ========================================================
        // EXACT PredDepthToTensor NORMALIZATION
        //
        // If max > 255:
        //
        // depth / 65535
        //
        // Otherwise:
        //
        // depth / 255
        //
        // ========================================================

        val maxValue =
            resizedDepth.maxOrNull()
                ?: 0.0f


        if (maxValue > 1.0f) {

            if (maxValue > 255.0f) {

                for (i in resizedDepth.indices) {

                    resizedDepth[i] =
                        resizedDepth[i] /
                                65535.0f
                }

            } else {

                for (i in resizedDepth.indices) {

                    resizedDepth[i] =
                        resizedDepth[i] /
                                255.0f
                }
            }
        }


        // ========================================================
        // FINAL CLIP [0,1]
        // ========================================================

        for (i in resizedDepth.indices) {

            resizedDepth[i] =
                resizedDepth[i].coerceIn(
                    0.0f,
                    1.0f
                )
        }


        // ========================================================
        // FINAL DEPTH STATISTICS
        // ========================================================

        var finalMin =
            Float.POSITIVE_INFINITY

        var finalMax =
            Float.NEGATIVE_INFINITY

        var finalSum =
            0.0


        for (value in resizedDepth) {

            if (value < finalMin) {

                finalMin =
                    value
            }


            if (value > finalMax) {

                finalMax =
                    value
            }


            finalSum +=
                value
        }


        val finalMean =
            if (resizedDepth.isNotEmpty()) {

                (
                        finalSum /
                                resizedDepth.size
                        ).toFloat()

            } else {

                0.0f
            }


        Log.d(
            "DepthEstimator",
            "Final depth size: " +
                    resizedDepth.size
        )


        Log.d(
            "DepthEstimator",
            "Final depth dimensions: " +
                    INPUT_HEIGHT +
                    "x" +
                    INPUT_WIDTH
        )


        Log.d(
            "DepthEstimator",
            "Final depth min: " +
                    finalMin
        )


        Log.d(
            "DepthEstimator",
            "Final depth max: " +
                    finalMax
        )


        Log.d(
            "DepthEstimator",
            "Final depth mean: " +
                    finalMean
        )


        // ========================================================
        // RETURN
        // ========================================================

        return DepthResult(

            inputWidth =
                INPUT_WIDTH,

            inputHeight =
                INPUT_HEIGHT,

            outputWidth =
                outputWidth,

            outputHeight =
                outputHeight,

            min =
                finalMin,

            max =
                finalMax,

            mean =
                finalMean,

            values =
                resizedDepth
        )
    }


    // ============================================================
    // BILINEAR DEPTH RESIZE
    // ============================================================

    private fun resizeDepthBilinear(
        source: FloatArray,
        sourceHeight: Int,
        sourceWidth: Int,
        targetHeight: Int,
        targetWidth: Int
    ): FloatArray {

        val result =
            FloatArray(
                targetHeight *
                        targetWidth
            )


        val xScale =
            if (targetWidth > 1) {

                (sourceWidth - 1).toFloat() /
                        (targetWidth - 1)

            } else {

                0.0f
            }


        val yScale =
            if (targetHeight > 1) {

                (sourceHeight - 1).toFloat() /
                        (targetHeight - 1)

            } else {

                0.0f
            }


        for (y in 0 until targetHeight) {

            val sourceY =
                y * yScale

            val y0 =
                sourceY.toInt()

            val y1 =
                minOf(
                    y0 + 1,
                    sourceHeight - 1
                )

            val yWeight =
                sourceY - y0


            for (x in 0 until targetWidth) {

                val sourceX =
                    x * xScale

                val x0 =
                    sourceX.toInt()

                val x1 =
                    minOf(
                        x0 + 1,
                        sourceWidth - 1
                    )

                val xWeight =
                    sourceX - x0


                val topLeft =
                    source[
                        y0 * sourceWidth +
                                x0
                    ]

                val topRight =
                    source[
                        y0 * sourceWidth +
                                x1
                    ]

                val bottomLeft =
                    source[
                        y1 * sourceWidth +
                                x0
                    ]

                val bottomRight =
                    source[
                        y1 * sourceWidth +
                                x1
                    ]


                val top =
                    topLeft +
                            (
                                    topRight -
                                            topLeft
                                    ) *
                            xWeight


                val bottom =
                    bottomLeft +
                            (
                                    bottomRight -
                                            bottomLeft
                                    ) *
                            xWeight


                val value =
                    top +
                            (
                                    bottom -
                                            top
                                    ) *
                            yWeight


                result[
                    y * targetWidth +
                            x
                ] = value
            }
        }


        return result
    }
}


// ============================================================
// PARSE DETECTED FOODS
// ============================================================

private fun parseDetectedFoods(
    result: String
): List<DetectedFood> {

    val foodsByName =
        linkedMapOf<String, DetectedFood>()


    val lines =
        result.lines()


    var readingFoods =
        false


    for (line in lines) {

        val trimmedLine =
            line.trim()


        // ========================================================
        // ENRICHED FOOD DATA
        // ========================================================

        if (
            trimmedLine.startsWith(
                "FOOD_DATA|",
                ignoreCase = true
            )
        ) {

            val parts =
                trimmedLine.split("|")


            if (
                parts.size >= 10
            ) {

                val name =
                    parts[1].trim()


                if (name.isEmpty()) {
                    continue
                }


                val confidence =
                    parts[2]
                        .trim()
                        .toFloatOrNull()
                        ?.toInt()
                        ?: 0


                val estimatedMass =
                    parts[4]
                        .trim()
                        .toDoubleOrNull()
                        ?: 0.0


                val calories =
                    parts[5]
                        .trim()
                        .toDoubleOrNull()
                        ?.toInt()
                        ?: 0


                val protein =
                    parts[6]
                        .trim()
                        .toDoubleOrNull()
                        ?: 0.0


                val carbohydrates =
                    parts[7]
                        .trim()
                        .toDoubleOrNull()
                        ?: 0.0


                val fat =
                    parts[8]
                        .trim()
                        .toDoubleOrNull()
                        ?: 0.0


                val nutritionAvailable =
                    parts[9]
                        .trim()
                        .toBooleanStrictOrNull()
                        ?: false


                val food =
                    DetectedFood(

                        name =
                            name,

                        confidence =
                            confidence,

                        calories =
                            calories,

                        protein =
                            protein,

                        carbohydrates =
                            carbohydrates,

                        fat =
                            fat,

                        estimatedMassGrams =
                            estimatedMass,

                        nutritionAvailable =
                            nutritionAvailable,

                        nutritionMethod =
                            if (
                                nutritionAvailable
                            ) {

                                "reference_per_100g_scaled_by_estimated_mass"

                            } else {

                                "no_reference_available"
                            }
                    )


                val key =
                    name.lowercase(
                        Locale.ROOT
                    )


                val existing =
                    foodsByName[key]


                if (
                    existing == null ||
                    confidence >
                    existing.confidence
                ) {

                    foodsByName[key] =
                        food
                }
            }

            continue
        }


        // ========================================================
        // FOOD LIST
        // ========================================================

        if (
            trimmedLine.equals(
                "FOODS:",
                ignoreCase = true
            )
        ) {

            readingFoods =
                true

            continue
        }


        if (!readingFoods) {
            continue
        }


        // ========================================================
        // END FOOD LIST
        // ========================================================

        if (
            trimmedLine.isEmpty() ||
            trimmedLine.startsWith(
                "SEGMENTATION:",
                ignoreCase = true
            ) ||
            trimmedLine.startsWith(
                "OUT0:",
                ignoreCase = true
            ) ||
            trimmedLine.startsWith(
                "OUT1:",
                ignoreCase = true
            ) ||
            trimmedLine.startsWith(
                "DEPTH MODEL:",
                ignoreCase = true
            )
        ) {

            break
        }


        // ========================================================
        // SIMPLE FORMAT
        // name|confidence
        // ========================================================

        val parts =
            trimmedLine.split("|")


        if (
            parts.size < 2
        ) {

            continue
        }


        val name =
            parts[0].trim()


        if (name.isEmpty()) {
            continue
        }


        val confidence =
            parts[1]
                .trim()
                .toFloatOrNull()
                ?.toInt()
                ?: 0


        val nutrition =
            NutritionRepository
                .getNutrition(
                    name
                )


        val food =
            DetectedFood(

                name =
                    name,

                confidence =
                    confidence,

                calories =
                    nutrition.calories,

                protein =
                    nutrition.protein,

                carbohydrates =
                    nutrition.carbohydrates,

                fat =
                    nutrition.fat,

                estimatedMassGrams =
                    0.0,

                nutritionAvailable =
                    hasNutritionData(
                        nutrition
                    ),

                nutritionMethod =
                    if (
                        hasNutritionData(
                            nutrition
                        )
                    ) {

                        "reference_per_100g"

                    } else {

                        "no_reference_available"
                    }
            )


        val key =
            name.lowercase(
                Locale.ROOT
            )


        val existingFood =
            foodsByName[key]


        if (
            existingFood == null ||
            confidence >
            existingFood.confidence
        ) {

            foodsByName[key] =
                food
        }
    }


    return foodsByName.values.toList()
}


// ============================================================
// NUTRITION DATA CHECK
// ============================================================

private fun hasNutritionData(
    nutrition: Any
): Boolean {

    return try {

        val calories =
            nutrition.javaClass
                .getDeclaredField("calories")
                .apply {
                    isAccessible = true
                }
                .get(nutrition) as? Number


        val protein =
            nutrition.javaClass
                .getDeclaredField("protein")
                .apply {
                    isAccessible = true
                }
                .get(nutrition) as? Number


        val carbohydrates =
            nutrition.javaClass
                .getDeclaredField("carbohydrates")
                .apply {
                    isAccessible = true
                }
                .get(nutrition) as? Number


        val fat =
            nutrition.javaClass
                .getDeclaredField("fat")
                .apply {
                    isAccessible = true
                }
                .get(nutrition) as? Number


        (
                (calories?.toDouble() ?: 0.0) != 0.0 ||
                        (protein?.toDouble() ?: 0.0) != 0.0 ||
                        (carbohydrates?.toDouble() ?: 0.0) != 0.0 ||
                        (fat?.toDouble() ?: 0.0) != 0.0
                )

    } catch (
        _: Exception
    ) {

        false
    }
}


// ============================================================
// FOOD HISTORY
// ============================================================

@androidx.compose.runtime.Composable
private fun FoodHistoryScreen(
    onBackClick: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF0D0D0D)
                )
                .padding(22.dp)
    ) {

        Spacer(
            modifier =
                Modifier.height(28.dp)
        )


        Text(
            text =
                "SCAN HISTORY",

            fontSize =
                25.sp,

            fontWeight =
                FontWeight.ExtraBold,

            letterSpacing =
                1.sp,

            color =
                Color(0xFFD4AF37)
        )


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        Text(
            text =
                "Your analyzed meals will appear here.",

            fontSize =
                14.sp,

            color =
                Color(0xFF888888)
        )


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


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
                modifier =
                    Modifier.padding(22.dp)
            ) {

                Text(
                    text =
                        "NO SCANS YET",

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color.White
                )


                Spacer(
                    modifier =
                        Modifier.height(7.dp)
                )


                Text(
                    text =
                        "Start your first food scan to build your nutrition history.",

                    fontSize =
                        13.sp,

                    lineHeight =
                        19.sp,

                    color =
                        Color(0xFF888888)
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(25.dp)
        )


        Button(
            onClick =
                onBackClick,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(54.dp),

            shape =
                RoundedCornerShape(16.dp),

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
                    "BACK TO HOME",

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}