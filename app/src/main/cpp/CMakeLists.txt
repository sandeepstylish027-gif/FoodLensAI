cmake_minimum_required(VERSION 3.22.1)

project("foodlens_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ============================================================
# NCNN
# ============================================================

set(NCNN_DIR ${CMAKE_CURRENT_SOURCE_DIR}/ncnn)

include_directories(
        ${NCNN_DIR}
        ${NCNN_DIR}/ncnn
)

add_library(
        ncnn
        STATIC
        IMPORTED
)

set_target_properties(
        ncnn
        PROPERTIES
        IMPORTED_LOCATION
        ${NCNN_DIR}/libncnn.a
)

# ============================================================
# ONNX RUNTIME
# ============================================================

set(ONNXRUNTIME_DIR
        ${CMAKE_CURRENT_SOURCE_DIR}/onnxruntime
)

set(ONNXRUNTIME_INCLUDE_DIR
        ${ONNXRUNTIME_DIR}/headers
)

set(ONNXRUNTIME_LIB
        ${ONNXRUNTIME_DIR}/jni/arm64-v8a/libonnxruntime.so
)

include_directories(
        ${ONNXRUNTIME_INCLUDE_DIR}
)

add_library(
        onnxruntime
        SHARED
        IMPORTED
)

set_target_properties(
        onnxruntime
        PROPERTIES
        IMPORTED_LOCATION
        ${ONNXRUNTIME_LIB}
)

# ============================================================
# FOODLENS NATIVE LIBRARY
# ============================================================

add_library(
        foodlens_native
        SHARED
        native-lib.cpp
)

# ============================================================
# COMPILER OPTIONS
# ============================================================

target_compile_options(
        foodlens_native
        PRIVATE
        -fopenmp
)

# ============================================================
# LINKER OPTIONS
# ============================================================

target_link_options(
        foodlens_native
        PRIVATE
        -fopenmp
)

# ============================================================
# LINK LIBRARIES
# ============================================================

target_link_libraries(
        foodlens_native

        ncnn

        onnxruntime

        log
        android
        jnigraphics
        m
        atomic
)