/***

Copyright (c) 2018-2019, NVIDIA CORPORATION.  All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
IN THE SOFTWARE.

***/


#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <time.h>
#include <chrono>
#include <cuda.h>
#include <gmp.h>
#include <cuda_runtime.h>
#include "cgbn/cgbn.h"
#include "../utility/support.h"
#include <iostream>
#include <list>
#include <tuple>
#include <sstream>      //  istringstream、ostringstream和stringstream
#include <vector>
#include <bitset>
#include <pybind11/pybind11.h>
#include <pybind11/stl.h>


namespace py = pybind11;

using namespace std;

template<typename T>
string toString(const T& t){
    ostringstream oss;  //创建一个格式化输出流
    oss<<t;             //把值传递如流中
    return oss.str();  
}

template <class T>
int getLength(const T &arr){ //由于我们不知道T是什么，所以采用 pass by reference-to-const
  return sizeof(arr) / sizeof(arr[0]);
};

//一般方法，32位，逐步与1做与运算。=====================================
void Binarycout(uint32_t n)
{
  for (int i = 31; i >= 0; i--)
  {
    cout << ((n >> i) & 1);
  }
}

struct powmod_param_int {
    std::vector<uint32_t> x;
    std::vector<uint32_t> p;
    std::vector<uint32_t> modulus;

    powmod_param_int(vector<uint32_t> _x, vector<uint32_t> _p, vector<uint32_t> _modulus)
    {
      x = _x;
      p = _p;
      modulus = _modulus;
    }
};


// 数组转 vector, 逆序返回
std::vector<uint32_t> to_vector(uint32_t *arrHeight, uint32_t count){

  std::vector<uint32_t> int_vec(count);
  for(int index=0; index < count ; index++){
    int_vec[(count - 1) - index ] = arrHeight[index];
  }
  return int_vec;
}

std::vector<uint32_t> str_to_vector(std::string str, uint32_t bits){


  int count = bits / 32;
  vector<uint32_t> vec(count);
  for (int i = 0; i < count ; i++){
    // vec 正序
    cout << str.substr(i * 32,  32)  << endl;
    uint32_t value = stol(str.substr(i * 32, 32), nullptr, 2);
    vec[i] = value;
  }

  return vec;
}


template <uint32_t tpi, uint32_t bits, uint32_t window_bits>
class powm_params_t
{
public:
  // parameters used by the CGBN context
  static const uint32_t TPB = 0;           // get TPB from blockDim.x
  static const uint32_t MAX_ROTATION = 4;  // good default value
  static const uint32_t SHM_LIMIT = 0;     // no shared mem available
  static const bool CONSTANT_TIME = false; // constant time implementations aren't available yet

  // parameters used locally in the application
  static const uint32_t TPI = tpi;                 // threads per instance
  static const uint32_t BITS = bits;               // instance size
  static const uint32_t WINDOW_BITS = window_bits; // window size
};

//使用拓展欧几里得算法求e的模n的逆元d
bool getModInverse(const mpz_t e, const mpz_t n, mpz_t d)
{
  mpz_t a, b, c, c1, t, q, r;
  mpz_inits(a, b, c, c1, t, q, r, NULL);
  mpz_set(a, n);     //a=n;
  mpz_set(b, e);     //b=e;
  mpz_set_ui(c, 0);  //c=0
  mpz_set_ui(c1, 1); //c1=1
  mpz_tdiv_qr(q, r, a, b);
  while (mpz_cmp_ui(r, 0)) //r==0终止循环
  {
    mpz_mul(t, q, c1); //t=q*c1
    mpz_sub(t, c, t);  //t=c-q*c1

    mpz_set(c, c1); //c=c1  向后移动
    mpz_set(c1, t); //c1=t  向后移动

    mpz_set(a, b);           //a=b 除数变为被除数
    mpz_set(b, r);           //b=r 余数变为除数，开始下一轮
    mpz_tdiv_qr(q, r, a, b); //取下一个q
  }
  mpz_set(d, t); //将最后一轮的t赋值给d, d就是e的模n的逆元

  //保证返回正整数
  mpz_add(d, d, n);
  mpz_mod(d, d, n);

  mpz_clears(a, c, t, q, r, NULL);

  if (mpz_cmp_ui(b, 1))
  {
    mpz_clear(b);
    return false;
  }
  else
  {
    mpz_clear(b);
    return true;
  }
}

