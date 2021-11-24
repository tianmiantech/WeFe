/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.component.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.OotComponent;
import com.welab.wefe.board.service.component.base.io.*;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.database.repository.TaskRepository;
import com.welab.wefe.board.service.dto.entity.job.TaskResultOutputModel;
import com.welab.wefe.board.service.dto.kernel.machine_learning.KernelJob;
import com.welab.wefe.board.service.dto.kernel.machine_learning.KernelTask;
import com.welab.wefe.board.service.dto.kernel.machine_learning.Member;
import com.welab.wefe.board.service.dto.kernel.machine_learning.TaskConfig;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.JobService;
import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.TaskStatus;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.util.ModelMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Service
public abstract class AbstractComponent<T extends AbstractCheckModel> {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * component type about data set
     */
    public final static List<ComponentType> DATA_IO_COMPONENT_TYPE_LIST = Arrays.asList(
            ComponentType.DataIO,
            ComponentType.HorzLRValidationDataSetLoader,
            ComponentType.VertLRValidationDataSetLoader,
            ComponentType.HorzXGBoostValidationDataSetLoader,
            ComponentType.VertXGBoostValidationDataSetLoader
    );

    /**
     * component type about modeling
     */
    public final static List<ComponentType> MODEL_COMPONENT_TYPE_LIST = Arrays.asList(
            ComponentType.HorzLR,
            ComponentType.VertLR,
            ComponentType.HorzSecureBoost,
            ComponentType.VertSecureBoost,
            ComponentType.MixLR,
            ComponentType.MixSecureBoost
    );


    @Autowired
    protected JobService jobService;
    @Autowired
    protected TaskResultService taskResultService;
    @Autowired
    protected TaskRepository taskRepository;

    /**
     * create mix flow task
     *
     * @param graph    graph
     * @param preTasks pre task list
     * @param node     node
     */
    public List<TaskMySqlModel> buildMixTask(FlowGraph graph, List<TaskMySqlModel> preTasks, KernelJob jobInfo, FlowGraphNode node) throws StatusCodeWithException {

        T params = (T) node.getParamsModel();

        // check task params at promoter
        if (graph.getJob().getMyRole() == JobMemberRole.promoter) {
            checkBeforeBuildTask(graph, preTasks, node, params);

            // need intersected
            if (needIntersectedDataSetBeforeMe()) {
                if (!parentHasIntersectedDataSet(graph, node)) {
                    throw new FlowNodeException(node, "请在前置流程中执行数据集对齐");
                }
            }
        }
        JSONObject taskParam = createTaskParams(graph, preTasks, node, params);
        if (taskParam == null) {
            return null;
        }
        List<KernelTask> kernelTasks = getMixTaskMembers(graph, node);
        List<TaskMySqlModel> subTasks = new ArrayList<>();
        int count = 1;

        FlowGraphNode parentNode = null;
        List<FlowGraphNode> parentNodes = node.getParents();
        if (!CollectionUtils.isEmpty(parentNodes)) {
            parentNode = parentNodes.get(0);
        }
        int randomCipherSeed = new Random().nextInt(100) + 1;

        for (KernelTask kernelTask : kernelTasks) {
            TaskMySqlModel task = new TaskMySqlModel();
            task.setDeep(node.getDeep());
            task.setPosition(node.getPosition());
            task.setJobId(graph.getJob().getJobId());
            task.setFlowId(graph.getJob().getFlowId());
            task.setFlowNodeId(node.getNodeId());
            task.setTaskType(taskType());
            node.setTaskName(FlowGraphNode.createTaskName(node.getComponentType(), node.getNodeId()) + "_" + count);
            task.setName(node.getTaskName());

            if (parentNode != null) {
                parentNode
                        .setTaskName(FlowGraphNode.createTaskName(parentNode.getComponentType(), parentNode.getNodeId())
                                + "_" + count);
            }

            TaskConfig taskConfig = new TaskConfig();

            if (graph.getJob().getMyRole() == JobMemberRole.provider) {
                if (node.getComponentType() == ComponentType.MixLR
                        || node.getComponentType() == ComponentType.MixSecureBoost) {
                    taskParam.put("random_cipher_seed", randomCipherSeed);
                }
            }
            taskConfig.setJob(jobInfo);
            taskConfig.setModule(taskType());
            taskConfig.setParams(taskParam);
            taskConfig.setInput(getInputs(graph, node));
            taskConfig.setOutput(getOutputs(graph, node));
            taskConfig.setTask(kernelTask);
            task.setTaskConf(JSON.toJSONString(taskConfig));
            task.setRole(graph.getJob().getMyRole());
            task.setStatus(TaskStatus.wait_run);
            task.setTaskId(node.createTaskId(graph.getJob(), count));
            task.setParentTaskIdList(
                    node.createParentTaskIds(graph.getJob(), getCount(preTasks, node.getDeep() - 1, count)));
            task.setProjectId(node.getProjectId());
            taskRepository.save(task);
            subTasks.add(task);
            count++;
        }
        return subTasks;
    }

