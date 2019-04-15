package com.imooc.service;

import java.util.List;

import com.imooc.pojo.Bgm;
import com.imooc.pojo.Users;

public interface BgmService {
	public List<Bgm> queryBgmList();
	public Bgm queryBgmById(String bgmId);
		
}
