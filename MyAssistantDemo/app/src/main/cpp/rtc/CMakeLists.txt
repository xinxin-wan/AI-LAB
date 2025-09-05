set(LIBRARY_NAME webrtc_audio)
cmake_minimum_required(VERSION 3.5)
add_compile_options(-frtti)
set(CMAKE_SHARED_LINKER_FLAGS -fuse-ld=bfd)
file(GLOB HARDER_FILES "${CMAKE_CURRENT_SOURCE_DIR}/agc/*.h")
set(SRC_FILES "${CMAKE_CURRENT_SOURCE_DIR}/agc/complex_bit_reverse.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/complex_fft.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/cross_correlation.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/dot_product_with_scale.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/downsample_fast.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/energy.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/fft4g.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/get_scaling_square.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/min_max_operations.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/real_fft.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/resample.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/resample_48khz.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/resample_by_2.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/resample_by_2_internal.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/resample_by_2_mips.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/copy_set_operations.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/division_operations.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/spl_init.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/spl_sqrt.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/spl_sqrt_floor.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/ring_buffer.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/resample_fractional.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/splitting_filter.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/vector_scaling_operations.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/analog_agc.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/digital_agc.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/ns_core.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/nsx_core.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/nsx_core_c.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/nsx_core_neon_offsets.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/noise_suppression.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/agc/noise_suppression_x.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/ns_jni_wrapper.c")

set(CMAKE_INCLUDE_CURRENT_DIR on)
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/agc")

add_library(${LIBRARY_NAME} SHARED ${SRC_FILES})
target_link_libraries(${LIBRARY_NAME} android log)