    private int getCount(List<TaskMySqlModel> preTasks, int parentDeep, int currentCount) {
        if (parentDeep < 0 || preTasks == null || preTasks.isEmpty()) {
            return currentCount;
        }
        List<TaskMySqlModel> pTasks = preTasks.stream().filter(t -> t.getDeep() == parentDeep)
                .collect(Collectors.toList());
        int size = pTasks.size();
        return Math.min(currentCount, size);
    }

    /**
     * create task instance
     *
     * @param graph    flow graph
     * @param preTasks A collection of created tasks
     * @param node     the node of flow
     */
    public TaskMySqlModel buildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, KernelJob jobInfo, FlowGraphNode node) throws StatusCodeWithException {

        T params = (T) node.getParamsModel();

        // Check the legitimacy of the task created on the promoter side (the legitimacy of the process, the validity of the parameters)
        if (graph.getJob().getMyRole() == JobMemberRole.promoter) {
            checkBeforeBuildTask(graph, preTasks, node, params);

            // Requires alignment in front
            if (needIntersectedDataSetBeforeMe()) {
                if (!parentHasIntersectedDataSet(graph, node)) {
                    throw new FlowNodeException(node, "请在前置流程中执行数据集对齐");
                }
            }
        }

        JSONObject taskParam = createTaskParams(graph, preTasks, node, params);
        if (taskParam == null) {
            return null;
        }

        TaskMySqlModel task = new TaskMySqlModel();
        task.setDeep(node.getDeep());
        task.setPosition(node.getPosition());
        task.setJobId(graph.getJob().getJobId());
        task.setFlowId(graph.getJob().getFlowId());
        task.setFlowNodeId(node.getNodeId());
        task.setTaskType(taskType());
        task.setName(node.getTaskName());

        TaskConfig taskConfig = new TaskConfig();
        taskConfig.setJob(jobInfo);
        taskConfig.setModule(taskType());
        taskConfig.setParams(taskParam);
        taskConfig.setInput(getInputs(graph, node));
        taskConfig.setOutput(getOutputs(graph, node));
        taskConfig.setTask(getTaskMembers(graph, node));

        task.setTaskConf(
                JSON.toJSONString(taskConfig)
        );

        task.setRole(graph.getJob().getMyRole());
        task.setStatus(TaskStatus.wait_run);
        task.setTaskId(node.createTaskId(graph.getJob()));
        task.setParentTaskIdList(node.createParentTaskIds(graph.getJob()));
        task.setProjectId(node.getProjectId());
        taskRepository.save(task);

        return task;
    }


    protected TaskMySqlModel findTaskFromPretasks(List<TaskMySqlModel> preTasks, FlowGraphNode node) {
        return preTasks
                .stream()
                .filter(x -> x.getFlowNodeId().equals(node.getNodeId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Do you need to stop creating the task
     */
    public boolean stopCreateTask(List<FlowGraphNode> preNodes, FlowGraphNode node) throws StatusCodeWithException {
        return false;
    }

    /**
     * Show all execution results
     */
    public List<TaskResultOutputModel> getTaskAllResult(String taskId) {
        List<TaskResultMySqlModel> result = getAllResult(taskId);
        if (result == null) {
            return null;
        }

        return result
                .stream()
                .filter(Objects::nonNull)
                .map(x -> ModelMapper.map(x, TaskResultOutputModel.class))
                .collect(Collectors.toList());
    }

    /**
     * Show the specified execution result
     */
    public TaskResultOutputModel getTaskResult(String taskId, String type) throws StatusCodeWithException {
        TaskResultMySqlModel result = getResult(taskId, type);
        if (result == null) {
            return null;
        }

        return ModelMapper.map(result, TaskResultOutputModel.class);
    }

    /**
     * Get the output node object of the task
     */
    public Map<String, Object> getOutputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        List<OutputItem> outputs = outputs(graph, node);
        if (outputs == null) {
            return null;
        }

        Map<DataTypeGroup, List<OutputItem>> map = outputs
                .stream()
                .collect(Collectors.groupingBy(
                        x -> x.getDataType().getGroup(),
                        Collectors.toList()
                ));

        JObject result = JObject.create();

        map.forEach((k, v) ->
                result.put(
                        k.getKey(),
                        v.stream()
                                .map(OutputItem::getName)
                                .collect(Collectors.toList())
                )
        );

        return result;
    }

    /**
     * Get the input node object of the task
     */
    public Map<String, Object> getInputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        // If manually specified, use the specified input.

        // else, automatic guess.
        List<NodeOutputItem> inputNodes = findInputNodes(graph, node);
        Map<DataTypeGroup, List<NodeOutputItem>> map = inputNodes
                .stream()
                .collect(Collectors.groupingBy(
                        x -> x.getDataType().getGroup(),
                        Collectors.toList()
                ));

        JObject result = JObject.create();

        map.forEach((k, v) -> result.putAll(new InputGroup(k, v).toJsonNode()));

        // When input is empty, a data node needs to be constructed.
        if (result.size() <= 0) {
            result.append("data", JObject.create());
        }

        return result;
    }

    /**
     * Automatic estimation of input and output between nodes
     */
    public List<NodeOutputItem> findInputNodes(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        List<NodeOutputItem> result = new ArrayList<>();

        List<InputMatcher> matchers = inputs(graph, node);
        if (matchers == null) {
            return result;
        }

        for (InputMatcher matcher : matchers) {

            NodeOutputItem item = matcher.dopeOut(graph, node);

            result.add(item);
        }

        return result;
    }


    /**
     * Find the current member's data from the list
     */
    public <T> T findMyData(Collection<T> list, Function<T, String> getMemberIdFunc) {

        if (list == null) {
            return null;
        }

        for (T item : list) {
            String memberId = getMemberIdFunc.apply(item);
            if (CacheObjects.getMemberId().equals(memberId)) {
                return item;
            }
        }

        return null;
    }

    /**
     * Deserialize form parameters into Param objects
     */
    public T deserializationParam(FlowGraphNode node, String json) throws FlowNodeException {
        if (json == null) {
            json = "{}";
        }
        Class<T> paramsClass = getParamsClass(this.getClass());
        T params = JObject
                .create(json)
                .toJavaObject(paramsClass);

        // Basic check of entry (non-empty, regular check)
        try {
            params.checkAndStandardize();
        } catch (StatusCodeWithException e) {
            throw new FlowNodeException(node, e.getMessage());
        }

        return params;
    }

    /**
     * Get the input parameter type of the current component
     */
    private Class<T> getParamsClass(Class<?> clazz) {

        while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
            clazz = clazz.getSuperclass();
        }

        Type[] types = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
        if (types.length > 0) {
            Class<?> type = (Class<?>) types[0];
            return (Class<T>) type;
        }

        return getParamsClass(clazz.getSuperclass());
    }

    public List<KernelTask> getMixTaskMembers(FlowGraph graph, FlowGraphNode node) {
        List<KernelTask> kernelTasks = new ArrayList<>();
        List<Member> allMembers = graph.getMembers().stream().map(Member::new).collect(Collectors.toList());
        List<Member> promoters = allMembers.stream().filter(s -> s.getMemberRole() == JobMemberRole.promoter)
                .collect(Collectors.toList());
        List<Member> providers = allMembers.stream().filter(s -> s.getMemberRole() == JobMemberRole.provider)
                .collect(Collectors.toList());

        Member arbiter = allMembers.stream().filter(x -> x.getMemberRole() == JobMemberRole.arbiter).findFirst()
                .orElse(null);

        if (arbiter == null) {
            // need arbiter
            if (node.getComponentType() == ComponentType.MixLR
                    || node.getComponentType() == ComponentType.MixSecureBoost
                    || node.getComponentType() == ComponentType.MixStatistic) {
                Member promoter = allMembers.stream().filter(x -> x.getMemberRole() == JobMemberRole.promoter)
                        .findFirst().orElse(null);
                if (promoter != null) {
                    arbiter = new Member();
                    arbiter.setMemberId(promoter.getMemberId());
                    arbiter.setMemberRole(JobMemberRole.arbiter);
                    arbiter.setMemberName(promoter.getMemberName());
                    allMembers.add(arbiter);
                }
            }
        }

        if (graph.getJob().getMyRole() == JobMemberRole.promoter) {
            // need arbiter
            if (node.getComponentType() == ComponentType.MixLR
                    || node.getComponentType() == ComponentType.MixSecureBoost
                    || node.getComponentType() == ComponentType.MixStatistic) {
                KernelTask task = new KernelTask();
                task.setMembers(allMembers);
                kernelTasks.add(task);
            } else {
                List<Member> members = new ArrayList<>();
                members.add(promoters.stream().filter(x -> CacheObjects.getMemberId().equals(x.getMemberId()))
                        .findFirst().orElse(null));
                members.addAll(providers);
                KernelTask task = new KernelTask();
                task.setMembers(members);
                kernelTasks.add(task);
            }
        } else if (graph.getJob().getMyRole() == JobMemberRole.arbiter) {
            if (node.getComponentType() == ComponentType.MixLR
                    || node.getComponentType() == ComponentType.MixSecureBoost) {
                // do not need provider
                List<Member> members = new ArrayList<>(promoters);
                members.add(
                        allMembers.stream().filter(s -> s.getMemberRole() == JobMemberRole.arbiter).findFirst().orElse(null));
                KernelTask task = new KernelTask();
                task.setMembers(members);
                kernelTasks.add(task);
            } else if (node.getComponentType() == ComponentType.MixStatistic) {
                KernelTask task = new KernelTask();
                task.setMembers(allMembers);
                kernelTasks.add(task);
            }
        }
        // provider need more tasks
        else if (graph.getJob().getMyRole() == JobMemberRole.provider) {
            int count = 0;
            int providerMasterIndex = 1;
            List<String> providerOtherInnerId = new ArrayList<>();
            String providerMasterInnerId = CacheObjects.getMemberId() + "_" + providerMasterIndex;
            int size = promoters.size();
            for (int i = 0; i < size; i++) {
                if (i != providerMasterIndex) {
                    providerOtherInnerId.add(CacheObjects.getMemberId() + "_" + i);
                }
            }
            for (Member promoter : promoters) {
                if (node.getComponentType() == ComponentType.MixLR
                        || node.getComponentType() == ComponentType.MixSecureBoost
                        || node.getComponentType() == ComponentType.MixStatistic) {
                    KernelTask task = new KernelTask();
                    task.setMembers(allMembers);
                    task.setMixPromoterMemberId(promoter.getMemberId());

                    // special params
                    task.setProviderMaster(count == providerMasterIndex);
                    task.setProviderInnerId(CacheObjects.getMemberId() + "_" + count);
                    task.setProviderOtherInnerId(providerOtherInnerId);
                    task.setProviderMasterInnerId(providerMasterInnerId);
                    count++;
                    kernelTasks.add(task);
                } else {
                    List<Member> members = new ArrayList<>();
                    members.add(promoter);
                    members.addAll(providers);
                    KernelTask task = new KernelTask();
                    task.setMembers(members);
                    task.setMixPromoterMemberId(promoter.getMemberId());

                    // special params
                    task.setProviderMaster(count == providerMasterIndex);
                    task.setProviderInnerId(CacheObjects.getMemberId() + "_" + count);
                    task.setProviderOtherInnerId(providerOtherInnerId);
                    task.setProviderMasterInnerId(providerMasterInnerId);
                    count++;
                    kernelTasks.add(task);
                }
            }
        }
        return kernelTasks;
    }

    /**
     * Obtain member information of participating components, subject to the members selected by DataIO.
     */
    public KernelTask getTaskMembers(FlowGraph graph, FlowGraphNode node) {

        List<DataIOComponent.DataSetItem> dataSetItems = null;
        if (node.getComponentType() == ComponentType.DataIO) {
            dataSetItems = ((DataIOComponent.Params) node.getParamsModel()).getDataSetList();
        } else if (node.getComponentType() == ComponentType.Oot) {
            OotComponent.Params params = ((OotComponent.Params) node.getParamsModel());
            dataSetItems = params.getDataSetList();
        } else {
            DataIOComponent.Params dataIOParams = (DataIOComponent.Params) graph.findOneNodeFromParent(node, ComponentType.DataIO).getParamsModel();
            dataSetItems = dataIOParams.getDataSetList();
        }

        List<Member> members = new ArrayList<>();
        KernelTask task = new KernelTask();
        dataSetItems.forEach(x -> {
            Member member = new Member();
            member.setMemberId(x.getMemberId());
            member.setMemberName(CacheObjects.getMemberName(x.getMemberId()));
            member.setMemberRole(x.getMemberRole());
            members.add(member);
            // Horizontal modeling component, and the current member is a promoter, need to increase arbiter.
            if (node.getComponentType() == ComponentType.HorzLR || node.getComponentType() == ComponentType.HorzSecureBoost) {
                if (x.getMemberRole() == JobMemberRole.promoter && CacheObjects.getMemberId().equals(x.getMemberId())) {
                    Member arbiterMember = new Member();
                    arbiterMember.setMemberId(x.getMemberId());
                    arbiterMember.setMemberName(CacheObjects.getMemberName(x.getMemberId()));
                    arbiterMember.setMemberRole(JobMemberRole.arbiter);
                    members.add(arbiterMember);
                }
            }
        });

        Member promoter = graph.getMembers().stream().map(x -> new Member(x))
                .filter(s -> s.getMemberRole() == JobMemberRole.promoter).findFirst().orElse(null);

        if (node.getComponentType() == ComponentType.HorzLR
                || node.getComponentType() == ComponentType.HorzSecureBoost) {
            if (graph.getJob().getMyRole() == JobMemberRole.provider && promoter != null) {
                Member arbiterMember = new Member();
                arbiterMember.setMemberId(promoter.getMemberId());
                arbiterMember.setMemberName(CacheObjects.getMemberName(promoter.getMemberId()));
                arbiterMember.setMemberRole(JobMemberRole.arbiter);
                members.add(arbiterMember);
            }
        }

        task.setMembers(members);

        return task;
    }

    /**
     * Whether you can select features, Not by default.
     */
    public boolean canSelectFeatures() {
        return false;
    }

    /**
     * Whether there are form parameters, there are by default,
     * and the components that are not are overridden in subclasses.
     */
    public boolean hasParams() {
        return true;
    }

    /**
     * Declare whether data alignment needs to be performed before me
     */
    protected boolean needIntersectedDataSetBeforeMe() {
        return false;
    }

    public boolean parentHasIntersectedDataSet(FlowGraph graph, FlowGraphNode node) {
        FlowGraphNode intersectedDataSet = graph.findOneNodeFromParent(node, x -> {
            if (x.getComponentType() == ComponentType.Intersection) {
                return true;
            }

            if (x.getComponentType() == ComponentType.DataIO) {
                DataIOComponent.Params dataIOParams = (DataIOComponent.Params) x.getParamsModel();
                DataSetMysqlModel myDataSet = dataIOParams.getMyDataSet();

                // If it is not a derived data set, it must have been misaligned.
                if (myDataSet != null && myDataSet.getSourceType() != null) {

                    // If the derived data set comes from alignment
                    if (myDataSet.getSourceType() == ComponentType.Intersection) {
                        return true;
                    }

                    /**
                     * Here is a bit rough for the time being,
                     * thinking that all the derived data sets are aligned.
                     * If you don't do this,
                     * you need to investigate the source job of the derived data set and search recursively.
                     */
                    return true;
                }


            }

            return false;
        });

        return intersectedDataSet != null;
    }

    //region abstract

    /**
     * Check the validity of the input parameters and throw an exception when the requirements are not met.
     */
    protected abstract void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, T params) throws FlowNodeException;

    /**
     * Assemble the input parameters of the task according to the component configuration
     */
    protected abstract JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, T params) throws FlowNodeException;

    public abstract ComponentType taskType();

    /**
     * Show all execution results
     */
    protected abstract List<TaskResultMySqlModel> getAllResult(String taskId);

    /**
     * Show the specified execution result
     */
    protected abstract TaskResultMySqlModel getResult(String taskId, String type) throws StatusCodeWithException;

    /**
     * Declare the input parameter type
     */
    protected abstract List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException;

    /**
     * Declare the output type
     */
    public abstract List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException;

    //endregion
}
