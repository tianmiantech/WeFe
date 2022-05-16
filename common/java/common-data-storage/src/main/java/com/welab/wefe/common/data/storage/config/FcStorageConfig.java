package com.welab.wefe.common.data.storage.config;


import com.welab.wefe.common.data.storage.common.FunctionComputeType;
import org.springframework.util.Assert;

/**
 * @author yuxin.zhang
 */
public class FcStorageConfig {
    private String instanceName;

    private String accessKeyId;

    private String accessKeySecret;

    private String ossInternalEndPoint;

    private String bucketName;
    private String region;

    private FunctionComputeType functionComputeType;

    public FcStorageConfig() {

    }

    public FcStorageConfig(
            FunctionComputeType functionComputeType,
            String accessKeyId,
            String accessKeySecret,
            String bucketName,
            String instanceName,
            String region
    ) {
        Assert.notNull(accessKeyId, "accessKeyId == null");
        Assert.notNull(accessKeySecret, "accessKeySecret == null");
        Assert.notNull(bucketName, "bucketName == null");
        Assert.notNull(instanceName, "instanceName == null");
        Assert.notNull(region, "region == null");
        Assert.notNull(functionComputeType, "functionComputeType == null");

        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        this.region = region;
        this.instanceName = instanceName;
        this.functionComputeType = functionComputeType;
    }


    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }


    public String getOssInternalEndPoint() {
        return ossInternalEndPoint;
    }

    public void setOssInternalEndPoint(String ossInternalEndPoint) {
        this.ossInternalEndPoint = ossInternalEndPoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }


    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }


    public FunctionComputeType getFunctionComputeType() {
        return functionComputeType;
    }

    public void setFunctionComputeType(FunctionComputeType functionComputeType) {
        this.functionComputeType = functionComputeType;
    }
}
