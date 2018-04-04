package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sf.json.JSONArray;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ModelCreateController {

	private Logger logger = LoggerFactory.getLogger(ModelCreateController.class);

	@Autowired
	private RepositoryService repositoryService;

	@RequestMapping(value = "create")
	public void create(@RequestParam("name") String name, @RequestParam("key") String key,
			@RequestParam("description") String description, HttpServletRequest request, HttpServletResponse response) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode editorNode = objectMapper.createObjectNode();
			editorNode.put("id", "canvas");
			editorNode.put("resourceId", "canvas");
			ObjectNode stencilSetNode = objectMapper.createObjectNode();
			stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
			editorNode.put("stencilset", stencilSetNode);
			Model modelData = repositoryService.newModel();

			ObjectNode modelObjectNode = objectMapper.createObjectNode();
			modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
			modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
			description = StringUtils.defaultString(description);
			modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
			modelData.setMetaInfo(modelObjectNode.toString());
			modelData.setName(name);
			modelData.setKey(StringUtils.defaultString(key));

			repositoryService.saveModel(modelData);
			repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));

			// response.sendRedirect(request.getContextPath() +
			// "/modeler.html?modelId=" + modelData.getId());
		} catch (Exception e) {
			logger.error("创建模型失败：", e);
		}
	}
	
	@RequestMapping(value = "deleteModel")
	@ResponseBody
	public ResponseEntity<?> deleteModel(@RequestParam("modelId") String modelId) {
		try {

			Model modelData = repositoryService.getModel(modelId);
			
			if(modelData.getDeploymentId()!=null){
				repositoryService.deleteDeployment(modelData.getDeploymentId(), true);
			}

			repositoryService.deleteModel(modelId);

			return ResponseEntity.ok().body(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok().body(2);
	}
	
	@RequestMapping(value = "cleanModel")
	@ResponseBody
	public ResponseEntity<?> cleanModel(@RequestParam("modelId") String modelId) {
		try {

			Model modelData = repositoryService.getModel(modelId);

			repositoryService.deleteDeployment(modelData.getDeploymentId(), true);

			return ResponseEntity.ok().body(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok().body(2);
	}

	/***
	 * 查询设计模板
	 */
	@RequestMapping(value = "queryDesignModel")
	@ResponseBody
	public ResponseEntity<?> appravolOver(HttpServletRequest request, HttpServletResponse response) {
		// 把传送过来的JSON字符串转成JSON数组
		JSONArray ja = JSONArray.fromObject(request.getParameter("aodata"));

		int startRow = 0;
		int pageSize = 10;
		for (int i = 0; i < ja.size(); i++) {
			if (ja.getJSONObject(i).getString("name").equals("iDisplayStart")) {
				startRow = Integer.valueOf(ja.getJSONObject(i).getString("value"));
			}
			if (ja.getJSONObject(i).getString("name").equals("iDisplayLength")) {
				pageSize = Integer.valueOf(ja.getJSONObject(i).getString("value"));
			}
		}

		Long totalCount = repositoryService.createModelQuery().count();
		List<Model> modelList = repositoryService.createModelQuery().orderByCreateTime().desc().listPage(startRow,
				pageSize);

		if (!modelList.isEmpty()) {
			for (Model m : modelList) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dateStr = sdf.format(m.getCreateTime());
				m.setMetaInfo(dateStr);

				String date2Str = sdf.format(m.getLastUpdateTime());
				m.setTenantId(date2Str);
			}
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("recordsTotal", modelList.size());// 本次加载记录数量
		map.put("recordsFiltered", totalCount);// 总记录数量
		map.put("data", modelList);

		return ResponseEntity.ok().body(map);
	}

	// /***
	// * 部署模板
	// */
	// @RequestMapping(value = "deployModel")
	// @ResponseBody
	// public ResponseEntity<?> deployModel(@RequestParam("dirver") String
	// dirver,@RequestParam("url") String url,
	// @RequestParam("userName") String userName,@RequestParam("password")
	// String password,@RequestParam("modelId") String modelId) {
	// Deployment deployment = null;
	// try {
	// if("".equals(dirver)||"".equals(url)||"".equals(userName)||"".equals(password))return
	// ResponseEntity.ok().body(2);
	// // 获取设计流程
	// ProcessEngine design = getProcessEngines("com.mysql.jdbc.Driver",
	// "jdbc:mysql://localhost:3306/bpmdb?useUnicode=true&amp;characterEncoding=utf8",
	// "root", "root");
	// RepositoryService repositoryService = design.getRepositoryService();
	//
	// Model modelData = repositoryService.getModel(modelId);
	// ObjectNode modelNode = (ObjectNode) new ObjectMapper()
	// .readTree(repositoryService.getModelEditorSource(modelData.getId()));
	//
	// // 部署到指定的数据服务器
	// ProcessEngine processEngine = getProcessEngines(dirver,url, userName,
	// password);
	// RepositoryService deployService = processEngine.getRepositoryService();
	//
	// byte[] bpmnBytes = null;
	// BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
	// bpmnBytes = new BpmnXMLConverter().convertToXML(model);
	// String processName = modelData.getName() + ".bpmn20.xml";
	// deployment = deployService.createDeployment().name(modelData.getName())
	// .addString(processName, new String(bpmnBytes)).deploy();
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return ResponseEntity.ok().body(1);
	// }

	/***
	 * 部署模板
	 */
	@RequestMapping(value = "deployModel")
	@ResponseBody
	public ResponseEntity<?> deployModel(@RequestParam("modelId") String modelId) {
		Deployment deployment = null;
		try {
			Model modelData = repositoryService.getModel(modelId);
			ObjectNode modelNode = (ObjectNode) new ObjectMapper()
					.readTree(repositoryService.getModelEditorSource(modelData.getId()));

			byte[] bpmnBytes = null;
			BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
			bpmnBytes = new BpmnXMLConverter().convertToXML(model);
			String processName = modelData.getName() + ".bpmn20.xml";
			deployment = repositoryService.createDeployment().name(modelData.getName())
					.addString(processName, new String(bpmnBytes)).deploy();

			modelData.setDeploymentId(deployment.getId());
			repositoryService.saveModel(modelData);

			return ResponseEntity.ok().body(1);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok().body(2);
	}

	/**
	 * 获取流程引擎
	 * 
	 * @param jdbcDriver
	 *            驱动
	 * @param jdbcUrl
	 *            地址
	 * @param jdbcUsername
	 *            用户名
	 * @param jdbcPassword
	 *            密码
	 * @return
	 */
	public ProcessEngine getProcessEngines(String jdbcDriver, String jdbcUrl, String jdbcUsername,
			String jdbcPassword) {
		// 流程对象
		ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
				.createStandaloneProcessEngineConfiguration();
		// Jdbc配置
		processEngineConfiguration.setJdbcDriver(jdbcDriver);
		processEngineConfiguration.setJdbcUrl(jdbcUrl);
		processEngineConfiguration.setJdbcUsername(jdbcUsername);
		processEngineConfiguration.setJdbcPassword(jdbcPassword);
		processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		return processEngineConfiguration.buildProcessEngine();
	}
}
