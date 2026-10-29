/*
 * Copyright (c) 2013 L2jMobius
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.l2jmobius.gameserver.entity.actor.holders.player;

import org.l2jmobius.gameserver.entity.actor.enums.player.DailyMissionStatus;

/**
 * Per-player progress entry of a daily mission.
 * @author UnAfraid
 */
public class DailyMissionPlayerEntry
{
	private final int _objectId;
	private final int _rewardId;
	private DailyMissionStatus _status = DailyMissionStatus.NOT_AVAILABLE;
	private int _progress;
	private long _lastCompleted;
	private boolean _recentlyCompleted;
	
	public DailyMissionPlayerEntry(int objectId, int rewardId)
	{
		_objectId = objectId;
		_rewardId = rewardId;
	}
	
	public DailyMissionPlayerEntry(int objectId, int rewardId, int status, int progress, long lastCompleted)
	{
		this(objectId, rewardId);
		_status = DailyMissionStatus.valueOf(status);
		_progress = progress;
		_lastCompleted = lastCompleted;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public int getRewardId()
	{
		return _rewardId;
	}
	
	public DailyMissionStatus getStatus()
	{
		return _status;
	}
	
	public void setStatus(DailyMissionStatus status)
	{
		_status = status;
	}
	
	public int getProgress()
	{
		return _progress;
	}
	
	public void setProgress(int progress)
	{
		_progress = progress;
	}
	
	public int increaseProgress()
	{
		_progress++;
		return _progress;
	}
	
	public long getLastCompleted()
	{
		return _lastCompleted;
	}
	
	public void setLastCompleted(long lastCompleted)
	{
		_lastCompleted = lastCompleted;
	}
	
	public boolean isRecentlyCompleted()
	{
		return _recentlyCompleted;
	}
	
	public void setRecentlyCompleted(boolean recentlyCompleted)
	{
		_recentlyCompleted = recentlyCompleted;
	}
}
