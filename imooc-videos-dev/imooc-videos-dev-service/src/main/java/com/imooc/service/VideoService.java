package com.imooc.service;

import java.util.List;

import com.imooc.pojo.Videos;
import com.imooc.utils.PagedResult;

public interface VideoService {
	//保存视频
	public String saveVideo(Videos video);
	
	//更新视频封面路径
	public void updateViedo(String videoId,String coverPath);
	
	public PagedResult getAllVideos(Videos video,Integer isSaveRecord,Integer page,Integer pageSize);

	public List<String> getHotWords();
	
	/**
	 * 用户点赞视频
	 * @param userId
	 * @param videoId
	 * @param videoCreaterId
	 */
	public void userLikeVideo(String userId,String videoId,String videoCreaterId);
	
	/**
	 * 用户取消点赞视频
	 * @param userId
	 * @param videoId
	 * @param videoCreaterId
	 */
	public void userUnLikeVideo(String userId,String videoId,String videoCreaterId);
}
