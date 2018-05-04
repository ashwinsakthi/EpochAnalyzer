package com.epoch.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class EpochAnalyser {

	private static final int EPOCH_COUNT_FOR_START = 150;
	private static final int EPOCH_COUNT_FOR_FINISH = 375;
	private static final int EPOCH_COUNT_FOR_ROLLING_PERCENTAGE = EPOCH_COUNT_FOR_START;
	private static final int ROLLING_PERCENT_THRESHOLD = 144;// 150 * 0.96 = 144
	private static final int ACTIVITY_ID_TO_ANALYSE = 8;
	private static String mFileName = "TestData.csv";
	//private static final String DATE_FORMAT_EPOCH = "yyyy-MM-dd HH:mm:ss"; 7/31/2014 19:00
	private static final String DATE_FORMAT_EPOCH = "M/dd/yyyy HH:mm";
	private static final String SEPERATOR = ",";
	static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	private Date mStartTime;
	private Date mFinishTime;
	private String mDuration;
	private CircularFifoQueue<Epoch> mRollingQueue;
	private BufferedReader mBufferedReader;
	private int mFinishTriggerCount;
	private int mRollingPercent;
    private int mStartTriggerCount = 0;
    private int mEpochCountForDuration;

	public EpochAnalyser() {
		mRollingQueue = new CircularFifoQueue<Epoch>(EPOCH_COUNT_FOR_ROLLING_PERCENTAGE);
		try {
			
			mBufferedReader = new BufferedReader(
					new FileReader((new File(getClass().getClassLoader().getResource(mFileName).getFile()))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		EpochAnalyser epochAnalyser = new EpochAnalyser();
		try {
			epochAnalyser.start();
			
			System.out.println("Start of Period - " + timeFormat.format(epochAnalyser.mStartTime));
			System.out.println("Finish of Period - " + timeFormat.format(epochAnalyser.mFinishTime));
			System.out.println("Period Duration - " + epochAnalyser.mDuration);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void start() throws IOException, ParseException {

		 String nextLine;
	        Epoch epoch = null;
	        Date possibleStartTime = null;
	        Date possibleFinishTime = null;
	        int epochsToRemove = 0;
	        mBufferedReader.readLine();//skip first line
	        while((nextLine = mBufferedReader.readLine()) != null){
	            epoch = getNextEpoch(nextLine);
	            if(epoch != null) {
	                mRollingPercent = updateRollingPercent(epoch);
	                //mark startTime
	                if (mStartTime == null) {
	                    if (mRollingPercent >= ROLLING_PERCENT_THRESHOLD
	                            && mRollingQueue.size() == EPOCH_COUNT_FOR_ROLLING_PERCENTAGE) {
	                        if (mStartTriggerCount == 0) {
	                            possibleStartTime = mRollingQueue.get(0).getTimeStamp();//head
	                            mEpochCountForDuration = getCurrentEpochCountForDuration();
	                        }
	                        mStartTriggerCount++;
	                    } else {
	                        mStartTriggerCount = 0;
	                    }
	                    if (mStartTriggerCount >= EPOCH_COUNT_FOR_START) {
	                        mStartTime = possibleStartTime;
	                        mEpochCountForDuration = mEpochCountForDuration + getCurrentEpochCountForDuration() - 1;
	                    }
	                }
	                //mark finishTime, once startTime is marked
	                else if (mFinishTime == null) {
	                    if(epoch.getActivityId() == ACTIVITY_ID_TO_ANALYSE){
	                        mEpochCountForDuration++;
	                    }
	                    if (mRollingPercent < ROLLING_PERCENT_THRESHOLD) {
	                        mFinishTriggerCount++;
	                        if(epoch.getActivityId() == ACTIVITY_ID_TO_ANALYSE){
	                            epochsToRemove++;
	                        }
	                    } else {
	                        //keep resetting...
	                        possibleFinishTime = epoch.getTimeStamp();
	                        mFinishTriggerCount = 0;
	                        epochsToRemove = 0;
	                    }
	                    if (mFinishTriggerCount >= EPOCH_COUNT_FOR_FINISH) {
	                        mFinishTime = possibleFinishTime;
	                        mEpochCountForDuration = mEpochCountForDuration - epochsToRemove;
	                        break;
	                    }
	                }
	            }
	        }
	        //if startTime is found and finishTime is not found by end of file
	        if(mStartTime != null && mFinishTime == null && epoch != null){
	            mFinishTime = epoch.getTimeStamp();
	        }
	        mDuration = secondsToHHMM(mEpochCountForDuration * 10);
		
	}
	
	private int getCurrentEpochCountForDuration() {
        int epochCount = 0;
        Iterator<Epoch> iterator = mRollingQueue.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getActivityId() == ACTIVITY_ID_TO_ANALYSE) {
                epochCount++;
            }
        }
        return epochCount;
    }

    private int updateRollingPercent(Epoch epoch) {
        mRollingQueue.add(epoch);
        int epochCount = 0;
        if(mRollingQueue.size() == EPOCH_COUNT_FOR_ROLLING_PERCENTAGE) {
            Iterator<Epoch> iterator = mRollingQueue.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getActivityId() == ACTIVITY_ID_TO_ANALYSE) {
                    epochCount++;
                }
            }
        }
        return epochCount;
    }

    public Epoch getNextEpoch(String nextLine) throws IOException, ParseException {
        String[] lineContents = nextLine.split(SEPERATOR);
        int activityId = Integer.parseInt(lineContents[0]);//first column contains activityId
        Date timestamp = getDateFromString(lineContents[1]);//second column contains timestamp
        Epoch epoch = new Epoch(activityId, timestamp);
        return epoch;
    }

    public static Date getDateFromString(String dateString) throws ParseException {
        return new SimpleDateFormat(DATE_FORMAT_EPOCH).parse(dateString);//2014-07-31 19:42:26
    }

    private String secondsToHHMM(int pTime) {
        String string = String.format("%02d:%02d", pTime / 3600, (pTime % 3600) / 60);
        return string;
    }

}