//蒙哥马利模乘，A=(A*B)mod n
void MontMult(mpz_t A, mpz_t B, const mpz_t n, int n_bit, const mpz_t IN)
{
  mpz_t T, T1, t0, b_32;
  mpz_inits(T, T1, t0, b_32, NULL);

  mpz_mul(T, A, B); //T=A*B
  mpz_set_ui(b_32, 0xFFFFFFFF);

  int t = n_bit >> 5;
  for (int i = 0; i < t; i++)
  {
    mpz_and(t0, T, b_32);
    mpz_mul(t0, IN, t0);
    mpz_and(t0, t0, b_32);

    //T1=T+n*t0
    mpz_mul(T1, n, t0);
    mpz_add(T1, T, T1);

    //T1>>32，T=T1
    mpz_tdiv_q_2exp(T1, T1, 32);
    mpz_set(T, T1);
  }

  if (mpz_cmp(T1, n) > 0)
  { //T1>n，A=T1-n
    mpz_sub(T1, T1, n);
    mpz_set(A, T1);
  }
  else
    mpz_set(A, T1);
  mpz_clears(T, T1, t0, b_32, NULL);
}

template <class params>
class powm_odd_t
{
public:
  static const uint32_t window_bits = params::WINDOW_BITS; // used a lot, give it an instance variable

  // define the instance structure
  typedef struct
  {
    cgbn_mem_t<params::BITS> x;
    cgbn_mem_t<params::BITS> power;
    cgbn_mem_t<params::BITS> modulus;
    cgbn_mem_t<params::BITS> result;
    // cgbn_mem_t<params::BITS> r_high;
  } instance_t;


  typedef cgbn_context_t<params::TPI, params> context_t;
  typedef cgbn_env_t<context_t, params::BITS> env_t;
  typedef typename env_t::cgbn_t bn_t;
  typedef typename env_t::cgbn_wide_t bn_wide_t;
  typedef typename env_t::cgbn_local_t bn_local_t;

  context_t _context;
  env_t _env;
  int32_t _instance;

  __device__ __forceinline__ powm_odd_t(cgbn_monitor_t monitor, cgbn_error_report_t *report, int32_t instance) : _context(monitor, report, (uint32_t)instance), _env(_context), _instance(instance)
  {
  }

  __device__ __forceinline__ void fixed_window_powm_odd(bn_t &result, const bn_t &x, const bn_t &power, const bn_t &modulus)
  {
    bn_t t;
    bn_local_t window[1 << window_bits];
    int32_t index, position, offset;
    uint32_t np0;

    // conmpute x^power mod modulus, using the fixed window algorithm
    // requires:  x<modulus,  modulus is odd

    // compute x^0 (in Montgomery space, this is just 2^BITS - modulus)
    cgbn_negate(_env, t, modulus);
    cgbn_store(_env, window + 0, t);

    // convert x into Montgomery space, store into window table
    np0 = cgbn_bn2mont(_env, result, x, modulus);
    cgbn_store(_env, window + 1, result);
    cgbn_set(_env, t, result);

// compute x^2, x^3, ... x^(2^window_bits-1), store into window table
#pragma nounroll
    for (index = 2; index < (1 << window_bits); index++)
    {
      cgbn_mont_mul(_env, result, result, t, modulus, np0);
      cgbn_store(_env, window + index, result);
    }

    // find leading high bit
    position = params::BITS - cgbn_clz(_env, power);

    // break the exponent into chunks, each window_bits in length
    // load the most significant non-zero exponent chunk
    offset = position % window_bits;
    if (offset == 0)
      position = position - window_bits;
    else
      position = position - offset;
    index = cgbn_extract_bits_ui32(_env, power, position, window_bits);
    cgbn_load(_env, result, window + index);

    // process the remaining exponent chunks
    while (position > 0)
    {
// square the result window_bits times
#pragma nounroll
      for (int sqr_count = 0; sqr_count < window_bits; sqr_count++)
        cgbn_mont_sqr(_env, result, result, modulus, np0);

      // multiply by next exponent chunk
      position = position - window_bits;
      index = cgbn_extract_bits_ui32(_env, power, position, window_bits);
      cgbn_load(_env, t, window + index);
      cgbn_mont_mul(_env, result, result, t, modulus, np0);
    }

    // we've processed the exponent now, convert back to normal space
    cgbn_mont2bn(_env, result, result, modulus, np0);
  }

