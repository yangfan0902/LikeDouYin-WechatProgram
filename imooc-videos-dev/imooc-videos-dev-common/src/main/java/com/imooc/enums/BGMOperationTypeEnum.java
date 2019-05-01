package com.imooc.enums;

public enum BGMOperationTypeEnum {
	
	ADD("1","添加bgm"),
	DELETE("2","删除bgm");
	
	public final String type;
	public final String value;
	
	
	
	
	BGMOperationTypeEnum(String type,String value){
		this.type=type;
		this.value=value;
	}




	public String getType() {
		return type;
	}




	public String getValue() {
		return value;
	}
	
	public static String getValueByKey(String key){
		if(key==null){
			return null;
		}
		if(key.equals("1")){
			return ADD.value;
		}
		if(key.equals("2")){
			return DELETE.value;
		}
		return null;
	}
}
