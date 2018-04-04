package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.dto.HistoryResultDto;
import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.dto.ProcessParamDto;
import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.dto.ResultDto;
import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.dto.ReturnJsonDto;
import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.enums.ProcessStatus;
import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.enums.StatusType;
import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.util.JsonUtil;

@RestController
public class ProcessController {

	private Logger logger = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private TaskService taskService;

	@RequestMapping(value = "startProcess")
	@ResponseBody
	public ResponseEntity<?> startProcess(@RequestBody String Data) {
		ProcessParamDto pageDto = JsonUtil.fromJson(Data, ProcessParamDto.class);
		String processInstanceId = "";
		String taskId = "";
		String userId = pageDto.getUserId();
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("userId", userId);
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(pageDto.getKey(), variables);
		processInstanceId = pi.getId();
		
		Task tt = null;
		if(userId==null||"".equals(userId)){
			tt=taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
		}else{
			tt=taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(userId).singleResult();
		}
		
		if (tt != null) {
			taskId = tt.getId();

			Map<String, Object> variablesM = new HashMap<String, Object>();
			if (pageDto.getVariables() != null && !"".equals(pageDto.getVariables())) {
				Map<String, Object> params = JsonUtil.jsonToMap(pageDto.getVariables());

				for (Map.Entry<String, Object> entry : params.entrySet()) {
					variablesM.put(entry.getKey(), entry.getValue());
				}
			}
			taskService.complete(taskId, variablesM);
		}

		ResultDto resultDto = new ResultDto();
		ReturnJsonDto returnDto = null;
		boolean f = queryProcessState(processInstanceId);
		if (f) {// 结束
			resultDto.setProcessStatus(ProcessStatus.off.getCode());
			resultDto.setProcessId(processInstanceId);
			resultDto.setProcessNextNode(getNextNode(processInstanceId));
			returnDto = new ReturnJsonDto(StatusType.Sucess.getCode(), resultDto, "process end");

			return ResponseEntity.ok().body(returnDto);
		} else {// 未结束
			resultDto.setProcessStatus(ProcessStatus.on.getCode());
			resultDto.setProcessId(processInstanceId);
			resultDto.setProcessNextNode(getNextNode(processInstanceId));
			returnDto = new ReturnJsonDto(StatusType.Sucess.getCode(), resultDto, "process continue");

			return ResponseEntity.ok().body(returnDto);
		}
	}

	@RequestMapping(value = "completeProcess")
	@ResponseBody
	public ResponseEntity<?> completeProcess(@RequestBody String Data) {
		ProcessParamDto pageDto = JsonUtil.fromJson(Data, ProcessParamDto.class);
		String taskId = "";

		if (pageDto.getFlag() == null && pageDto.getProcessId() == null)
			return ResponseEntity.ok()
					.body(new ReturnJsonDto(StatusType.Fail.getCode(), null, "flag or processId is null"));

		List<Task> tt = taskService.createTaskQuery().processInstanceId(pageDto.getProcessId()).list();
		// Task tt =
		// taskService.createTaskQuery().processInstanceId(pageDto.getProcessId()).singleResult();
		if (tt != null)
			taskId = tt.get(0).getId();

		// 记录到历史变量表提供历史查询
		setHisVarForHisQue(taskId, pageDto);
		// 设置执行人
		taskService.setAssignee(taskId, pageDto.getUserId());
		// 设置操作和意见
		Map<String, Object> variablesM = new HashMap<String, Object>();
		if (pageDto.getFlag() != null && !"".equals(pageDto.getFlag()))
			variablesM.put("flag", pageDto.getFlag());
		if (pageDto.getOption() != null && !"".equals(pageDto.getOption()))
			variablesM.put("option", pageDto.getOption());
		// 设置其他变量
		if (pageDto.getVariables() != null && !"".equals(pageDto.getVariables())) {
			Map<String, Object> params = JsonUtil.jsonToMap(pageDto.getVariables());
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				variablesM.put(entry.getKey(), entry.getValue());
			}
		}
		taskService.complete(taskId, variablesM);