  __device__ __forceinline__ void sliding_window_powm_odd(bn_t &result, const bn_t &x, const bn_t &power, const bn_t &modulus)
  {
    bn_t t, starts;
    int32_t index, position, leading;
    uint32_t mont_inv;
    bn_local_t odd_powers[1 << window_bits - 1];

    // conmpute x^power mod modulus, using Constant Length Non-Zero windows (CLNZ).
    // requires:  x<modulus,  modulus is odd

    // find the leading one in the power
    leading = params::BITS - 1 - cgbn_clz(_env, power);
    if (leading >= 0)
    {
      // convert x into Montgomery space, store in the odd powers table
      mont_inv = cgbn_bn2mont(_env, result, x, modulus);

      // compute t=x^2 mod modulus
      cgbn_mont_sqr(_env, t, result, modulus, mont_inv);

      // compute odd powers window table: x^1, x^3, x^5, ...
      cgbn_store(_env, odd_powers, result);
#pragma nounroll
      for (index = 1; index < (1 << window_bits - 1); index++)
      {
        cgbn_mont_mul(_env, result, result, t, modulus, mont_inv);
        cgbn_store(_env, odd_powers + index, result);
      }

      // starts contains an array of bits indicating the start of a window
      cgbn_set_ui32(_env, starts, 0);

      // organize p as a sequence of odd window indexes
      position = 0;
      while (true)
      {
        if (cgbn_extract_bits_ui32(_env, power, position, 1) == 0)
          position++;
        else
        {
          cgbn_insert_bits_ui32(_env, starts, starts, position, 1, 1);
          if (position + window_bits > leading)
            break;
          position = position + window_bits;
        }
      }

      // load first window.  Note, since the window index must be odd, we have to
      // divide it by two before indexing the window table.  Instead, we just don't
      // load the index LSB from power
      index = cgbn_extract_bits_ui32(_env, power, position + 1, window_bits - 1);
      cgbn_load(_env, result, odd_powers + index);
      position--;

      // Process remaining windows
      while (position >= 0)
      {
        cgbn_mont_sqr(_env, result, result, modulus, mont_inv);
        if (cgbn_extract_bits_ui32(_env, starts, position, 1) == 1)
        {
          // found a window, load the index
          index = cgbn_extract_bits_ui32(_env, power, position + 1, window_bits - 1);
          cgbn_load(_env, t, odd_powers + index);
          cgbn_mont_mul(_env, result, result, t, modulus, mont_inv);
        }
        position--;
      }

      // convert result from Montgomery space
      cgbn_mont2bn(_env, result, result, modulus, mont_inv);
    }
    else
    {
      // p=0, thus x^p mod modulus=1
      cgbn_set_ui32(_env, result, 1);
    }
  }


  __host__ static void verify_results(instance_t *instances, uint32_t count)
  {
    mpz_t x, p, m, computed, correct;

    mpz_init(x);
    mpz_init(p);
    mpz_init(m);
    mpz_init(computed);
    mpz_init(correct);

    for (int index = 0; index < count; index++)
    {
      to_mpz(x, instances[index].x._limbs, params::BITS / 32);
      to_mpz(p, instances[index].power._limbs, params::BITS / 32);
      to_mpz(m, instances[index].modulus._limbs, params::BITS / 32);
      to_mpz(computed, instances[index].result._limbs, params::BITS / 32);

      // 蒙哥马利算法 计算  ( a * b ) % N
      // mpz_powm(correct, x, p, m);
      // getModInverse(m, p, IN);

      // MontMult(x, p, m, 1024, IN);

      if (mpz_cmp(x, computed) != 0)
      {
        printf("gpu inverse kernel failed on instance %d\n", index);
        return;
      }
    }

    mpz_clear(x);
    mpz_clear(p);
    mpz_clear(m);
    mpz_clear(computed);
    mpz_clear(correct);

    printf("All results match\n");
  }

  __host__ static void str_to_limbs(uint32_t *x_list, vector<uint32_t> bignum, uint32_t count) {
    
    for(int index=0;index<count;index++){
        // 逆序放入数组
        x_list[(count - 1 )- index] = bignum[index];
    }
  }

      // transform to instances
      // __host__ static instance_t *to_instances(std::vector<powmod_param_int> arrs, uint32_t bits, uint32_t instance_count) {
      //   instance_t *instances=(instance_t *)malloc(sizeof(instance_t)*instance_count);

      //   for (int index=0; index < instance_count ; index++){
      //       str_to_limbs(instances[index].x._limbs, arrs[index].x , bits/32);
      //       str_to_limbs(instances[index].power._limbs, arrs[index].p , bits/32);
      //       str_to_limbs(instances[index].modulus._limbs,  arrs[index].modulus , bits/32);
      //   }
      //   return instances;
      // }

