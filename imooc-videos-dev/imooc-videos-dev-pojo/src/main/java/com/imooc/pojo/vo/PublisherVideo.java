package com.imooc.pojo.vo;

public class PublisherVideo {
	public UsersVO publisher;
	public boolean userLikeVideo;

	public UsersVO getPublisher() {
		return publisher;
	}

	public void setPublisher(UsersVO publicsher) {
		this.publisher = publicsher;
	}

	public boolean isUserLikeVideo() {
		return userLikeVideo;
	}

	public void setUserLikeVideo(boolean userLikeVideo) {
		this.userLikeVideo = userLikeVideo;
	}

}