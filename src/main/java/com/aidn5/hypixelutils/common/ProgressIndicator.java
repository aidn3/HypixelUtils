package com.aidn5.hypixelutils.common;

public class ProgressIndicator<T> {
	private onProgressUpdate listener;
	private int progress = 0;
	private int totalProgress = -1;

	private boolean isCancelled = false;

	public int getProgress() {
		return progress;
	}

	public int getTotalProgress() {
		return totalProgress;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	protected void notifyProgressUpdate(int progress) {
		this.progress = progress;
		if (listener != null) listener.progressUpdate(getTotalProgress(), getProgress());
	}

	protected void setTotalProgress(int totalProgress) {
		this.totalProgress = totalProgress;
	}

	protected void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	public void setListener(onProgressUpdate<T> jobListener) {
		this.listener = jobListener;
	}

	public onProgressUpdate<T> getListener() {
		return listener;
	}

	public interface onProgressUpdate<T> {
		public void progressUpdate(int total, int current);

		public void onFinish(T result);
	}
}