    __host__ static std::time_t getTimeStamp()
    {
        std::chrono::time_point<std::chrono::system_clock,std::chrono::milliseconds> tp = std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::system_clock::now());//获取当前时间点
        std::time_t timestamp =  tp.time_since_epoch().count(); //计算距离1970-1-1,00:00的时间长度
        return timestamp;
    }


    __host__ static std::time_t getTimeStamp()
    {
        std::chrono::time_point<std::chrono::system_clock,std::chrono::milliseconds> tp = std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::system_clock::now());//获取当前时间点
        std::time_t timestamp =  tp.time_since_epoch().count(); //计算距离1970-1-1,00:00的时间长度
        return timestamp;
    }


  __host__ static instance_t *to_instances(std::vector<tuple<py::bytes, py::bytes, py::bytes>> arrs, uint32_t bits, uint32_t instance_count)
  {
    instance_t *instances = (instance_t *)malloc(sizeof(instance_t) * instance_count);
    // cout << "copy start currentTime =  " << getTimeStamp() << endl;
    for (int index = 0; index < instance_count; index++)
    {
      char *x, *p, *modulus;

      Py_ssize_t len;
      PyBytes_AsStringAndSize(std::get<0>(arrs[index]).ptr(), &x, &len);
      PyBytes_AsStringAndSize(std::get<1>(arrs[index]).ptr(), &p, &len);
      PyBytes_AsStringAndSize(std::get<2>(arrs[index]).ptr(), &modulus, &len);

      memcpy(&(instances[index].x._limbs), x, len);
      memcpy(&(instances[index].power._limbs), p, len);
      memcpy(&(instances[index].modulus._limbs), modulus, len);
    }
    // cout << "copy end currentTime =  " << getTimeStamp() << endl;
    return instances;
  }

  __host__ static instance_t *to_instances_2(std::vector<py::bytes> arrs, py::bytes p_byte, py::bytes m_byte, uint32_t bits, uint32_t instance_count)
  {
    instance_t *instances = (instance_t *)malloc(sizeof(instance_t) * instance_count);

    for (int index = 0; index < instance_count; index++)
    {
      char *x, *p, *modulus;

      Py_ssize_t len;
      PyBytes_AsStringAndSize(arrs[index].ptr(), &x, &len);
      PyBytes_AsStringAndSize(p_byte.ptr(), &p, &len);
      PyBytes_AsStringAndSize(m_byte.ptr(), &modulus, &len);

      memcpy(&(instances[index].x._limbs), x, len);
      memcpy(&(instances[index].power._limbs), p, len);
      memcpy(&(instances[index].modulus._limbs), modulus, len);
    }
    // cout << "copy end currentTime =  " << getTimeStamp() << endl;
    return instances;
  }

  __host__ static instance_t *to_instances_2(std::vector<py::bytes> arrs, py::bytes p_byte, py::bytes m_byte, uint32_t bits, uint32_t instance_count)
  {
    instance_t *instances = (instance_t *)malloc(sizeof(instance_t) * instance_count);

    for (int index = 0; index < instance_count; index++)
    {
      char *x, *p, *modulus;

      Py_ssize_t len;
      PyBytes_AsStringAndSize(arrs[index].ptr(), &x, &len);
      PyBytes_AsStringAndSize(p_byte.ptr(), &p, &len);
      PyBytes_AsStringAndSize(m_byte.ptr(), &modulus, &len);

      memcpy(&(instances[index].x._limbs), x, len);
      memcpy(&(instances[index].power._limbs), p, len);
      memcpy(&(instances[index].modulus._limbs), modulus, len);

    }
    return instances;
  }


  // __host__ static std::vector<std::vector<uint32_t>> result_to_list(powm_odd_t<params>::instance_t *instances, uint32_t bits, uint32_t count){
      
  //     std::vector<std::vector<uint32_t>> result_list(count);
  //     for (int index = 0; index < count ; index++){
  //       // to_vector 方法逆序转化
  //       result_list[index] = to_vector(instances[index].result._limbs, bits/32);
  //     }
  //     return result_list;
  // }
  __host__ static std::vector<py::bytes> result_to_list(powm_odd_t<params>::instance_t *instances, uint32_t bits, uint32_t instance_count)
  {

    std::vector<py::bytes> result_list(instance_count);

    for (int i = 0;i < instance_count ; i++){
      py::bytes result = py::bytes((char *)instances[i].result._limbs, bits / 8);
      // cout << result << endl;
      result_list[i] = result;
    }
    return result_list;
  }
};


template <class params>
__global__ void kernel_powm_odd(cgbn_error_report_t *report, typename powm_odd_t<params>::instance_t *instances, uint32_t count)
{
  int32_t instance;

  // decode an instance number from the blockIdx and threadIdx
  instance = (blockIdx.x * blockDim.x + threadIdx.x) / params::TPI;
  if (instance >= count)
    return;

  powm_odd_t<params> po(cgbn_report_monitor, report, instance);
  typename powm_odd_t<params>::bn_t r, x, p, m;

  // the loads and stores can go in the class, but it seems more natural to have them
  // here and to pass in and out bignums
  cgbn_load(po._env, x, &(instances[instance].x));
  cgbn_load(po._env, p, &(instances[instance].power));
  cgbn_load(po._env, m, &(instances[instance].modulus));

  // this can be either fixed_window_powm_odd or sliding_window_powm_odd.
  // when TPI<32, fixed window runs much faster because it is less divergent, so we use it here
  po.fixed_window_powm_odd(r, x, p, m);

  cgbn_store(po._env, &(instances[instance].result), r);
}


