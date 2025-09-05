cmake_minimum_required(VERSION 3.4.1)
set(TARGET wenet)
#project(${TARGET} CXX)
set(CMAKE_CXX_STANDARD 17)
include(ExternalProject)
include(FetchContent)

option(TORCH "whether to build with Torch" ON)
option(ONNX "whether to build with ONNX" OFF)
option(ITN "whether to use WeTextProcessing" ON)
set(CMAKE_VERBOSE_MAKEFILE on)
set(build_DIR ${CMAKE_SOURCE_DIR}/../../../build)
list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/cmake)
string(REPLACE "-Wl,--exclude-libs,libgcc_real.a" "" CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS}")

include(libtorch)
include(openfst)
#include(wetextprocessing)

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/kaldi
        ${CMAKE_CURRENT_SOURCE_DIR}/text_utils
)
add_subdirectory(text_processor)
add_subdirectory(text_utils)
add_subdirectory(utils)
add_subdirectory(frontend)
add_subdirectory(post_processor)
add_subdirectory(kaldi)  # kaldi: wfst based decoder
add_subdirectory(decoder)


add_dependencies(utils openfst)
add_dependencies(text_utils glog gflags openfst)
add_dependencies(text_processor text_utils openfst)
add_dependencies(post_processor text_processor)


link_libraries(frontend decoder android)
add_library(${TARGET} SHARED wenet.cc)

# 确保库被复制到正确位置
add_custom_command(TARGET ${TARGET} POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy
        ${openfst_LIB_DIR}/libfst.so
        ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libfst.so
)

#add_executable(decoder_main bin/decoder_main.cc)
#target_link_libraries(decoder_main PUBLIC libc++_shared.so)
