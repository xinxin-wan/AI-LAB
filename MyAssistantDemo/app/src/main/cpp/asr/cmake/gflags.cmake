#FetchContent_Declare(gflags
#  URL      https://github.com/gflags/gflags/archive/v2.2.2.zip
#  URL_HASH SHA256=19713a36c9f32b33df59d1c79b4958434cb005b5b47dc5400a7a4b078111d9b5
#)
#FetchContent_MakeAvailable(gflags)


include(ExternalProject)
set(CMAKE_VERBOSE_MAKEFILE on)
set(ANDROID_CMAKE_ARGS
        -DBUILD_TESTING=OFF
        -DBUILD_SHARED_LIBS=OFF
        -DCMAKE_BUILD_TYPE=Release
        -DCMAKE_MAKE_PROGRAM=${CMAKE_MAKE_PROGRAM}
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_NATIVE_API_LEVEL=${ANDROID_NATIVE_API_LEVEL}
)

# >>>> gflags >>>>
ExternalProject_Add(gflags
        URL              https://github.com/gflags/gflags/archive/v2.2.2.zip
        URL_HASH         SHA256=19713a36c9f32b33df59d1c79b4958434cb005b5b47dc5400a7a4b078111d9b5
        BUILD_ALWAYS     true
        CMAKE_ARGS       ${ANDROID_CMAKE_ARGS}
        INSTALL_COMMAND  ""
)
ExternalProject_Get_Property(gflags BINARY_DIR)
include_directories(${BINARY_DIR}/include)
#include_directories(${gflags_BINARY_DIR}/include)
link_directories(${BINARY_DIR}/lib)
set(gflags_BINARY_DIR ${BINARY_DIR})
# <<<< gflags <<<<