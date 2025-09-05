project(nebula_audio_front)

cmake_minimum_required(VERSION 3.5)

add_custom_target(${PROJECT_NAME})

# kws
add_subdirectory(kws)
add_dependencies(${PROJECT_NAME} wekws)

# asr
add_subdirectory(asr)
add_dependencies(${PROJECT_NAME} wenet)

# rtc
add_subdirectory(rtc)
add_dependencies(${PROJECT_NAME} webrtc_audio)

