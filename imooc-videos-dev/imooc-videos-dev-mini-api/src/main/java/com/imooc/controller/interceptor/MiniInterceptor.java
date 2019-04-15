package com.imooc.controller.interceptor;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.druid.support.json.JSONUtils;
import com.imooc.utils.IMoocJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;

public class MiniInterceptor implements HandlerInterceptor {
	@Autowired
	private RedisOperator redis;
	
	public static final String USER_REDIS_SESSION="user-redis-session";
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
		
		String userId=request.getHeader("userId");
		String userToken=request.getHeader("userToken");
	
		if(!StringUtils.isBlank(userId) && !StringUtils.isBlank(userToken)){
			String uniqueToken=redis.get(USER_REDIS_SESSION+":"+userId);
			if(StringUtils.isEmpty(uniqueToken)||StringUtils.isBlank(uniqueToken)){
				//登陆过期或未登录
				returnErrorResponse(response, new IMoocJSONResult().errorTokenMsg("请登录"));
				return false;
			}else{
				if(!uniqueToken.equals(userToken)){
					//账号在 另一台手机上登陆
					returnErrorResponse(response, new IMoocJSONResult().errorTokenMsg("在其他设备登陆"));
					return false;
				}
			}
		}else{
			returnErrorResponse(response, new IMoocJSONResult().errorTokenMsg("请登录"));
		}
		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void returnErrorResponse(HttpServletResponse response,IMoocJSONResult result) throws IOException{
		OutputStream out=null;
		try{
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/json");
			out=response.getOutputStream();
			out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
			out.flush();
		}finally{
			if(out!=null){
				out.close();
			}
		}
	}



}