template <class params>
__global__ void kernel_mulm(cgbn_error_report_t *report, typename powm_odd_t<params>::instance_t *instances, uint32_t count)
{
  int32_t instance;

  // decode an instance number from the blockIdx and threadIdx
  instance = (blockIdx.x * blockDim.x + threadIdx.x) / params::TPI;
  if (instance >= count)
    return;

  powm_odd_t<params> po(cgbn_report_monitor, report, instance);
  typename powm_odd_t<params>::bn_t  r, x, p, m, approx;
  typename powm_odd_t<params>::bn_wide_t w;

  // the loads and stores can go in the class, but it seems more natural to have them
  // here and to pass in and out bignums


  cgbn_load(po._env, x, &(instances[instance].x));
  cgbn_load(po._env, p, &(instances[instance].power));
  cgbn_load(po._env, m, &(instances[instance].modulus));

  uint32_t    clz_count;
  
  // compute the approximation of the inverse
  clz_count=cgbn_barrett_approximation(po._env, approx, m);
  
  // compute the wide product of a*b
  cgbn_mul_wide(po._env, w, x, p);
  
  // compute r=a*b mod d.  Pass the clz_count returned by the approx routine.
  cgbn_barrett_rem_wide(po._env, r, w, m, approx, clz_count);

  // cgbn_store(po._env, &(instances[instance].r_low), r_low );
  // cgbn_store(po._env, &(instances[instance].r_high), r_high );
  cgbn_store(po._env, &(instances[instance].result), r );

}


template <uint32_t tpi, uint32_t bits, uint32_t window_bits>
std::vector<py::bytes> powm(std::vector<tuple<py::bytes, py::bytes, py::bytes>> arrs, uint32_t instance_count)
{
  //   TPI             - threads per instance
  //   BITS            - number of bits per instance
  //   WINDOW_BITS     - number of bits to use for the windowed exponentiation

  typedef powm_params_t<tpi, bits, window_bits> params;
  // typedef powm_params_t<_tpi, _bits, _ window_bits> params;
  typedef typename powm_odd_t<params>::instance_t instance_t;

  instance_t *instances, *gpuInstances;
  cgbn_error_report_t *report;
  int32_t TPB = (params::TPB == 0) ? 128 : params::TPB; // default threads per block to 128
  int32_t TPI = params::TPI, IPB = TPB / TPI;           // IPB is instances per block

  instances = powm_odd_t<params>::to_instances(arrs, bits, instance_count);

  // printf("Copying instances to the GPU ...\n");
  CUDA_CHECK(cudaSetDevice(0));

  // cout << "copy to gpu start currentTime =  " << powm_odd_t<params>::getTimeStamp() << endl;
  CUDA_CHECK(cudaMalloc((void **)&gpuInstances, sizeof(instance_t) * instance_count));
  CUDA_CHECK(cudaMemcpy(gpuInstances, instances, sizeof(instance_t) * instance_count, cudaMemcpyHostToDevice));
  // cout << "copy to gpu end currentTime =  " << powm_odd_t<params>::getTimeStamp() << endl;

  // create a cgbn_error_report for CGBN to report back errors
  CUDA_CHECK(cgbn_error_report_alloc(&report));

  // printf("Running GPU kernel ...\n");

  // launch kernel with blocks=ceil(instance_count/IPB) and threads=TPB
  kernel_powm_odd<params><<<(instance_count + IPB - 1) / IPB, TPB>>>(report, gpuInstances, instance_count);

  // error report uses managed memory, so we sync the device (or stream) and check for cgbn errors
  CUDA_CHECK(cudaDeviceSynchronize());
  CGBN_CHECK(report);

  // copy the instances back from gpuMemory
//   printf("Copying results back to CPU ...\n");
    // cout << "back to host start currentTime =  " << powm_odd_t<params>::getTimeStamp() << endl;
  CUDA_CHECK(cudaMemcpy(instances, gpuInstances, sizeof(instance_t) * instance_count, cudaMemcpyDeviceToHost));
    // cout << "back to host end currentTime =  " << powm_odd_t<params>::getTimeStamp() << endl;

  // printf("Verifying the results ...\n");
  // powm_odd_t<params>::verify_results(instances, instance_count);

  std::vector<py::bytes> gpu_result = powm_odd_t<params>::result_to_list(instances, bits, instance_count);

  // clean up
  free(instances);
  CUDA_CHECK(cudaFree(gpuInstances));
  CUDA_CHECK(cgbn_error_report_free(report));

  return gpu_result;
}



