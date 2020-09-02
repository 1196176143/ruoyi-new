package com.ruoyi.web.controller.flowable;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


@Controller
@RequestMapping(value = "expense")

public class ExpenseController {

    private String prefix = "flowable";
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    @Autowired
    private  RepositoryService repositoryService;
    private final ProcessEngine processEngine;

   /* private final static List<UserProcessModel> ProcessModels = new ArrayList<UserProcessModel>();
    {
    *//*    ProcessModels.add(new UserProcessModel("asdfasdf", "1000001", "测试1", "0", "15888888888", "ry@qq.com", "123", "0"));
        ProcessModels.add(new UserProcessModel("fdaadsff", "1000002", "测试2", "1", "15666666666", "ry@qq.com", "456", "1"));
        ProcessModels.add(new UserProcessModel("asfdccsse", "1000003", "测试3", "0", "15666666666", "ry@qq.com", "789", "1"));*//*
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> list = processDefinitionQuery.list();
    }*/




    public ExpenseController(RuntimeService runtimeService, TaskService taskService, RepositoryService repositoryService, @Qualifier("processEngine") ProcessEngine processEngine) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
        this.processEngine = processEngine;
    }


    /**
     * 流程部署本地路径方式
     */
    @RequestMapping("/dm")
    @ResponseBody
    public Deployment deployFlow()
    {
        try
        {
            String filePath ="C:\\Users\\liulianfeng\\Desktop\\OrderApproval.bpmn20.xml";
            InputStream inputStream = new FileInputStream(new File(filePath));
            DeploymentBuilder deploymentBuilder = repositoryService
                    .createDeployment()
                    .category("测试分类")
                    .name("sxspv")
                    .addInputStream(filePath,inputStream);
             Deployment deployment= deploymentBuilder.deploy();
            System.out.println("成功：部署工作流成：" + filePath+ ",流程ID："+deployment.getId());
            return deployment;
        }
        catch (Exception e)
        {
            System.out.println("失败：部署工作流：" + e);
            return null;
        }
    }




    /**
     * 直接加载表格数据
     */
    @GetMapping("/data")
    public String data(ModelMap mmap)
    {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> list = processDefinitionQuery.list();
        mmap.put("users", list);
        return prefix + "/data";

    }



    /**
     * 查询所有的流程定义
     */
    @RequestMapping("/re")
    @ResponseBody
    public void findProcessDefinition() {
        List<ProcessDefinition> list = processEngine.getRepositoryService()// 与流程定义和部署对象先相关的service
                .createProcessDefinitionQuery()// 创建一个流程定义的查询

                /** 指定查询条件，where条件 */
                // .deploymentId(deploymentId) //使用部署对象ID查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                // .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

                /* 排序 */
                .orderByProcessDefinitionVersion().asc()
                // .orderByProcessDefinitionVersion().desc()

                /* 返回的结果集 */
                .list();// 返回一个集合列表，封装流程定义
        // .singleResult();//返回惟一结果集
        // .count();//返回结果集数量
        // .listPage(firstResult, maxResults);//分页查询

        if (list != null && list.size() > 0) {
            for (ProcessDefinition pd : list) {
                System.out.println("ID:" + pd.getId());// 流程定义的key+版本+随机生成数
                System.out.println("名称:" + pd.getName());// 对应helloworld.bpmn文件中的name属性值
                System.out.println("key:" + pd.getKey());// 对应helloworld.bpmn文件中的id属性值
                System.out.println("版本:" + pd.getVersion());// 当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("bpmn文件:" + pd.getResourceName());
                System.out.println("png文件:" + pd.getDiagramResourceName());
                System.out.println("部署对象ID：" + pd.getDeploymentId());
                System.out.println("是否暂停：" + pd.isSuspended());
                System.out.println("#########################################################");
            }
        } else {
            System.out.println("没有流程正在运行。");
        }
    }

























    /**
     * 添加报销
     *
     * @param userId    用户Id
     * @param money     报销金额
     * @param descption 描述
     */
    @RequestMapping(value = "add")
    @ResponseBody
    public String addExpense(String userId, Integer money, String descption) {
        //启动流程
        HashMap<String, Object> map = new HashMap<>();
        map.put("taskUser", userId);
        map.put("money", money);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Expense", map);
        return "提交成功.流程Id为：" + processInstance.getId();
    }


    /**
     * 获取审批管理列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(String userId) {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
        for (Task task : tasks) {
        }
        System.out.println(tasks.toString());

        return tasks.toString();
    }


    /**
     * 批准
     *
     * @param taskId 任务ID
     */
    @RequestMapping(value = "apply")
    @ResponseBody
    public String apply(String taskId) {
        List<Task> t = taskService.createTaskQuery().list();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (task == null) {
            System.out.println("流程不存在");
        }
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "通过");
        taskService.complete(taskId, map);
        return "processed ok!";
    }

    /**
     * 拒绝
     */
    @ResponseBody
    @RequestMapping(value = "reject")
    public String reject(String taskId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "驳回");
        taskService.complete(taskId, map);
        return "reject";
    }

    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @RequestMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        List<ProcessInstance> t = runtimeService.createProcessInstanceQuery().list();
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();


        //流程走完的不显示图
        if (pi == null) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
//        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0);
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, Collections.emptyList(),engconf.getActivityFontName(),engconf.getLabelFontName(),engconf.getAnnotationFontName(),null,1.0, false);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

}

class UserProcessModel
{
    /** 流程定义ID */
    private String processId;

    /** 流程名称 */
    private String processName;

    /** key */
    private String processKey;

    /** 版本 */
    private String processVersion;

    /** BPMN文件路径 */
    private String resourceName;

    /** PNG文件路径 */
    private String diagramResourceName;

    /** 部署对象ID */
    private String deploymentId;

    /** 是否暂停 */
    private String isSuspendedatus;


    public UserProcessModel()
    {

    }

    public UserProcessModel(String processId, String processName, String processKey, String processVersion, String resourceName,
                          String diagramResourceName, String deploymentId, String isSuspendedatus)
    {
        this.processId = processId;
        this.processName = processName;
        this.processKey = processKey;
        this.processVersion = processVersion;
        this.resourceName = resourceName;
        this.diagramResourceName = diagramResourceName;
        this.deploymentId = deploymentId;
        this.isSuspendedatus = isSuspendedatus;
    }

    public String getProcessId()
    {
        return processId;
    }

    public void setProcessId(String processId)
    {
        this.processId = processId;
    }


    public String getProcessName()
    {
        return processName;
    }

    public void setProcessName(String processName)
    {
        this.processName = processName;
    }



    public String getProcessKey()
    {
        return processKey;
    }

    public void setProcessKey(String processKey)
    {
        this.processKey = processKey;
    }



    public String getProcessVersion()
    {
        return processVersion;
    }

    public void setProcessVersion(String processVersion)
    {
        this.processVersion = processVersion;
    }



    public String getResourceName()
    {
        return resourceName;
    }

    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
    }



    public String getDiagramResourceName()
    {
        return diagramResourceName;
    }

    public void setDiagramResourceName(String diagramResourceName)
    {
        this.diagramResourceName = diagramResourceName;
    }



    public String getDeploymentId()
    {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId)
    {
        this.deploymentId = deploymentId;
    }



    public String getIsSuspendedatus()
    {
        return isSuspendedatus;
    }

    public void setIsSuspendedatus(String isSuspendedatus)
    {
        this.isSuspendedatus = isSuspendedatus;
    }
    
}

