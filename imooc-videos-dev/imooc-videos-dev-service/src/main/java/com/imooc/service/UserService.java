package com.imooc.service;

import com.imooc.pojo.Users;

public interface UserService {
	public boolean queryUsernameIsExist(String username);
	
	public void saveUser(Users user);
	
	public boolean queryUserIsExist(Users user);
	
	public void updateUserInfo(Users user);
	
	public Users queryUserInfo(String userId);
	
	public boolean isUserLikeVideo(String userId,String videoId);

	public void saveUserFanRelation(String userId,String fanId);
	
	public void deleteUserFanRelation(String userId,String fanId);
	
	/**
	 * 查询用户是否关注
	 * @param userId
	 * @param fanId
	 * @return
	 */
	public boolean queryIfFollow(String userId,String fanId);
}
