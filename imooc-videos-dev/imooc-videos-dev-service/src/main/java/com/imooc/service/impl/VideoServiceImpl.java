package com.imooc.service.impl;

import java.util.List;

import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mapper.BgmMapper;
import com.imooc.mapper.SearchRecordsMapper;
import com.imooc.mapper.UsersLikeVideosMapper;
import com.imooc.mapper.UsersMapper;
import com.imooc.mapper.VideosMapper;
import com.imooc.mapper.VideosMapperCustom;
import com.imooc.pojo.Bgm;
import com.imooc.pojo.SearchRecords;
import com.imooc.pojo.Users;
import com.imooc.pojo.UsersLikeVideos;
import com.imooc.pojo.Videos;
import com.imooc.pojo.vo.VideosVO;
import com.imooc.service.BgmService;
import com.imooc.service.UserService;
import com.imooc.service.VideoService;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.PagedResult;

@Service
public class VideoServiceImpl implements VideoService {
	@Autowired
	private VideosMapper videosMapper;

	@Autowired
	private VideosMapperCustom videosMapperCustom;

	@Autowired
	private SearchRecordsMapper searchRecordsMapper;

	@Autowired
	private Sid sid;

	@Autowired
	private UsersLikeVideosMapper usersLikeVideosMapper;

	@Autowired
	private UsersMapper usersMapper;

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public String saveVideo(Videos video) {
		String id = sid.nextShort();
		video.setId(id);
		videosMapper.insertSelective(video);
		return id;

	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void updateViedo(String videoId, String coverPath) {
		Videos video = new Videos();
		video.setId(videoId);
		video.setCoverPath(coverPath);
		videosMapper.updateByPrimaryKeySelective(video);

	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public PagedResult getAllVideos(Videos video, Integer isSaveRecord, Integer page, Integer pageSize) {
		// 保存热搜词
		String desc = video.getVideoDesc();
		if (isSaveRecord != null && isSaveRecord == 1) {
			SearchRecords record = new SearchRecords();
			record.setId(sid.nextShort());
			record.setContent(desc);
			searchRecordsMapper.insert(record);
		}

		PageHelper.startPage(page, pageSize);
		List<VideosVO> list = videosMapperCustom.queryAllVideos(desc);
		PageInfo<VideosVO> pageList = new PageInfo(list);
		PagedResult pagedResult = new PagedResult();
		pagedResult.setPage(page);
		pagedResult.setTotal(pageList.getPages());
		pagedResult.setRows(list);
		pagedResult.setRecords(pageList.getTotal());
		return pagedResult;
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<String> getHotWords() {
		return searchRecordsMapper.getHotWords();
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void userLikeVideo(String userId, String videoId, String videoCreaterId) {
		// 1.保存用户喜欢视频的关联关系表
		String id = sid.nextShort();
		UsersLikeVideos ulv = new UsersLikeVideos();
		ulv.setId(id);
		ulv.setUserId(userId);
		ulv.setVideoId(videoId);
		usersLikeVideosMapper.insert(ulv);

		// 2.视频喜欢数量累加
		videosMapperCustom.addViedoLikeCount(videoId);

		// 3.用户受喜欢数量累加
		usersMapper.addReceiveLikeCount(userId);

	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void userUnLikeVideo(String userId, String videoId, String videoCreaterId) {
		// 1.删除用户喜欢视频的关联关系表
		Example example=new Example(UsersLikeVideos.class);
		Criteria criteria=example.createCriteria();
		
		criteria.andEqualTo("userId",userId);
		criteria.andEqualTo("videoId", videoId);
	
		usersLikeVideosMapper.deleteByExample(example);

		// 2.视频喜欢数量累减
		videosMapperCustom.reduceViedoLikeCount(videoId);

		// 3.用户受喜欢数量累减
		usersMapper.reduceReceiveLikeCount(userId);

	}

}
