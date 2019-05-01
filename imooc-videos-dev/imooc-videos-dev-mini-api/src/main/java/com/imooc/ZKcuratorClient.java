package com.imooc;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.assertj.core.api.UrlAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.druid.support.json.JSONUtils;
import com.imooc.cofig.ResourceConfig;
import com.imooc.enums.BGMOperationTypeEnum;
import com.imooc.pojo.Bgm;
import com.imooc.service.BgmService;
import com.imooc.utils.JsonUtils;

@Component
public class ZKcuratorClient {
	
	@Autowired
	private BgmService bgmService;
	
	@Autowired
	private ResourceConfig resourceConfig;
	
	//zk客户端
	private CuratorFramework client=null;
	final static Logger log=LoggerFactory.getLogger(ZKcuratorClient.class);
	
//	public static final String zookeeper_server="192.168.91.128:2181";
	
	
	public void init(){
		String zookeeper_server=resourceConfig.getZookeeperServer();
		if(client !=null){
			return;
		}
		//重连策略
		RetryPolicy retryPolicy=new ExponentialBackoffRetry(1000,5);
		
		//创建客户端
		client= CuratorFrameworkFactory.builder().connectString(zookeeper_server)
				.sessionTimeoutMs(10000)
				.retryPolicy(retryPolicy)
				.namespace("admin")
				.build();
		
		client.start();
		
		try {
			addChildWatch("/bgm");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addChildWatch(String nodePath) throws Exception{
		
		final PathChildrenCache cache=new PathChildrenCache(client, nodePath, true);
		cache.start();
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				
				if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)){
					log.info("监听到了事件");
					
			
					String path=event.getData().getPath();
					String operObject=new String(event.getData().getData());
					Map<String,String> map=JsonUtils.jsonToPojo(operObject,Map.class);
					
					String operatorType=map.get("operType");
					String songPath=map.get("path");
					
//					String arr[]=path.split("/");
//					String bgmId=arr[arr.length-1];
					
//					Bgm bgm=bgmService.queryBgmById(bgmId);
//					
//					if(bgm==null){
//						return;
//					}
//					String songPath=bgm.getPath();
					//2.定义保存到本地的bgm路径
//					String filePath="C:\\Users\\fan\\Desktop"+songPath;
					String filePath=resourceConfig.getFileSpace()+songPath;
					
					//3.定义下载的路径
					String arrPath[]=songPath.split("\\\\");
					String finalPath="";
					//处理url的斜杠及编码
					for(int i=0;i<arrPath.length;i++){
						if(StringUtils.isNoneBlank(arrPath[i])){
							finalPath+="/";
							finalPath+=URLEncoder.encode(arrPath[i],"UTF-8");
						}
					}
//					String bgmUrl="http://localhost:8080/mvc"+finalPath;
					String bgmUrl=resourceConfig.getBgmServer()+finalPath;
					
					if(operatorType.equals(BGMOperationTypeEnum.ADD.type)){
						
						//下载bgm到springboot服务器
						URL url=new URL(bgmUrl);
						File file=new File(filePath);
						FileUtils.copyURLToFile(url, file);
						client.delete().forPath(path);
						
					}else if(operatorType.equals(BGMOperationTypeEnum.DELETE.type)){
						File file=new File(filePath);
						FileUtils.forceDelete(file);
						client.delete().forPath(path);
					}
				}
				
			}
		});
		
	}
	
}
