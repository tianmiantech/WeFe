/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.dto.entity.modeling_config;

/**
 * 模型配置·逻辑回归
 *
 * @author Zane
 */
public class ModelingConfigLogisticRegressionOutputModel extends AbstractModelingConfigOutputModel {
    /**
     * 模型初始化方式
     */
    private String initParam_InitMethod;
    /**
     * 是否需要偏置系数
     */
    private Boolean initParam_FitIntercept;
    /**
     * 惩罚方式
     */
    private String penalty;
    /**
     * 收敛容忍度
     */
    private Double tol;
    /**
     * 惩罚项系数
     */
    private Double alpha;
    /**
     * 优化算法
     */
    private String optimizer;
    /**
     * 批量大小
     */
    private Integer batchSize;
    /**
     * 学习率
     */
    private Double learningRate;
    /**
     * 最大迭代次数
     */
    private Integer maxIter;
    /**
     * 判断收敛性与否的方法
     */
    private String earlyStop;
    /**
     * 同态加密方法
     */
    private String encryptParam_Method;
    /**
     * 在KFold中使用分割符大小
     */
    private Integer cvParam_NSplits;
    /**
     * 在KFold之前是否进行洗牌
     */
    private Boolean cvParam_Shuffle;
    /**
     * 是否需要进行此模块
     */
    private Boolean cvParam_NeedCv;
    /**
     * 学习速率的衰减率
     */
    private Double decay;
    /**
     * 衰减率是否开平方
     */
    private Boolean decaySqrt;
    /**
     * 多分类策略;枚举（ovr/ovo）
     */
    private String multiClass;
    /**
     * 验证频次
     */
    private Integer validationFreqs;
    /**
     * 提前结束的迭代次数
     */
    private Integer earlyStoppingRounds;

    //region getter/setter

    public String getInitParam_InitMethod() {
        return initParam_InitMethod;
    }

    public void setInitParam_InitMethod(String initParam_InitMethod) {
        this.initParam_InitMethod = initParam_InitMethod;
    }

    public Boolean getInitParam_FitIntercept() {
        return initParam_FitIntercept;
    }

    public void setInitParam_FitIntercept(Boolean initParam_FitIntercept) {
        this.initParam_FitIntercept = initParam_FitIntercept;
    }

    public String getPenalty() {
        return penalty;
    }

    public void setPenalty(String penalty) {
        this.penalty = penalty;
    }

    public Double getTol() {
        return tol;
    }

    public void setTol(Double tol) {
        this.tol = tol;
    }

    public Double getAlpha() {
        return alpha;
    }

    public void setAlpha(Double alpha) {
        this.alpha = alpha;
    }

    public String getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(String optimizer) {
        this.optimizer = optimizer;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(Double learningRate) {
        this.learningRate = learningRate;
    }

    public Integer getMaxIter() {
        return maxIter;
    }

    public void setMaxIter(Integer maxIter) {
        this.maxIter = maxIter;
    }

    public String getEarlyStop() {
        return earlyStop;
    }

    public void setEarlyStop(String earlyStop) {
        this.earlyStop = earlyStop;
    }

    public String getEncryptParam_Method() {
        return encryptParam_Method;
    }

    public void setEncryptParam_Method(String encryptParam_Method) {
        this.encryptParam_Method = encryptParam_Method;
    }

    public Integer getCvParam_NSplits() {
        return cvParam_NSplits;
    }

    public void setCvParam_NSplits(Integer cvParam_NSplits) {
        this.cvParam_NSplits = cvParam_NSplits;
    }

    public Boolean getCvParam_Shuffle() {
        return cvParam_Shuffle;
    }

    public void setCvParam_Shuffle(Boolean cvParam_Shuffle) {
        this.cvParam_Shuffle = cvParam_Shuffle;
    }

    public Boolean getCvParam_NeedCv() {
        return cvParam_NeedCv;
    }

    public void setCvParam_NeedCv(Boolean cvParam_NeedCv) {
        this.cvParam_NeedCv = cvParam_NeedCv;
    }

    public Double getDecay() {
        return decay;
    }

    public void setDecay(Double decay) {
        this.decay = decay;
    }

    public Boolean getDecaySqrt() {
        return decaySqrt;
    }

    public void setDecaySqrt(Boolean decaySqrt) {
        this.decaySqrt = decaySqrt;
    }

    public String getMultiClass() {
        return multiClass;
    }

    public void setMultiClass(String multiClass) {
        this.multiClass = multiClass;
    }

    public Integer getValidationFreqs() {
        return validationFreqs;
    }

    public void setValidationFreqs(Integer validationFreqs) {
        this.validationFreqs = validationFreqs;
    }

    public Integer getEarlyStoppingRounds() {
        return earlyStoppingRounds;
    }

    public void setEarlyStoppingRounds(Integer earlyStoppingRounds) {
        this.earlyStoppingRounds = earlyStoppingRounds;
    }
//endregion
}
