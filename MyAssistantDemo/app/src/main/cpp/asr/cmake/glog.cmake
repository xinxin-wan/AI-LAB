#FetchContent_Declare(glog
#  URL      https://github.com/google/glog/archive/v0.4.0.zip
#  URL_HASH SHA256=9e1b54eb2782f53cd8af107ecf08d2ab64b8d0dc2b7f5594472f3bd63ca85cdc
#)
#FetchContent_MakeAvailable(glog)
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

# >>>> glog >>>>
ExternalProject_Add(glog
        URL              https://github.com/google/glog/archive/refs/tags/v0.6.0.zip
        URL_HASH         SHA256=122fb6b712808ef43fbf80f75c52a21c9760683dae470154f02bddfc61135022
        BUILD_ALWAYS     true
        CMAKE_ARGS       ${ANDROID_CMAKE_ARGS}
        INSTALL_COMMAND  ""
)

ExternalProject_Get_Property(glog SOURCE_DIR BINARY_DIR INSTALL_DIR)
include_directories(${SOURCE_DIR}/src ${BINARY_DIR} ${BINARY_DIR} ${INSTALL_DIR}/include)
#include_directories(${glog_SOURCE_DIR}/src ${glog_BINARY_DIR})
link_directories(${BINARY_DIR})
set(glog_SOURCE_DIR ${SOURCE_DIR})
set(glog_BINARY_DIR ${BINARY_DIR})
set(glog_INCLUDE_DIR ${INSTALL_DIR}/include)
# <<<< glog <<<<