package cn.itcast.ssh.utils;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;

public class DynamicBpmn {

	private RepositoryService repositoryService;
	
	private RuntimeService runtimeService;
	
	private TaskService taskService;
	
	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public RuntimeService getRuntimeService() {
		return runtimeService;
	}

	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	private BpmnModel createBpmnModel(){
		//创建bpmn模型
		BpmnModel model = new BpmnModel();
		Process process = new Process();
		model.addProcess(process);
		process.setId("my-process");
		process.setName("my-process");
		  
		//创建bpmn元素
		process.addFlowElement(createStartEvent());
		process.addFlowElement(createUserTask("task1", "First task", "fred"));
		process.addFlowElement(createUserTask("task2", "Second task", "john"));
		process.addFlowElement(createEndEvent());

		process.addFlowElement(createSequenceFlow("start", "task1"));
		process.addFlowElement(createSequenceFlow("task1", "task2"));
		process.addFlowElement(createSequenceFlow("task2", "end"));

		// 生成BPMN自动布局
		new BpmnAutoLayout(model).execute();
		return model;
	}
	
	public void deploy() throws Exception{
		BpmnModel model = createBpmnModel();
		// 部署这个BPMN模型
		Process process = model.getMainProcess();
		String processId=process.getId();
		String processName=process.getName();
		String bpmnName = processId+".bpmn";
		Deployment deployment = repositoryService.createDeployment()
				.addBpmnModel(bpmnName, model).name(processName).deploy();
		
		// 保存bpmn流程图
		ProcessDefinition pd=repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).singleResult();
		InputStream processDiagram = repositoryService.getProcessDiagram(pd.getId());
		String path = "E:/"+File.separator+"bpmn";
		File pngFile = new File(path+File.separator+processId+".png");
		FileUtils.copyInputStreamToFile(processDiagram, pngFile);
		
		// 保存为bpmn文件
		InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), bpmnName);
		File bpmnFile = new File(path+File.separator+bpmnName);
		FileUtils.copyInputStreamToFile(processBpmn,bpmnFile);
	}
	
	public void test() throws Exception {
		deploy();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
		Assert.assertEquals(1, tasks.size());
		Assert.assertEquals("First task", tasks.get(0).getName());
		Assert.assertEquals("fred", tasks.get(0).getAssignee());	  
	}

	//创建task
	protected UserTask createUserTask(String id, String name, String assignee) {
		UserTask userTask = new UserTask();
		userTask.setName(name);
		userTask.setId(id);
		userTask.setAssignee(assignee);
		return userTask;
	}

	//创建箭头
	protected SequenceFlow createSequenceFlow(String from, String to) {
		SequenceFlow flow = new SequenceFlow();
		flow.setSourceRef(from);
		flow.setTargetRef(to);
		return flow;
	}

	protected StartEvent createStartEvent() {
		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");
		return startEvent;
	}

	protected EndEvent createEndEvent() {
		EndEvent endEvent = new EndEvent();
		endEvent.setId("end");
		return endEvent;
	}

}
