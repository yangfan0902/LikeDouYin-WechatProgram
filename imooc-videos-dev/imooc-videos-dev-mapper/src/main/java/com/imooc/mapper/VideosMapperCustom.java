package com.imooc.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.imooc.pojo.Videos;
import com.imooc.pojo.vo.VideosVO;
import com.imooc.utils.MyMapper;

public interface VideosMapperCustom extends MyMapper<Videos> {
	public List<VideosVO> queryAllVideos(@Param("videoDesc") String videoDesc);
	
	/**
	 * 
	 *对喜欢的视频数量进行累加
	 * @param videoId
	 */
	public void addViedoLikeCount(String videoId);
	
	/**
	 * 对喜欢的视频数量进行累剪
	 * @param videoId
	 */
	public void reduceViedoLikeCount(String videoId);
}