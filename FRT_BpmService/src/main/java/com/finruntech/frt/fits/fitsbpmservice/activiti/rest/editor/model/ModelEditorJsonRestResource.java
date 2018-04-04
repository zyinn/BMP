/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.model;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sf.json.JSONObject;

/**
 * @author Tijs Rademakers
 */
@RestController
public class ModelEditorJsonRestResource implements ModelDataJsonConstants {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(ModelEditorJsonRestResource.class);
  
  @Autowired
  private RepositoryService repositoryService;
  
  @Autowired
  private ObjectMapper objectMapper;
  
  @RequestMapping(value="/model/{modelId}/json", method = RequestMethod.GET, produces = "application/json")
  public ObjectNode getEditorJson(@PathVariable String modelId) {
	  ObjectNode modelNode = null;

		Model model = repositoryService.getModel(modelId);

		if (model != null) {
			try {
				if (StringUtils.isNotEmpty(model.getMetaInfo())) {
					modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
				} else {
					modelNode = objectMapper.createObjectNode();
					modelNode.put(MODEL_NAME, model.getName());
				}
				modelNode.put(MODEL_ID, model.getId());
				ObjectNode editorJsonNode = (ObjectNode) objectMapper
						.readTree(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));

				if (editorJsonNode != null) {
					JSONObject jb = JSONObject.fromObject(editorJsonNode.toString());
					JSONObject jb2 = JSONObject.fromObject(jb.get("properties"));
					if (!jb2.isNullObject()) {
						jb2.put("process_id", model.getKey());
						jb.put("properties", jb2);

						ObjectMapper mapper = new ObjectMapper();
						// JSON ----> JsonNode
						JsonNode rootNode = mapper.readTree(jb.toString());
						modelNode.put("model", rootNode);
					} else {
						String jsonStr = "{\"process_id\":\"" + model.getKey()
								+ "\",\"name\":\"\",\"documentation\":\"\",\"process_author\":\"\",\"process_version\":\"\",\"process_namespace\":\"http://www.activiti.org/processdef\",\"executionlisteners\":\"\",\"eventlisteners\":\"\",\"signaldefinitions\":\"\",\"messagedefinitions\":\"\"}";
						JSONObject aa = JSONObject.fromObject(jsonStr);

						jb.put("properties", aa);
						ObjectMapper mapper = new ObjectMapper();
						// JSON ----> JsonNode
						JsonNode rootNode = mapper.readTree(jb.toString());
						modelNode.put("model", rootNode);
					}
				} else {
					modelNode.put("model", editorJsonNode);
				}

			} catch (Exception e) {
				LOGGER.error("Error creating model JSON", e);
				throw new ActivitiException("Error creating model JSON", e);
			}
		}

		return modelNode;
  }
}
