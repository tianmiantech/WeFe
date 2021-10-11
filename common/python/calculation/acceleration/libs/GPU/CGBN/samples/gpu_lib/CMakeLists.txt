cmake_minimum_required(VERSION 3.6)

project(gpu_lib)
# set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}")

find_package(CUDA)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR})
# find_package(GMP REQUIRED)

# find_package(PythonLibs 3.6 REQUIRED)
set(PYTHON_INCLUDE_DIRS "/data/environment/miniconda3/envs/wefe-python37/include/python3.7m")
set(PYTHON_LIBRARIES "/data/environment/miniconda3/envs/wefe-python37/lib")

set(CGBN_INCLUDES "/home/welab/PycharmProjects/Wefe/acceleration/GPU/CGBN/include")
set(PYBIND11_INCLUDE "/home/welab/PycharmProjects/Wefe/acceleration/tools/pybind11/include")

SET(CUDA_NVCC_FLAGS -arch=sm_61)
# SET(CUDA_NVCC_FLAGS arch=sm_35)
# set(CUDA_NVCC_FLAGS -gencode arch=compute_35,code=sm_35;-G;-g)

include_directories(
  ${PYTHON_INCLUDE_DIRS}
  ${PYBIND11_INCLUDE}
  ${CGBN_INCLUDES}
  # ${GMP_INCLUDES}
  )

link_directories(
  /usr/local/cuda/lib64
  ${PYTHON_LIBRARIES}
)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

file(GLOB CURRENT_SOURCES  powm_mulm.cu)

cuda_add_library(gpu_lib SHARED  ${CURRENT_SOURCES})

# cuda_add_library(gpu_lib SHARED
#   mulm.cu
#   )

target_link_libraries(gpu_lib
  cudart)

target_link_libraries(gpu_lib gmp gmpxx)

set_target_properties(gpu_lib PROPERTIES PREFIX "")
