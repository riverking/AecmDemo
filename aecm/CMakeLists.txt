# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        AEC

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/lib/randomization_functions.c
        src/main/cpp/lib/spl_sqrt_floor.c
        src/main/cpp/lib/division_operations.c
        src/main/cpp/lib/vector_scaling_operations.c
        src/main/cpp/lib/downsample_fast.c
        src/main/cpp/lib/cross_correlation.c
        src/main/cpp/lib/spl_init.c
        src/main/cpp/lib/min_max_operations.c
        src/main/cpp/lib/complex_fft.c
        src/main/cpp/lib/complex_bit_reverse.c
        src/main/cpp/lib/real_fft.c
        src/main/cpp/lib/delay_estimator.c
        src/main/cpp/lib/delay_estimator_wrapper.c
        src/main/cpp/lib/ring_buffer.c
        src/main/cpp/lib/aecm_core.c
        src/main/cpp/lib/echo_control_mobile.c
        src/main/cpp/aecm/AEC.c)

include_directories(src/main/cpp)

add_definitions(-DWEBRTC_POSIX)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        AEC

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})