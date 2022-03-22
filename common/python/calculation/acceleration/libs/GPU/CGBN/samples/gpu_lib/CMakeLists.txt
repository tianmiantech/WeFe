cmake_minimum_required(VERSION 3.6)

project(gpu_lib)

# to replace you environment
set(MY_PYTHON_ROOT "/data/miniconda3/envs/wefe_python37")
set(MY_PROJECT_ROOT "/home/xxx/data/code/wefe.git")

# set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}")

find_package(CUDA)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR})

# find_package(GMP REQUIRED)
# find_package(PythonLibs 3.6 REQUIRED)

set(PYTHON_INCLUDE_DIRS "${MY_PYTHON_ROOT}/include/python3.7m")
set(PYTHON_LIBRARIES "${MY_PYTHON_ROOT}/lib")

set(CGBN_INCLUDES "${MY_PROJECT_ROOT}/common/python/calculation/acceleration/libs/GPU/CGBN/include")
set(PYBIND11_INCLUDE "${MY_PROJECT_ROOT}/common/python/calculation/acceleration/libs/tools/pybind11/include")

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