template <uint32_t tpi, uint32_t bits, uint32_t window_bits>
std::vector<py::bytes> powm_2(std::vector<py::bytes> arrs , py::bytes p, py::bytes m, uint32_t instance_count)
{
  //   TPI             - threads per instance
  //   BITS            - number of bits per instance
  //   WINDOW_BITS     - number of bits to use for the windowed exponentiation

  typedef powm_params_t<tpi, bits, window_bits> params;
  // typedef powm_params_t<_tpi, _bits, _ window_bits> params;
  typedef typename powm_odd_t<params>::instance_t instance_t;

  instance_t *instances, *gpuInstances;
  cgbn_error_report_t *report;
  int32_t TPB = (params::TPB == 0) ? 128 : params::TPB; // default threads per block to 128
  int32_t TPI = params::TPI, IPB = TPB / TPI;           // IPB is instances per block

  instances = powm_odd_t<params>::to_instances_2(arrs, p,m,bits, instance_count);

//   printf("Copying instances to the GPU ...\n");
  CUDA_CHECK(cudaSetDevice(0));

  // cout << "copy to gpu start currentTime =  " << powm_odd_t<params>::getTimeStamp() << endl;
  CUDA_CHECK(cudaMalloc((void **)&gpuInstances, sizeof(instance_t) * instance_count));
  CUDA_CHECK(cudaMemcpy(gpuInstances, instances, sizeof(instance_t) * instance_count, cudaMemcpyHostToDevice));
  // cout << "copy to gpu end currentTime =  " << powm_odd_t<params>::getTimeStamp() << endl;

  // create a cgbn_error_report for CGBN to report back errors
  CUDA_CHECK(cgbn_error_report_alloc(&report));

//   printf("Running GPU kernel ...\n");

  // launch kernel with blocks=ceil(instance_count/IPB) and threads=TPB
  kernel_powm_odd<params><<<(instance_count + IPB - 1) / IPB, TPB>>>(report, gpuInstances, instance_count);

  // error report uses managed memory, so we sync the device (or stream) and check for cgbn errors
  CUDA_CHECK(cudaDeviceSynchronize());
  CGBN_CHECK(report);

  // copy the instances back from gpuMemory
  // printf("Copying results back to CPU ...\n");
  // cout << "back to host start currentTime =  " << powm_odd_t<params>::getTimeStamp() << endl;
  CUDA_CHECK(cudaMemcpy(instances, gpuInstances, sizeof(instance_t) * instance_count, cudaMemcpyDeviceToHost));
  // cout << "back to host end currentTime =  " << powm_odd_t<params>::getTimeStamp() << endl;

  // printf("Verifying the results ...\n");
  // powm_odd_t<params>::verify_results(instances, instance_count);

  std::vector<py::bytes> gpu_result = powm_odd_t<params>::result_to_list(instances, bits, instance_count);

  // clean up
  free(instances);
  CUDA_CHECK(cudaFree(gpuInstances));
  CUDA_CHECK(cgbn_error_report_free(report));

  return gpu_result;
}



template <uint32_t tpi, uint32_t bits, uint32_t window_bits>
std::vector<py::bytes> powm_2(std::vector<py::bytes> arrs , py::bytes p, py::bytes m, uint32_t instance_count)
{
  //   TPI             - threads per instance
  //   BITS            - number of bits per instance
  //   WINDOW_BITS     - number of bits to use for the windowed exponentiation

  typedef powm_params_t<tpi, bits, window_bits> params;
  // typedef powm_params_t<_tpi, _bits, _ window_bits> params;
  typedef typename powm_odd_t<params>::instance_t instance_t;

  instance_t *instances, *gpuInstances;
  cgbn_error_report_t *report;
  int32_t TPB = (params::TPB == 0) ? 128 : params::TPB; // default threads per block to 128
  int32_t TPI = params::TPI, IPB = TPB / TPI;           // IPB is instances per block

  instances = powm_odd_t<params>::to_instances_2(arrs, p,m,bits, instance_count);

  // printf("Copying instances to the GPU ...\n");
  CUDA_CHECK(cudaSetDevice(0));
  CUDA_CHECK(cudaMalloc((void **)&gpuInstances, sizeof(instance_t) * instance_count));
  CUDA_CHECK(cudaMemcpy(gpuInstances, instances, sizeof(instance_t) * instance_count, cudaMemcpyHostToDevice));

  // create a cgbn_error_report for CGBN to report back errors
  CUDA_CHECK(cgbn_error_report_alloc(&report));

  // printf("Running GPU kernel ...\n");

  // launch kernel with blocks=ceil(instance_count/IPB) and threads=TPB
  kernel_powm_odd<params><<<(instance_count + IPB - 1) / IPB, TPB>>>(report, gpuInstances, instance_count);

  // error report uses managed memory, so we sync the device (or stream) and check for cgbn errors
  CUDA_CHECK(cudaDeviceSynchronize());
  CGBN_CHECK(report);

  // copy the instances back from gpuMemory
  // printf("Copying results back to CPU ...\n");
  CUDA_CHECK(cudaMemcpy(instances, gpuInstances, sizeof(instance_t) * instance_count, cudaMemcpyDeviceToHost));

  // printf("Verifying the results ...\n");
  // powm_odd_t<params>::verify_results(instances, instance_count);

  std::vector<py::bytes> gpu_result = powm_odd_t<params>::result_to_list(instances, bits, instance_count);

  // clean up
  free(instances);
  CUDA_CHECK(cudaFree(gpuInstances));
  CUDA_CHECK(cgbn_error_report_free(report));

  return gpu_result;
}


