# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.22.1-g37088a8-dirty)

# We name the project and the target for the ExternalProject_Add() call
# to something that will highlight to the user what we are working on if
# something goes wrong and an error message is produced.

project(ggml-populate NONE)



include(ExternalProject)
ExternalProject_Add(ggml-populate
                     "UPDATE_DISCONNECTED" "False"
                    SOURCE_DIR          "C:/Users/Vergel/Documents/development/ewn/avelisse-android/whisper/src/main/jni/whisper/../../../../../third_party/whisper.cpp/ggml"
                    BINARY_DIR          "C:/Users/Vergel/Documents/development/ewn/avelisse-android/whisper/.cxx/Release/2k1d4k3p/arm64-v8a/_deps/ggml-build"
                    CONFIGURE_COMMAND   ""
                    BUILD_COMMAND       ""
                    INSTALL_COMMAND     ""
                    TEST_COMMAND        ""
                    USES_TERMINAL_DOWNLOAD  YES
                    USES_TERMINAL_UPDATE    YES
)


