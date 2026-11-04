#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/bitmap.h>

#include <ncnn/net.h>

#include <algorithm>
#include <cmath>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>

#define LOG_TAG "FoodLensAI"

#define LOGI(...) \
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#define LOGE(...) \
    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


// ============================================================
// FOODSEG - EXACT 103 CLASS MAPPING
// ============================================================

static const char* FOOD_NAMES[103] = {

        "French beans",          // 0
        "almond",                // 1
        "apple",                 // 2
        "apricot",               // 3
        "asparagus",             // 4
        "avocado",               // 5
        "bamboo shoots",         // 6
        "banana",                // 7
        "bean sprouts",          // 8
        "biscuit",               // 9
        "blueberry",             // 10
        "bread",                 // 11
        "broccoli",              // 12
        "cabbage",               // 13
        "cake",                  // 14
        "candy",                 // 15
        "carrot",                // 16
        "cashew",                // 17
        "cauliflower",           // 18
        "celery stick",          // 19
        "cheese butter",         // 20
        "cherry",                // 21
        "chicken duck",          // 22
        "chocolate",             // 23
        "cilantro mint",         // 24
        "coffee",                // 25
        "corn",                  // 26
        "crab",                  // 27
        "cucumber",              // 28
        "date",                  // 29
        "dried cranberries",     // 30
        "egg",                   // 31
        "egg tart",              // 32
        "eggplant",              // 33
        "enoki mushroom",        // 34
        "fig",                   // 35
        "fish",                  // 36
        "french fries",          // 37
        "fried meat",            // 38
        "garlic",                // 39
        "ginger",                // 40
        "grape",                 // 41
        "green beans",           // 42
        "hamburg",               // 43
        "hanamaki baozi",        // 44
        "ice cream",             // 45
        "juice",                 // 46
        "kelp",                  // 47
        "king oyster mushroom",  // 48
        "kiwi",                  // 49
        "lamb",                  // 50
        "lemon",                 // 51
        "lettuce",               // 52
        "mango",                 // 53
        "melon",                 // 54
        "milk",                  // 55
        "milkshake",             // 56
        "noodles",               // 57
        "okra",                  // 58
        "olives",                // 59
        "onion",                 // 60
        "orange",                // 61
        "other ingredients",     // 62
        "oyster mushroom",       // 63
        "pasta",                 // 64
        "peach",                 // 65
        "peanut",                // 66
        "pear",                  // 67
        "pepper",                // 68
        "pie",                   // 69
        "pineapple",             // 70
        "pizza",                 // 71
        "popcorn",               // 72
        "pork",                  // 73
        "potato",                // 74
        "pudding",               // 75
        "pumpkin",               // 76
        "rape",                  // 77
        "raspberry",             // 78
        "red beans",             // 79
        "rice",                  // 80
        "salad",                 // 81
        "sauce",                 // 82
        "sausage",               // 83
        "seaweed",               // 84
        "shellfish",             // 85
        "shiitake",              // 86
        "shrimp",                // 87
        "snow peas",             // 88
        "soup",                  // 89
        "soy",                   // 90
        "spring onion",          // 91
        "steak",                 // 92
        "strawberry",            // 93
        "tea",                   // 94
        "tofu",                  // 95
        "tomato",                // 96
        "walnut",                // 97
        "watermelon",            // 98
        "white button mushroom", // 99
        "white radish",          // 100
        "wine",                  // 101
        "wonton dumplings"       // 102
};


// ============================================================
// DETECTION STRUCTURE
// ============================================================

struct Detection {

    int classId;

    float confidence;

    float x1;
    float y1;
    float x2;
    float y2;
};


// ============================================================
// SETTINGS
// ============================================================

static constexpr int FOOD_CLASS_COUNT = 103;

static constexpr float CONFIDENCE_THRESHOLD = 0.25f;

static constexpr float NMS_IOU_THRESHOLD = 0.70f;

static constexpr int MAX_DETECTIONS = 10;


// ============================================================
// IOU
// ============================================================