		ResultDto resultDto = new ResultDto();
		ReturnJsonDto returnDto = null;
		boolean f = queryProcessState(pageDto.getProcessId());
		if (f) {// 结束
			resultDto.setProcessStatus(ProcessStatus.off.getCode());
			resultDto.setProcessId(pageDto.getProcessId());
			resultDto.setProcessNextNode(getNextNode(pageDto.getProcessId()));
			returnDto = new ReturnJsonDto(StatusType.Sucess.getCode(), resultDto, "process end");

			return ResponseEntity.ok().body(returnDto);
		} else {// 未结束
			resultDto.setProcessStatus(ProcessStatus.on.getCode());
			resultDto.setProcessId(pageDto.getProcessId());
			resultDto.setProcessNextNode(getNextNode(pageDto.getProcessId()));
			returnDto = new ReturnJsonDto(StatusType.Sucess.getCode(), resultDto, "process continue");

			return ResponseEntity.ok().body(returnDto);
		}
	}

	@RequestMapping(value = "completeNextStep")
	@ResponseBody
	public ResponseEntity<?> completeNextStep(@RequestBody String Data) {
		ProcessParamDto pageDto = JsonUtil.fromJson(Data, ProcessParamDto.class);
		String taskId = "";

		if (pageDto.getFlag() == null && pageDto.getProcessId() == null)
			return ResponseEntity.ok()
					.body(new ReturnJsonDto(StatusType.Fail.getCode(), null, "flag or processId is null"));

		List<Task> tt = taskService.createTaskQuery().processInstanceId(pageDto.getProcessId()).list();
		// Task tt =
		// taskService.createTaskQuery().processInstanceId(pageDto.getProcessId()).singleResult();
		if (tt != null)
			taskId = tt.get(0).getId();

		// 设置执行人
		taskService.setAssignee(taskId, pageDto.getUserId());
		// 设置操作和意见
		Map<String, Object> variablesM = new HashMap<String, Object>();
		// 设置其他变量
		if (pageDto.getVariables() != null && !"".equals(pageDto.getVariables())) {
			Map<String, Object> params = JsonUtil.jsonToMap(pageDto.getVariables());
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				variablesM.put(entry.getKey(), entry.getValue());
			}
		}
		taskService.complete(taskId, variablesM);

		ResultDto resultDto = new ResultDto();
		ReturnJsonDto returnDto = null;
		boolean f = queryProcessState(pageDto.getProcessId());
		if (f) {// 结束
			resultDto.setProcessStatus(ProcessStatus.off.getCode());
			resultDto.setProcessId(pageDto.getProcessId());
			resultDto.setProcessNextNode(getNextNode(pageDto.getProcessId()));
			returnDto = new ReturnJsonDto(StatusType.Sucess.getCode(), resultDto, "process end");

			return ResponseEntity.ok().body(returnDto);
		} else {// 未结束
			resultDto.setProcessStatus(ProcessStatus.on.getCode());
			resultDto.setProcessId(pageDto.getProcessId());
			resultDto.setProcessNextNode(getNextNode(pageDto.getProcessId()));
			returnDto = new ReturnJsonDto(StatusType.Sucess.getCode(), resultDto, "process continue");

			return ResponseEntity.ok().body(returnDto);
		}
	}

	@RequestMapping(value = "hisQuery")
	@ResponseBody
	public ResponseEntity<?> hisQuery(@RequestBody String Data) {
		ProcessParamDto pageDto = JsonUtil.fromJson(Data, ProcessParamDto.class);

		List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId(pageDto.getProcessId()).finished().orderByTaskCreateTime().asc().list();

		List<HistoryResultDto> ll = new ArrayList<HistoryResultDto>();
		if (!htiList.isEmpty()) {
			for (int i = 0; i < htiList.size(); i++) {
				HistoryResultDto historyResultDto = new HistoryResultDto();
				historyResultDto.setUserId(htiList.get(i).getAssignee());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				historyResultDto.setApprovalTime(sdf.format(htiList.get(i).getEndTime()));
				historyResultDto.setApprovalStep(htiList.get(i).getName());

				List<HistoricVariableInstance> hlist = historyService.createHistoricVariableInstanceQuery()
						.taskId(htiList.get(i).getId()).list();
				if (!hlist.isEmpty()) {
					for (HistoricVariableInstance v : hlist) {
						if ("flag".equals(v.getVariableName())) {
							historyResultDto.setApprovalResult(v.getValue() + "");
						}
						if ("Opinion".equals(v.getVariableName())) {
							historyResultDto.setOpinion(v.getValue() + "");
						}
					}
				}

				ll.add(historyResultDto);
			}
		}
		return ResponseEntity.ok().body(ll);
	}

	private String getNextNode(String processInstanceId) {
		List<HistoricActivityInstance> lista = historyService.createHistoricActivityInstanceQuery()//
				.processInstanceId(processInstanceId)//
				.unfinished()// 未完成的活动(任务)
				.list();
		String result = "";

		if (!lista.isEmpty()) {
			for (int i = 0; i < lista.size(); i++) {
				result += lista.get(i).getActivityName() + ",";
			}
		}
		if (!"".equals(result)) {
			result = result.substring(0, result.lastIndexOf(","));
		}
		return result;
	}

	/**
	 * 记录到历史变量表提供历史查询
	 * 
	 * @param taskId
	 * @param ppDto
	 */
	private void setHisVarForHisQue(String taskId, ProcessParamDto ppDto) {
		taskService.setVariableLocal(taskId, "flag", ppDto.getFlag());
		taskService.setVariableLocal(taskId, "Opinion", ppDto.getOption());
	}

	/**
	 * 审批状态查询
	 * 
	 * @param processInstanceId
	 *            流程ID
	 * @return
	 */
	private boolean queryProcessState(String processInstanceId) {
		ProcessInstance piState = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
				.singleResult();
		if (piState != null) {
			return false;
		} else {
			return true;
		}
	}
}
