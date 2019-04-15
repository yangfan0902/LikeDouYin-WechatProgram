package com.imooc.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import com.imooc.mapper.UsersFansMapper;
import com.imooc.mapper.UsersLikeVideosMapper;
import com.imooc.mapper.UsersMapper;
import com.imooc.mapper.UsersReportMapper;
import com.imooc.pojo.Users;
import com.imooc.pojo.UsersFans;
import com.imooc.pojo.UsersLikeVideos;
import com.imooc.pojo.UsersReport;
import com.imooc.service.UserService;
import com.imooc.utils.MD5Utils;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UsersMapper usersMapper;
	
	@Autowired
	private Sid sid;
	
	@Autowired
	private UsersLikeVideosMapper usersLikeVideosMapper;
	
	@Autowired
	private UsersFansMapper usersFansMapper;
	
	@Autowired
	private UsersReportMapper usersReportMapper;
	
	@Transactional(propagation=Propagation.SUPPORTS)
	@Override
	public boolean queryUsernameIsExist(String username) {
		Users user=new Users();
		user.setUsername(username);
		Users res=usersMapper.selectOne(user);
		return res==null?false:true;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public void saveUser(Users user) {
		user.setId(sid.nextShort()); 
		usersMapper.insert(user);

	}

	@Override
	public boolean queryUserIsExist(Users user) {
		try {
			user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Users res=usersMapper.selectOne(user);
		if(res!=null){
			BeanUtils.copyProperties(res, user);
		}
		return res==null?false:true;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public void updateUserInfo(Users user) {
		Example userExample=new Example(Users.class);
		Criteria criteria=userExample.createCriteria();
		criteria.andEqualTo("id", user.getId());
		usersMapper.updateByExampleSelective(user, userExample);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS)
	@Override
	public Users queryUserInfo(String userId) {
		Example userExample=new Example(Users.class);
		Criteria criteria=userExample.createCriteria();
		criteria.andEqualTo("id", userId);
		Users user=usersMapper.selectOneByExample(userExample);
		return user;
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	@Override
	public boolean isUserLikeVideo(String userId, String videoId) {
		if(StringUtils.isBlank(userId)||StringUtils.isBlank(videoId)){
			return false;
		}
		Example example=new Example(UsersLikeVideos.class);
		Criteria criteria=example.createCriteria();
		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("videoId", videoId);
		
		List<UsersLikeVideos> list= usersLikeVideosMapper.selectByExample(example);
		if(list!=null&&list.size()>0){
			return true;
		}else{
			return false;
		}
	}

	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public void saveUserFanRelation(String userId, String fanId) {
		UsersFans userFan=new UsersFans();
		String id=sid.nextShort();
		userFan.setId(id);
		userFan.setUserId(userId);
		userFan.setFanId(fanId);
		usersFansMapper.insert(userFan);
		
		usersMapper.addFansCount(userId);
		usersMapper.addFollersCount(fanId);
		
	}

	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public void deleteUserFanRelation(String userId, String fanId) {
		Example example=new Example(UsersFans.class);
		Criteria criteria=example.createCriteria();
		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("fanId", fanId);
		
		usersFansMapper.deleteByExample(example);
		
		usersMapper.reduceFansCount(userId);
		usersMapper.reduceFollersCount(fanId);
		
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	@Override
	public boolean queryIfFollow(String userId, String fanId) {
		Example example=new Example(UsersFans.class);
		Criteria criteria=example.createCriteria();
		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("fanId", fanId);
		
		List<UsersFans> list=usersFansMapper.selectByExample(example);
		if(list!=null&&list.size()>0){ 
			return true;
		}
		return false;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public void reportUser(UsersReport usersReport) {
		String id=sid.nextShort();
		Date date=new Date();
		usersReport.setId(id);
		usersReport.setCreateDate(date);
		
		usersReportMapper.insert(usersReport);
		
	}

}