static float calculateIoU(
        const Detection& a,
        const Detection& b) {

    float intersectionX1 =
            std::max(a.x1, b.x1);

    float intersectionY1 =
            std::max(a.y1, b.y1);

    float intersectionX2 =
            std::min(a.x2, b.x2);

    float intersectionY2 =
            std::min(a.y2, b.y2);

    float intersectionWidth =
            std::max(
                    0.0f,
                    intersectionX2 - intersectionX1
            );

    float intersectionHeight =
            std::max(
                    0.0f,
                    intersectionY2 - intersectionY1
            );

    float intersectionArea =
            intersectionWidth *
            intersectionHeight;

    float areaA =
            std::max(
                    0.0f,
                    a.x2 - a.x1
            ) *
            std::max(
                    0.0f,
                    a.y2 - a.y1
            );

    float areaB =
            std::max(
                    0.0f,
                    b.x2 - b.x1
            ) *
            std::max(
                    0.0f,
                    b.y2 - b.y1
            );

    float unionArea =
            areaA +
            areaB -
            intersectionArea;

    if (unionArea <= 0.0f) {
        return 0.0f;
    }

    return intersectionArea / unionArea;
}


// ============================================================
// NON-MAXIMUM SUPPRESSION
// ============================================================

static std::vector<Detection> applyNMS(
        std::vector<Detection> detections) {

    std::sort(
            detections.begin(),
            detections.end(),
            [](const Detection& a,
               const Detection& b) {

                return a.confidence >
                       b.confidence;
            }
    );

    std::vector<Detection> selected;

    for (const Detection& candidate : detections) {

        bool suppressed = false;

        for (const Detection& existing : selected) {

            // Class-aware NMS.
            if (candidate.classId !=
                existing.classId) {

                continue;
            }

            float iou =
                    calculateIoU(
                            candidate,
                            existing
                    );

            if (iou >= NMS_IOU_THRESHOLD) {

                suppressed = true;

                break;
            }
        }

        if (!suppressed) {

            selected.push_back(
                    candidate
            );
        }

        if (selected.size() >=
            MAX_DETECTIONS) {

            break;
        }
    }

    return selected;
}


// ============================================================
// LOAD MODEL
// ============================================================

static bool loadFoodSegModel(
        AAssetManager* mgr,
        ncnn::Net& net) {

    if (mgr == nullptr) {

        LOGE("AssetManager is null");

        return false;
    }

    net.opt.use_vulkan_compute = false;

    net.opt.num_threads = 4;

    int paramResult =
            net.load_param(
                    mgr,
                    "model.ncnn.param"
            );

    if (paramResult != 0) {

        LOGE(
                "PARAM load failed: %d",
                paramResult
        );

        return false;
    }

    LOGI("FOODSEG PARAM loaded");

    int modelResult =
            net.load_model(
                    mgr,
                    "model.ncnn.bin"
            );

    if (modelResult != 0) {

        LOGE(
                "BIN load failed: %d",
                modelResult
        );

        return false;
    }

    LOGI("FOODSEG BIN loaded");

    return true;
}


// ============================================================
// MODEL TEST
// ============================================================

extern "C"
JNIEXPORT jstring JNICALL
Java_com_foodlens_ai_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject thiz) {

    LOGI("========================================");
    LOGI("FOODLENS AI - NCNN MODEL TEST");
    LOGI("========================================");

    jclass activityClass =
            env->GetObjectClass(thiz);

    if (activityClass == nullptr) {

        return env->NewStringUTF(
                "ACTIVITY CLASS FAILED"
        );
    }

    jmethodID getAssetsMethod =
            env->GetMethodID(
                    activityClass,
                    "getAssets",
                    "()Landroid/content/res/AssetManager;"
            );

    if (getAssetsMethod == nullptr) {

        env->DeleteLocalRef(
                activityClass
        );

        return env->NewStringUTF(
                "ASSET MANAGER FAILED"
        );
    }

    jobject assetManagerObject =
            env->CallObjectMethod(
                    thiz,
                    getAssetsMethod
            );

    if (assetManagerObject == nullptr) {

        env->DeleteLocalRef(
                activityClass
        );

        return env->NewStringUTF(
                "ASSET MANAGER NULL"
        );
    }

    AAssetManager* assetManager =
            AAssetManager_fromJava(
                    env,
                    assetManagerObject
            );

    if (assetManager == nullptr) {

        env->DeleteLocalRef(
                assetManagerObject
        );

        env->DeleteLocalRef(
                activityClass
        );

        return env->NewStringUTF(
                "ASSET MANAGER CONVERSION FAILED"
        );
    }

    LOGI("AssetManager ready");

    ncnn::Net net;

    if (!loadFoodSegModel(
            assetManager,
            net)) {

        env->DeleteLocalRef(
                assetManagerObject
        );

        env->DeleteLocalRef(
                activityClass
        );

        return env->NewStringUTF(
                "FOODSEG MODEL LOAD FAILED"
        );
    }

    LOGI("========================================");
    LOGI("FOODSEG MODEL LOADED");
    LOGI("========================================");

    env->DeleteLocalRef(
            assetManagerObject
    );

    env->DeleteLocalRef(
            activityClass
    );

    return env->NewStringUTF(
            "FOODSEG MODEL LOADED"
    );
}


