# 公钥密码算法

## RSA加解密算法

### 密钥生成

1. 随机选择两个大素数 p和q，而且保密；

2. 计算$ n = pq $ ，将 n公开；

3. 计算 $\varphi = (p-1)(q-1)$，对 $\varphi$保密；

4. 随机第选取一个正整数e ， $1 < e < \varphi$ 且$(e,\varphi(n))=1 $，将e公开；

5. 根据 $ed = 1mod \varphi(n)$，求出d，并对d保密。

   RSA 密码算法的公钥   $k_e(n,e)$，私钥  $K_d (p,q,d,\varphi(n) )$。

### 加密

$$
C = M^e mod \, n
$$



#### 解密

$$
M = C^d mod \,n 
$$







## DH秘钥交换算法





![image-20211223184148296](images/RSA密码算法和DH密码算法/image-20211223184148296.png)


