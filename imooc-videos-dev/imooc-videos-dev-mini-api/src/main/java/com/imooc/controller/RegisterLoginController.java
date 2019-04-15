package com.imooc.controller;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.IMoocJSONResult;
import com.imooc.utils.MD5Utils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "用戶注冊登陸的接口", tags = { "注冊和登陸的controller" })
public class RegisterLoginController extends BasicController{
	@Autowired
	private UserService userService;

	@ApiOperation(value = "用戶注冊", notes = "用戶注冊")
	@PostMapping("/regist")
	public IMoocJSONResult regist(@RequestBody Users user) throws Exception {
		// 判断用户名是否为空
		if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
			return IMoocJSONResult.errorMsg("用户名或密码不能为空");
		}
		// 判断用户名是否存在
		boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());

		// 保存
		if (!usernameIsExist) {
			user.setNickname(user.getUsername());
			user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
			user.setFansCounts(0);
			user.setFollowCounts(0);
			user.setReceiveLikeCounts(0);
			userService.saveUser(user);
		} else {
			return IMoocJSONResult.errorMsg("用户名已存在");
		}
		user.setPassword("");
		UsersVO userVO=setUserRedisSessionToken(user);
		return IMoocJSONResult.ok(userVO);
	}
	
	public UsersVO setUserRedisSessionToken(Users user){
		String uniqueToken=UUID.randomUUID().toString();
		redis.set(USER_REDIS_SESSION + ":"+ user.getId() , uniqueToken,1000*60*30);
		UsersVO userVO=new UsersVO();
		BeanUtils.copyProperties(user, userVO);
		userVO.setUserToken(uniqueToken);
		return userVO;
	}
	
	
	@ApiOperation(value="用户登录",notes="用户登录")
	@PostMapping("/login")
	public IMoocJSONResult login(@RequestBody Users user){
		//判断用户名是否为空
		if(StringUtils.isBlank(user.getUsername())||StringUtils.isBlank(user.getPassword())){
			return IMoocJSONResult.errorMsg("用户名或密码不能为空");
		}
		boolean userIsExist=userService.queryUserIsExist(user);
		
		
		if(userIsExist){
			user.setPassword("");
			UsersVO userVO=setUserRedisSessionToken(user);
			return IMoocJSONResult.ok(userVO);
		}else{
			return IMoocJSONResult.errorMsg("用户名或密码错误");
		}
	}
	
	@ApiOperation(value="用户注销",notes="用户注销")
	@ApiImplicitParam(name="userId",value="用户id",required=true,dataType="String",paramType="query")
	@PostMapping("/logout")
	public IMoocJSONResult logout(String userId){
		redis.del(USER_REDIS_SESSION+":"+userId);
		return null;
	}
}