// ============================================================
// BITMAP ANALYSIS
// ============================================================

extern "C"
JNIEXPORT jstring JNICALL
Java_com_foodlens_ai_MainActivity_analyzeBitmap(
        JNIEnv* env,
        jobject /* thiz */,
        jobject bitmapObject,
        jobject asset_manager) {

    LOGI("========================================");
    LOGI("FOODLENS AI - ANALYZE BITMAP");
    LOGI("========================================");

    // --------------------------------------------------------
    // 1. Check bitmap
    // --------------------------------------------------------

    if (bitmapObject == nullptr) {

        LOGE("Bitmap is null");

        return env->NewStringUTF(
                "BITMAP IS NULL"
        );
    }

    // --------------------------------------------------------
    // 2. Check AssetManager
    // --------------------------------------------------------

    if (asset_manager == nullptr) {

        LOGE(
                "AssetManager object is null"
        );

        return env->NewStringUTF(
                "ASSET MANAGER IS NULL"
        );
    }

    // --------------------------------------------------------
    // 3. Convert AssetManager
    // --------------------------------------------------------

    AAssetManager* mgr =
            AAssetManager_fromJava(
                    env,
                    asset_manager
            );

    if (mgr == nullptr) {

        LOGE(
                "AssetManager conversion failed"
        );

        return env->NewStringUTF(
                "ASSET MANAGER CONVERSION FAILED"
        );
    }

    LOGI("AssetManager ready");

    // --------------------------------------------------------
    // 4. Load model
    // --------------------------------------------------------

    ncnn::Net net;

    if (!loadFoodSegModel(
            mgr,
            net)) {

        return env->NewStringUTF(
                "FOODSEG MODEL LOAD FAILED"
        );
    }

    // --------------------------------------------------------
    // 5. Bitmap information
    // --------------------------------------------------------

    AndroidBitmapInfo bitmapInfo;

    int infoResult =
            AndroidBitmap_getInfo(
                    env,
                    bitmapObject,
                    &bitmapInfo
            );

    if (infoResult !=
        ANDROID_BITMAP_RESULT_SUCCESS) {

        return env->NewStringUTF(
                "BITMAP INFO FAILED"
        );
    }

    LOGI(
            "Bitmap size: %d x %d",
            bitmapInfo.width,
            bitmapInfo.height
    );

    // --------------------------------------------------------
    // 6. Check format
    // --------------------------------------------------------

    if (bitmapInfo.format !=
        ANDROID_BITMAP_FORMAT_RGBA_8888) {

        LOGE(
                "Unsupported bitmap format: %d",
                bitmapInfo.format
        );

        return env->NewStringUTF(
                "BITMAP FORMAT UNSUPPORTED"
        );
    }

    // --------------------------------------------------------
    // 7. Lock pixels
    // --------------------------------------------------------

    void* pixels = nullptr;

    int lockResult =
            AndroidBitmap_lockPixels(
                    env,
                    bitmapObject,
                    &pixels
            );

    if (lockResult !=
        ANDROID_BITMAP_RESULT_SUCCESS) {

        return env->NewStringUTF(
                "BITMAP LOCK FAILED"
        );
    }

    if (pixels == nullptr) {

        AndroidBitmap_unlockPixels(
                env,
                bitmapObject
        );

        return env->NewStringUTF(
                "BITMAP PIXELS NULL"
        );
    }

    LOGI("Bitmap pixels locked");

    // --------------------------------------------------------
    // 8. Bitmap → NCNN Mat
    // --------------------------------------------------------

    ncnn::Mat input =
            ncnn::Mat::from_pixels_resize(
                    static_cast<
                            const unsigned char*
                            >(pixels),

                    ncnn::Mat::PIXEL_RGBA2RGB,

                    bitmapInfo.width,
                    bitmapInfo.height,

                    640,
                    640
            );

    AndroidBitmap_unlockPixels(
            env,
            bitmapObject
    );

    LOGI("Bitmap pixels unlocked");

    if (input.empty()) {

        return env->NewStringUTF(
                "NCNN INPUT FAILED"
        );
    }

    LOGI(
            "NCNN input created: %d x %d",
            input.w,
            input.h
    );

    // --------------------------------------------------------
    // 9. Normalize
    // --------------------------------------------------------

    const float meanVals[3] = {
            0.0f,
            0.0f,
            0.0f
    };

    const float normVals[3] = {
            1.0f / 255.0f,
            1.0f / 255.0f,
            1.0f / 255.0f
    };

    input.substract_mean_normalize(
            meanVals,
            normVals
    );

    LOGI("Image normalized");

    // --------------------------------------------------------
    // 10. Extractor
    // --------------------------------------------------------

    ncnn::Extractor extractor =
            net.create_extractor();

    extractor.set_light_mode(true);

    // --------------------------------------------------------
    // 11. Model input
    // --------------------------------------------------------

    int inputResult =
            extractor.input(
                    "in0",
                    input
            );

    if (inputResult != 0) {

        LOGE(
                "Model input failed: %d",
                inputResult
        );

        return env->NewStringUTF(
                "MODEL INPUT FAILED"
        );
    }

    LOGI(
            "Image successfully sent to input: in0"
    );

    // --------------------------------------------------------
    // 12. Detection output
    // --------------------------------------------------------

    ncnn::Mat out0;

    int outputResult =
            extractor.extract(
                    "out0",
                    out0
            );

    if (outputResult != 0) {

        LOGE(
                "out0 extraction failed: %d",
                outputResult
        );

        return env->NewStringUTF(
                "OUT0 EXTRACTION FAILED"
        );
    }

    LOGI(
            "out0 dimensions: w=%d h=%d c=%d",
            out0.w,
            out0.h,
            out0.c
    );

    // --------------------------------------------------------
    // 13. Segmentation output
    // --------------------------------------------------------

    ncnn::Mat out1;

    int outputResult1 =
            extractor.extract(
                    "out1",
                    out1
            );

    if (outputResult1 != 0) {

        return env->NewStringUTF(
                "OUT1 EXTRACTION FAILED"
        );
    }

    LOGI(
            "out1 dimensions: w=%d h=%d c=%d",
            out1.w,
            out1.h,
            out1.c
    );

    // ========================================================
    // 14. Validate tensor shape
    // ========================================================

    if (out0.w != 8400 ||
        out0.h != 139) {

        LOGE(
                "Unexpected out0 shape"
        );

        return env->NewStringUTF(
                "INVALID OUT0 SHAPE"
        );
    }

    /*
     * out0:
     *
     * 4   box values
     * 103 class scores
     * 32  mask coefficients
     *
     * Total:
     *
     * 4 + 103 + 32 = 139
     */

    const int classStart = 4;

    const int maskStart =
            classStart +
            FOOD_CLASS_COUNT;

    LOGI(
            "Class start: %d",
            classStart
    );

    LOGI(
            "Mask start: %d",
            maskStart
    );

    // ========================================================
    // 15. Decode detections
    // ========================================================

    std::vector<Detection> detections;

    detections.reserve(100);

    for (int candidate = 0;
         candidate < out0.w;
         candidate++) {

        float bestScore = 0.0f;

        int bestClass = -1;

        // ----------------------------------------------------
        // Find best class
        // ----------------------------------------------------

        for (int classId = 0;
             classId < FOOD_CLASS_COUNT;
             classId++) {

            int index =
                    classStart +
                    classId;

            const float* row =
                    out0.row(index);

            if (row == nullptr) {
                continue;
            }

            float score =
                    row[candidate];

            if (!std::isfinite(score)) {
                continue;
            }

            if (score > bestScore) {

                bestScore = score;

                bestClass = classId;
            }
        }

        // ----------------------------------------------------
        // Confidence filtering
        // ----------------------------------------------------

        if (bestClass < 0) {
            continue;
        }

        if (bestScore <
            CONFIDENCE_THRESHOLD) {

            continue;
        }

        // ----------------------------------------------------
        // Box values
        // ----------------------------------------------------

        const float* boxX =
                out0.row(0);

        const float* boxY =
                out0.row(1);

        const float* boxW =
                out0.row(2);

        const float* boxH =
                out0.row(3);

        if (boxX == nullptr ||
            boxY == nullptr ||
            boxW == nullptr ||
            boxH == nullptr) {

            continue;
        }

        float centerX =
                boxX[candidate];

        float centerY =
                boxY[candidate];

        float width =
                boxW[candidate];

        float height =
                boxH[candidate];

        if (!std::isfinite(centerX) ||
            !std::isfinite(centerY) ||
            !std::isfinite(width) ||
            !std::isfinite(height)) {

            continue;
        }

        if (width <= 0.0f ||
            height <= 0.0f) {

            continue;
        }

        float x1 =
                centerX -
                width * 0.5f;

        float y1 =
                centerY -
                height * 0.5f;

        float x2 =
                centerX +
                width * 0.5f;

        float y2 =
                centerY +
                height * 0.5f;

        // ----------------------------------------------------
        // Clamp box to 640 x 640
        // ----------------------------------------------------

        x1 =
                std::max(
                        0.0f,
                        std::min(
                                640.0f,
                                x1
                        )
                );

        y1 =
                std::max(
                        0.0f,
                        std::min(
                                640.0f,
                                y1
                        )
                );

        x2 =
                std::max(
                        0.0f,
                        std::min(
                                640.0f,
                                x2
                        )
                );

        y2 =
                std::max(
                        0.0f,
                        std::min(
                                640.0f,
                                y2
                        )
                );

        if (x2 <= x1 ||
            y2 <= y1) {

            continue;
        }

        Detection detection;

        detection.classId =
                bestClass;

        detection.confidence =
                bestScore;

        detection.x1 = x1;
        detection.y1 = y1;
        detection.x2 = x2;
        detection.y2 = y2;

        detections.push_back(
                detection
        );
    }

    LOGI(
            "Raw valid detections: %d",
            static_cast<int>(
                    detections.size()
            )
    );

    // ========================================================
    // 16. NMS
    // ========================================================

    std::vector<Detection> finalDetections =
            applyNMS(
                    detections
            );

    LOGI(
            "Final detections after NMS: %d",
            static_cast<int>(
                    finalDetections.size()
            )
    );

    // ========================================================
    // 17. Log detections
    // ========================================================

    for (size_t i = 0;
         i < finalDetections.size();
         i++) {

        const Detection& detection =
                finalDetections[i];

        int classId =
                detection.classId;

        if (classId < 0 ||
            classId >= FOOD_CLASS_COUNT) {

            continue;
        }

        LOGI(
                "DETECTION %d: %s | %.2f%%",
                static_cast<int>(i + 1),
                FOOD_NAMES[classId],
                detection.confidence * 100.0f
        );
    }

    // ========================================================
    // 18. Build Android result
    // ========================================================

    std::ostringstream result;

    result << "FOODSEG INFERENCE SUCCESS\n";

    result << "DETECTION_COUNT: "
           << finalDetections.size()
           << "\n";

    result << "FOODS:\n";

    if (finalDetections.empty()) {

        result << "NONE\n";

    } else {

        for (size_t i = 0;
             i < finalDetections.size();
             i++) {

            const Detection& detection =
                    finalDetections[i];

            int classId =
                    detection.classId;

            if (classId < 0 ||
                classId >= FOOD_CLASS_COUNT) {

                continue;
            }

            int confidence =
                    static_cast<int>(
                            detection.confidence *
                            100.0f
                    );

            result << FOOD_NAMES[classId]
                   << "|"
                   << confidence
                   << "\n";
        }
    }

    result << "SEGMENTATION: READY\n";

    result << "OUT0: "
           << out0.w
           << "x"
           << out0.h
           << "\n";

    result << "OUT1: "
           << out1.w
           << "x"
           << out1.h
           << "x"
           << out1.c;

    LOGI("========================================");
    LOGI("MULTI-FOOD ANALYSIS COMPLETED");
    LOGI("========================================");

    return env->NewStringUTF(
            result.str().c_str()
    );
}