template<uint32_t tpi, uint32_t bits, uint32_t window_bits>
std::vector<py::bytes> mulm(std::vector<tuple<py::bytes, py::bytes, py::bytes>> arrs, uint32_t instance_count) {
  //   TPI             - threads per instance
  //   BITS            - number of bits per instance
  //   WINDOW_BITS     - number of bits to use for the windowed exponentiation

  typedef powm_params_t<tpi, bits, window_bits> params;
  // typedef powm_params_t<_tpi, _bits, _ window_bits> params;
  typedef typename powm_odd_t<params>::instance_t instance_t;


  instance_t          *instances, *gpuInstances;
  cgbn_error_report_t *report;
  int32_t              TPB=(params::TPB==0) ? 128 : params::TPB;    // default threads per block to 128
  int32_t              TPI=params::TPI, IPB=TPB/TPI;                // IPB is instances per block

    
  instances = powm_odd_t<params>::to_instances(arrs, bits, instance_count);

  
  // printf("Copying instances to the GPU ...\n");
  CUDA_CHECK(cudaSetDevice(0));
  CUDA_CHECK(cudaMalloc((void **)&gpuInstances, sizeof(instance_t)*instance_count));
  CUDA_CHECK(cudaMemcpy(gpuInstances, instances, sizeof(instance_t)*instance_count, cudaMemcpyHostToDevice));
  
  // create a cgbn_error_report for CGBN to report back errors
  CUDA_CHECK(cgbn_error_report_alloc(&report)); 
  
  // printf("Running GPU kernel ...\n");
  
  // launch kernel with blocks=ceil(instance_count/IPB) and threads=TPB
  kernel_mulm<params><<<(instance_count+IPB-1)/IPB, TPB>>>(report, gpuInstances, instance_count);

  // error report uses managed memory, so we sync the device (or stream) and check for cgbn errors
  CUDA_CHECK(cudaDeviceSynchronize());
  CGBN_CHECK(report);
    
  // copy the instances back from gpuMemory
  // printf("Copying results back to CPU ...\n");
  CUDA_CHECK(cudaMemcpy(instances, gpuInstances, sizeof(instance_t)*instance_count, cudaMemcpyDeviceToHost));

  // std::vector<std::vector<uint32_t>> gpu_result = powm_odd_t<params>::result_to_list(instances,bits,instance_count);
  std::vector<py::bytes> gpu_result = powm_odd_t<params>::result_to_list(instances, bits, instance_count);


  // clean up
  free(instances);
  CUDA_CHECK(cudaFree(gpuInstances));
  CUDA_CHECK(cgbn_error_report_free(report));

  return gpu_result;
}


template<uint32_t tpi, uint32_t bits, uint32_t window_bits>
std::vector<py::bytes> mulm_2(std::vector<py::bytes> arrs , py::bytes p, py::bytes m, uint32_t instance_count) {
  //   TPI             - threads per instance
  //   BITS            - number of bits per instance
  //   WINDOW_BITS     - number of bits to use for the windowed exponentiation

  typedef powm_params_t<tpi, bits, window_bits> params;
  // typedef powm_params_t<_tpi, _bits, _ window_bits> params;
  typedef typename powm_odd_t<params>::instance_t instance_t;


  instance_t          *instances, *gpuInstances;
  cgbn_error_report_t *report;
  int32_t              TPB=(params::TPB==0) ? 128 : params::TPB;    // default threads per block to 128
  int32_t              TPI=params::TPI, IPB=TPB/TPI;                // IPB is instances per block


  instances = powm_odd_t<params>::to_instances_2(arrs, p, m, bits, instance_count);


//   printf("Copying instances to the GPU ...\n");
  CUDA_CHECK(cudaSetDevice(0));
  CUDA_CHECK(cudaMalloc((void **)&gpuInstances, sizeof(instance_t)*instance_count));
  CUDA_CHECK(cudaMemcpy(gpuInstances, instances, sizeof(instance_t)*instance_count, cudaMemcpyHostToDevice));

  // create a cgbn_error_report for CGBN to report back errors
  CUDA_CHECK(cgbn_error_report_alloc(&report));

//   printf("Running GPU kernel ...\n");

  // launch kernel with blocks=ceil(instance_count/IPB) and threads=TPB
  kernel_mulm<params><<<(instance_count+IPB-1)/IPB, TPB>>>(report, gpuInstances, instance_count);

  // error report uses managed memory, so we sync the device (or stream) and check for cgbn errors
  CUDA_CHECK(cudaDeviceSynchronize());
  CGBN_CHECK(report);

  // copy the instances back from gpuMemory
//   printf("Copying results back to CPU ...\n");
  CUDA_CHECK(cudaMemcpy(instances, gpuInstances, sizeof(instance_t)*instance_count, cudaMemcpyDeviceToHost));

  // std::vector<std::vector<uint32_t>> gpu_result = powm_odd_t<params>::result_to_list(instances,bits,instance_count);
  std::vector<py::bytes> gpu_result = powm_odd_t<params>::result_to_list(instances, bits, instance_count);


  // clean up
  free(instances);
  CUDA_CHECK(cudaFree(gpuInstances));
  CUDA_CHECK(cgbn_error_report_free(report));

  return gpu_result;
}


