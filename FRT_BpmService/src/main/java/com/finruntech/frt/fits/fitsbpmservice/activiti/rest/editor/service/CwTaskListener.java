package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.service;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.util.HttpClient3;
import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.util.SysConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;



@SuppressWarnings("serial")
public class CwTaskListener implements TaskListener{
	
	@Override
	public void notify(DelegateTask delegateTask) {
		HttpClient3 h=new HttpClient3();
		try {
			String result=h.doGet(SysConfig.getProperty("auth_fihead_url"), "");
			if(!"".equals(result)){
				JSONArray ja=JSON.parseArray(result);
				for(int i=0;i<ja.size();i++){
					JSONObject jo=JSON.parseObject(ja.get(i).toString());
					delegateTask.addCandidateUser(jo.getString("uId"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
