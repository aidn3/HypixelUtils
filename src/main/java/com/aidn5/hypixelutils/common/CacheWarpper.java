package com.aidn5.hypixelutils.common;

public class CacheWarpper<T> {
	private long cacheTime;
	private T data;

	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}

	public void setData(T data) {
		this.data = data;
	}

	public long getCacheTime() {
		return cacheTime;
	}

	public T getData() {
		return data;
	}
}
