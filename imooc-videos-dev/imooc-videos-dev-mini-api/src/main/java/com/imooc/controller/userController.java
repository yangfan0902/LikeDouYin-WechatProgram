package com.imooc.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imooc.pojo.Users;
import com.imooc.pojo.vo.PublisherVideo;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.IMoocJSONResult;
import com.imooc.utils.MD5Utils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "用户相关业务的接口", tags = { "用户相关业务的controller" })
@RequestMapping("/user")
public class userController extends BasicController{
	@Autowired
	private UserService userService;

	@ApiOperation(value = "用户上传头像", notes = "用户上传头像")
	@PostMapping("/uploadFace")
	public IMoocJSONResult uploadFace(String userId,@RequestParam("file") MultipartFile[] files) throws IOException{
		if(StringUtils.isBlank(userId)){
			return IMoocJSONResult.errorMsg("用户id不能为空");
		}
		
		//文件保存的命名空间
		String fileSpace="D:/imooc_videos_dev";
		//保存到数据库中的相对路径
		String uploadPathDB="/"+userId+"/face";
		
		FileOutputStream fileOutputStream=null;
		InputStream inputStream=null;
		
		try {
			if(files!=null && files.length>0){
				String fileName=files[0].getOriginalFilename();
				if(StringUtils.isNoneBlank(fileName)){
					String finalFacePath=fileSpace+uploadPathDB+"/"+fileName;
					uploadPathDB+=("/"+fileName);
					File outFile=new File(finalFacePath);
					if(outFile.getParentFile()!=null||!outFile.getParentFile().isDirectory()){
						outFile.getParentFile().mkdirs();
					}
					fileOutputStream=new FileOutputStream(outFile);
					inputStream = files[0].getInputStream();
					IOUtils.copy(inputStream, fileOutputStream);
				}
			}else{
				return IMoocJSONResult.errorMsg("上传出错");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return IMoocJSONResult.errorMsg("上传出错");
		}finally {
			if(fileOutputStream!=null){
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}
		
		Users user=new Users();
		user.setId(userId);
		user.setFaceImage(uploadPathDB);
		
		userService.updateUserInfo(user);
		
		return IMoocJSONResult.ok(uploadPathDB);
	
	}
	
	@ApiOperation(value = "用户信息查询", notes = "用户信息查询")
	@ApiImplicitParam(name="userId",value="用户id",required=true,paramType="query")
	@PostMapping("/query")
	public IMoocJSONResult query(String userId,String fanId){
		if(StringUtils.isBlank(userId)){
			return IMoocJSONResult.errorMsg("用户id不能为空");
		}
		Users user=userService.queryUserInfo(userId);
		UsersVO userVO=new UsersVO();
		BeanUtils.copyProperties(user, userVO);
		userService.queryIfFollow(userId, fanId);
		userVO.setFollow(userService.queryIfFollow(userId, fanId));
		return IMoocJSONResult.ok(userVO);
		
	
	}
	

	
	@PostMapping("/queryPublisher")
	public IMoocJSONResult queryPublisher(String loginUserId,String videoId,String publishUserId){
		if(StringUtils.isBlank(publishUserId)){
			return IMoocJSONResult.errorMsg("");
		}
		//1.查询视频发布者的信息
		Users userInfo=userService.queryUserInfo(publishUserId);
		UsersVO publisher=new UsersVO();
		BeanUtils.copyProperties(userInfo, publisher);
		
		//2.查询当前登陆者和视频点赞关系
		boolean userLikeVideo=userService.isUserLikeVideo(loginUserId, videoId);
	
		PublisherVideo bean=new PublisherVideo();
		bean.setPublisher(publisher);
		bean.setUserLikeVideo(userLikeVideo);
		
		return IMoocJSONResult.ok(bean);
	}
	
	@PostMapping("/beyourfans")
	public IMoocJSONResult beyourfans(String userId,String fanId){
		if(StringUtils.isBlank(userId)||StringUtils.isBlank(fanId)){
			return IMoocJSONResult.errorMsg("");
		}
		userService.saveUserFanRelation(userId, fanId);

		return IMoocJSONResult.ok("关注成功");
		
	
	}
	
	@PostMapping("/dontbeyourfans")
	public IMoocJSONResult dontbeyourfans(String userId,String fanId){
		if(StringUtils.isBlank(userId)||StringUtils.isBlank(fanId)){
			return IMoocJSONResult.errorMsg("");
		}
		userService.deleteUserFanRelation(userId, fanId);

		return IMoocJSONResult.ok("取消关注成功");
		
	
	}
}
