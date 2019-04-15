package com.imooc.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imooc.enums.VideoStatusEnum;
import com.imooc.pojo.Bgm;
import com.imooc.pojo.Comments;
import com.imooc.pojo.Users;
import com.imooc.pojo.Videos;
import com.imooc.service.BgmService;
import com.imooc.service.UserService;
import com.imooc.service.VideoService;
import com.imooc.utils.FetchRideoCover;
import com.imooc.utils.IMoocJSONResult;
import com.imooc.utils.MergeVideoMp3;
import com.imooc.utils.PagedResult;
import com.imooc.utils.RedisOperator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(value = "视频相关业务的接口", tags = { "视频相关业务的controller" })
@RequestMapping("/video")
public class VeidoController extends BasicController {
	@Autowired
	private VideoService videoService;

	@Autowired
	private BgmService bgmService;

	@ApiOperation(value = "用户上传视频", notes = "用户上传视频")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "userId", value = "用户id", required = true, dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "bgmId", value = "背景音乐id", required = false, dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "vedioSeconds", value = "视频时长", required = true, dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "vedioWidth", value = "视频宽度", required = true, dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "vedioHeight", value = "视频高度", required = true, dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "desc", value = "视频描述", required = false, dataType = "String", paramType = "form"), })
	@PostMapping(value = "/upload", headers = "content-type=multipart/form-data")
	public IMoocJSONResult upload(String userId, String bgmId, double videoSeconds, int videoWidth, int videoHeight,
			String desc, @ApiParam(value = "短视频", required = true) MultipartFile file) throws Exception {
		if (StringUtils.isBlank(userId)) {
			return IMoocJSONResult.errorMsg("用户id不能为空");
		}

		// 保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/video";
		String coverPathDB = "/" + userId + "/video";
		String finalVedioPath = "";
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;

		try {
			if (file != null) {
				String fileName = file.getOriginalFilename();
				String fileNamePrefix = fileName.split("\\.")[0];
				if (StringUtils.isNoneBlank(fileName)) {
					finalVedioPath = FILE_SPACE + uploadPathDB + "/" + fileName;
					uploadPathDB += ("/" + fileName);
					coverPathDB += ("/" + fileNamePrefix + ".jpg");
					File outFile = new File(finalVedioPath);
					if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						outFile.getParentFile().mkdirs();
					}
					fileOutputStream = new FileOutputStream(outFile);
					inputStream = file.getInputStream();
					IOUtils.copy(inputStream, fileOutputStream);
				}
			} else {
				return IMoocJSONResult.errorMsg("上传出错");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return IMoocJSONResult.errorMsg("上传出错");
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}

		// 判断bgmId是否为空，不为空则需要整合视频和bgm
		if (!StringUtils.isBlank(bgmId)) {
			Bgm bgm = bgmService.queryBgmById(bgmId);
			String mp3InputPath = FILE_SPACE + bgm.getPath();
			System.out.println(mp3InputPath);
			String videoInputPath = finalVedioPath;
			MergeVideoMp3 tool = new MergeVideoMp3(FFMPEG_EXE);
			String videoOutputName = UUID.randomUUID().toString() + ".mp4";
			uploadPathDB = "/" + userId + "/video" + "/" + videoOutputName;
			finalVedioPath = FILE_SPACE + uploadPathDB;
			System.out.println(videoInputPath);
			tool.convertor(videoInputPath, mp3InputPath, videoSeconds, finalVedioPath);
			System.out.print(finalVedioPath);
		}
		// 对视频进行截图
		FetchRideoCover tool = new FetchRideoCover(FFMPEG_EXE);
		tool.getCover(finalVedioPath, FILE_SPACE+coverPathDB);
		

		// 保存视频信息到数据库
		Videos video = new Videos();
		video.setAudioId(bgmId);
		video.setUserId(userId);
		video.setVideoDesc(desc);
		video.setVideoWidth(videoWidth);
		video.setCoverPath(coverPathDB);
		video.setVideoHeight(videoHeight);
		video.setVideoPath(uploadPathDB);
		video.setVideoSeconds((float) videoSeconds);
		video.setStatus(VideoStatusEnum.SUCCESS.value);
		video.setCreateTime(new Date());

		String videoId = videoService.saveVideo(video);
		;

		return IMoocJSONResult.ok(videoId);

	}

	@ApiOperation(value = "用户上传封面", notes = "用户上传封面")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "videoId", value = "视频id", required = true, dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "userId", value = "用户id", required = true, dataType = "String", paramType = "form"), })
	@PostMapping(value = "/uploadCover", headers = "content-type=multipart/form-data")
	public IMoocJSONResult uploadCover(@ApiParam(value = "封面", required = true) MultipartFile file, String videoId,
			String userId) throws Exception {
		if (StringUtils.isBlank(videoId)) {
			return IMoocJSONResult.errorMsg("视频id不能为空");
		}
		if (StringUtils.isBlank(userId)) {
			return IMoocJSONResult.errorMsg("用户id不能为空");
		}

		// 保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/video";

		// 保存到数据库的绝对路径
		String finalCoverPath = "";
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;

		try {
			if (file != null) {
				String fileName = file.getOriginalFilename();
				if (StringUtils.isNoneBlank(fileName)) {
					finalCoverPath = FILE_SPACE + uploadPathDB + "/" + fileName;
					uploadPathDB += ("/" + fileName);
					File outFile = new File(finalCoverPath);
					if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						outFile.getParentFile().mkdirs();
					}
					fileOutputStream = new FileOutputStream(outFile);
					inputStream = file.getInputStream();
					IOUtils.copy(inputStream, fileOutputStream);
				}
			} else {
				return IMoocJSONResult.errorMsg("上传出错");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return IMoocJSONResult.errorMsg("上传出错");
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}
		videoService.updateViedo(videoId, uploadPathDB);
		return IMoocJSONResult.ok();

	}
	
	
	@PostMapping(value = "/showAll")
	public IMoocJSONResult showAll(@RequestBody Videos video,Integer isSaveRecord,Integer page) {
		if(page==null){
			page=1;
		}
		PagedResult pagedResult=videoService.getAllVideos(video,isSaveRecord,page, PAGE_SIZE);
		return IMoocJSONResult.ok(pagedResult);
	}
	
	@PostMapping(value = "/showOnesAll")
	public IMoocJSONResult showOnesAll (String userId,Integer page) {
		if(page==null){
			page=1;
		}
		PagedResult pagedResult=videoService.getOnesVideos(userId,page, PAGE_SIZE);
		return IMoocJSONResult.ok(pagedResult);
	}
	@PostMapping(value = "/showOnesLike")
	public IMoocJSONResult showOnesLike (String userId,Integer page) {
		if(page==null){
			page=1;
		}
		PagedResult pagedResult=videoService.getOnesLikeVideos(userId,page, PAGE_SIZE);
		return IMoocJSONResult.ok(pagedResult);
	}
	@PostMapping(value = "/showOnesFollow")
	public IMoocJSONResult showOnesFollow (String userId,Integer page) {
		if(page==null){
			page=1;
		}
		PagedResult pagedResult=videoService.getOnesFollowVideos(userId,page, PAGE_SIZE);
		return IMoocJSONResult.ok(pagedResult);
	}
	
	@PostMapping(value = "/hot")
	public IMoocJSONResult hot() {
		return IMoocJSONResult.ok(videoService.getHotWords());
	}
	
	@PostMapping(value = "/userLike")
	public IMoocJSONResult userLike(String userId,String videoId,String videoCreaterId) {
		videoService.userLikeVideo(userId, videoId, videoCreaterId);
		return IMoocJSONResult.ok();
	}
	
	@PostMapping(value = "/userUnLike")
	public IMoocJSONResult userUnLike(String userId,String videoId,String videoCreaterId) {
		videoService.userUnLikeVideo(userId, videoId, videoCreaterId);
		return IMoocJSONResult.ok();
	}
	
	@PostMapping(value = "/saveComment")
	public IMoocJSONResult saveComment(@RequestBody Comments comments) {
		videoService.saveComment(comments);
		return IMoocJSONResult.ok();
	}
	
	@PostMapping(value = "/getVideoComments")
	public IMoocJSONResult getVideoComments(String videoId,Integer page) {
		if(page==null){
			page=1;
		}
		PagedResult pagedResult=videoService.getVideoComments(videoId,page, PAGE_SIZE);
		return IMoocJSONResult.ok(pagedResult);
	}
	
	

}