template<uint32_t tpi, uint32_t bits, uint32_t window_bits>
std::vector<py::bytes> mulm_2(std::vector<py::bytes> arrs , py::bytes p, py::bytes m, uint32_t instance_count) {
  //   TPI             - threads per instance
  //   BITS            - number of bits per instance
  //   WINDOW_BITS     - number of bits to use for the windowed exponentiation

  typedef powm_params_t<tpi, bits, window_bits> params;
  // typedef powm_params_t<_tpi, _bits, _ window_bits> params;
  typedef typename powm_odd_t<params>::instance_t instance_t;


  instance_t          *instances, *gpuInstances;
  cgbn_error_report_t *report;
  int32_t              TPB=(params::TPB==0) ? 128 : params::TPB;    // default threads per block to 128
  int32_t              TPI=params::TPI, IPB=TPB/TPI;                // IPB is instances per block


  instances = powm_odd_t<params>::to_instances_2(arrs, p, m, bits, instance_count);


  // printf("Copying instances to the GPU ...\n");
  CUDA_CHECK(cudaSetDevice(0));
  CUDA_CHECK(cudaMalloc((void **)&gpuInstances, sizeof(instance_t)*instance_count));
  CUDA_CHECK(cudaMemcpy(gpuInstances, instances, sizeof(instance_t)*instance_count, cudaMemcpyHostToDevice));

  // create a cgbn_error_report for CGBN to report back errors
  CUDA_CHECK(cgbn_error_report_alloc(&report));

  // printf("Running GPU kernel ...\n");

  // launch kernel with blocks=ceil(instance_count/IPB) and threads=TPB
  kernel_mulm<params><<<(instance_count+IPB-1)/IPB, TPB>>>(report, gpuInstances, instance_count);

  // error report uses managed memory, so we sync the device (or stream) and check for cgbn errors
  CUDA_CHECK(cudaDeviceSynchronize());
  CGBN_CHECK(report);

  // copy the instances back from gpuMemory
  // printf("Copying results back to CPU ...\n");
  CUDA_CHECK(cudaMemcpy(instances, gpuInstances, sizeof(instance_t)*instance_count, cudaMemcpyDeviceToHost));

  // std::vector<std::vector<uint32_t>> gpu_result = powm_odd_t<params>::result_to_list(instances,bits,instance_count);
  std::vector<py::bytes> gpu_result = powm_odd_t<params>::result_to_list(instances, bits, instance_count);


  // clean up
  free(instances);
  CUDA_CHECK(cudaFree(gpuInstances));
  CUDA_CHECK(cgbn_error_report_free(report));

  return gpu_result;
}


PYBIND11_MODULE(gpu_lib, m) {

    // py::class_<powmod_param_int>(m, "powmod_param_int")
    //   .def(py::init<vector<uint32_t>, vector<uint32_t>, vector<uint32_t>>());
    m.def("mulm_2048", &mulm<8, 2048, 5>, py::return_value_policy::reference);
    m.def("mulm_1024", &mulm<8, 1024, 5>, py::return_value_policy::reference);

    m.def("powm_1024", &powm<8, 1024, 5>, py::return_value_policy::reference);
    m.def("powm_2048", &powm<8, 2048, 5>, py::return_value_policy::reference);
    m.def("powm_1024_2", &powm_2<8, 1024, 5>, py::return_value_policy::reference);
    m.def("powm_2048_2", &powm_2<8, 2048, 5>, py::return_value_policy::reference);
}
