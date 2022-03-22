# 公钥密码算法

## RSA加解密算法

### 密钥生成

1. 随机选择两个大素数 p和q，而且保密；

2. 计算 n = pq ，将 n公开；

3. 计算<img src="https://render.githubusercontent.com/render/math?math=\varphi=(p-1)(q-1)">，对<img src="https://render.githubusercontent.com/render/math?math=\varphi">保密；

4. 随机第选取一个正整数e ，<img src="https://render.githubusercontent.com/render/math?math=1 \lt e \lt \varphi">  且 <img src="https://render.githubusercontent.com/render/math?math=(e,\varphi(n)) =1">，将e公开；

5. 根据 <img src="https://render.githubusercontent.com/render/math?math=ed=1mod\varphi(n) "> ，求出 d，并对 d 保密。

   RSA 密码算法的公钥 <img src="https://render.githubusercontent.com/render/math?math=k_e(n,e) "> ，私钥 <img src="https://render.githubusercontent.com/render/math?math=K_d(p,q,d,\varphi(n)) ">。

### 加密

<img src="https://render.githubusercontent.com/render/math?math=C=M^emod\,n ">

### 解密

<img src="https://render.githubusercontent.com/render/math?math=M=C^dmod\,n ">



## DH秘钥交换算法





![image-20211223184148296](images/RSA密码算法和DH密码算法/image-20211223184148296.png)

