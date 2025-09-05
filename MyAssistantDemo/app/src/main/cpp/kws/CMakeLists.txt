cmake_minimum_required(VERSION 3.4.1)
set(LIBRARY_NAME wekws)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_INCLUDE_CURRENT_DIR on)

set(build_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../build)

file(GLOB ONNXRUNTIME_INCLUDE_DIRS "${build_DIR}/onnxruntime*.aar/headers")
include_directories(${ONNXRUNTIME_INCLUDE_DIRS})

file(GLOB ONNXRUNTIME_LINK_DIRS "${build_DIR}/onnxruntime*.aar/jni/${ANDROID_ABI}")
link_directories(${ONNXRUNTIME_LINK_DIRS})

file(GLOB HARDER_FILES "${CMAKE_CURRENT_SOURCE_DIR}/*.h")
file(GLOB SRC_FILES "${CMAKE_CURRENT_SOURCE_DIR}/*.cc")

add_library(${LIBRARY_NAME} SHARED ${HARDER_FILES} ${SRC_FILES})
target_link_libraries(${LIBRARY_NAME} PUBLIC onnxruntime)
