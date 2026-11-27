cmake_minimum_required(VERSION 3.22)
project(viz CXX)

set(ENABLE_PLAYLIST OFF CACHE BOOL "" FORCE)
set(ENABLE_DEBUG_POSTFIX OFF CACHE BOOL "" FORCE)  # else debug builds emit libvizd.so
set(BUILD_SHARED_LIBS ON CACHE BOOL "" FORCE)  # LGPL-2.1: ship libprojectM as its own replaceable .so
set(ENABLE_SYSTEM_PROJECTM_EVAL OFF CACHE BOOL "" FORCE)
# Keep the build machine's paths out of the shipped .so files (__FILE__ in asserts/logs): repo root -> "."
get_filename_component(REPO_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/../../../.. ABSOLUTE)
add_compile_options(-ffile-prefix-map=${REPO_ROOT}=.)

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../../../../third_party/projectm projectm)

add_library(viz SHARED viz.cpp)
target_link_libraries(viz PRIVATE libprojectM::projectM log)
