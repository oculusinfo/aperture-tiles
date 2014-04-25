/*
 * Copyright (c) 2014 Oculus Info Inc. 
 * http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.twitter.binning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oculusinfo.binning.util.Pair;

import org.junit.Assert;
import org.junit.Test;

public class TwitterTopicTests {

	private long _endTimeSecs = 2000000000L;			// arbitrary end time (epoch format in sec) for these JUnit tests
	private String _sampleTopic = "futebol";			// sample topic for these tests (in portuguese)
	private String _sampleTopicEnglish = "football";	// sample topic in English
	private int[] _sampleDailyCounts = new int[31];
	private int[] _sample6HrsCounts = new int[28];
	private int[] _sampleHourlyCounts = new int[24];
	
	
	// sample record with no counts and a given end time
	private TwitterDemoTopicRecord _sampleRecord = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 0, 
													_sampleDailyCounts,
													_sample6HrsCounts,
													_sampleHourlyCounts,
													Arrays.asList(new Pair<String, Long>("", _endTimeSecs)),
													_endTimeSecs);
	
	//---- Create a topic with no counts and an end time.
	@Test
	public void testCreateTopicWithNoCounts() {
			TwitterDemoTopicRecord a = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 0, 
													_sampleDailyCounts,
													_sample6HrsCounts,
													_sampleHourlyCounts,
													Arrays.asList(new Pair<String, Long>("", _endTimeSecs)),
													_endTimeSecs);
		Assert.assertEquals(_sampleRecord, a);
	}
	
	//---- Adding a tweet to a record before the beginning of its valid time range
	@Test
	public void testAddTweetBeforeBeginning() {	
		
		Pair<String, Long> tweet1 = new Pair<String, Long>("Eu amo o futebol", _endTimeSecs - (2678400L+1)); // 1 month + 1 sec from end time
		
		TwitterDemoTopicRecord a = TwitterDemoTopicRecord.addTweetToRecord(_sampleRecord, tweet1);
		Assert.assertEquals(_sampleRecord, a);
	}
	
	//---- Adding a tweet to a record after the end time
	@Test
	public void testAddTweetAfterEnd() {	
		
		Pair<String, Long> tweet1 = new Pair<String, Long>("Eu amo o futebol", _endTimeSecs + 1L); // 1 sec after end time

		
		TwitterDemoTopicRecord a = TwitterDemoTopicRecord.addTweetToRecord(_sampleRecord, tweet1);
		Assert.assertEquals(_sampleRecord, a);
	}	
	
	//---- Adding a tweet to a record so it increments monthly count per day, but not quarter-daily or hourly
	@Test
	public void testAddTweetMonthly() {	
		
		Pair<String, Long> tweet1 = new Pair<String, Long>("Eu amo o futebol", _endTimeSecs - (2678400L-1)); // 1 month - 1 sec from end time
		
		TwitterDemoTopicRecord a = TwitterDemoTopicRecord.addTweetToRecord(_sampleRecord, tweet1);
		Assert.assertEquals(a.getCountMonthly(), _sampleRecord.getCountMonthly()+1);
		Assert.assertTrue(a.getCountDaily()[30] == _sampleRecord.getCountDaily()[30]+1);	// check count for last day of month
		for (int n=0; n<30; n++) {
			Assert.assertTrue(a.getCountDaily()[n] == _sampleRecord.getCountDaily()[n]);
		}
		Assert.assertTrue(arraysEqual(a.getCountPer6hrs(), _sampleRecord.getCountPer6hrs()));
		Assert.assertTrue(arraysEqual(a.getCountPerHour(), _sampleRecord.getCountPerHour()));
	}
	
	
	//---- Adding a tweet to a record so it increments monthly count per day and quarter-daily, but not hourly
	@Test
	public void testAddTweetQuarterDaily() {	
		
		Pair<String, Long> tweet1 = new Pair<String, Long>("Eu amo o futebol", _endTimeSecs - (604800L-1)); // 7 days - 1 sec from end time
	
		TwitterDemoTopicRecord a = TwitterDemoTopicRecord.addTweetToRecord(_sampleRecord, tweet1);
		Assert.assertEquals(a.getCountMonthly(), _sampleRecord.getCountMonthly()+1);
		for (int n=0; n<31; n++) {
			if (n!=6)
				Assert.assertTrue(a.getCountDaily()[n] == _sampleRecord.getCountDaily()[n]);
			else
				Assert.assertTrue(a.getCountDaily()[n] == _sampleRecord.getCountDaily()[n]+1);	// check count for 7 days from end			
		}
		for (int n=0; n<28; n++) {
			if (n!=27)
				Assert.assertTrue(a.getCountPer6hrs()[n] == _sampleRecord.getCountPer6hrs()[n]);
			else
				Assert.assertTrue(a.getCountPer6hrs()[n] == _sampleRecord.getCountPer6hrs()[n]+1);	// check last quarter-daily count			
		}		
		Assert.assertTrue(arraysEqual(a.getCountPerHour(), _sampleRecord.getCountPerHour()));
	}	

	//---- Adding a tweet to a record so it increments monthly count per day, quarter-daily, and hourly
	@Test
	public void testAddTweetHourly() {	
		
		Pair<String, Long> tweet1 = new Pair<String, Long>("Eu amo o futebol", _endTimeSecs - 1L); // 1 sec prior to end time
	
		TwitterDemoTopicRecord a = TwitterDemoTopicRecord.addTweetToRecord(_sampleRecord, tweet1);
		Assert.assertEquals(a.getCountMonthly(), _sampleRecord.getCountMonthly()+1);
		for (int n=0; n<31; n++) {
			if (n!=0)
				Assert.assertTrue(a.getCountDaily()[n] == _sampleRecord.getCountDaily()[n]);
			else
				Assert.assertTrue(a.getCountDaily()[n] == _sampleRecord.getCountDaily()[n]+1);		
		}
		for (int n=0; n<28; n++) {
			if (n!=0)
				Assert.assertTrue(a.getCountPer6hrs()[n] == _sampleRecord.getCountPer6hrs()[n]);
			else
				Assert.assertTrue(a.getCountPer6hrs()[n] == _sampleRecord.getCountPer6hrs()[n]+1);			
		}
		for (int n=0; n<24; n++) {
			if (n!=0)
				Assert.assertTrue(a.getCountPerHour()[n] == _sampleRecord.getCountPerHour()[n]);
			else
				Assert.assertTrue(a.getCountPerHour()[n] == _sampleRecord.getCountPerHour()[n]+1);			
		}
	}
	
//TODO --- need to re-write the tests below to use arrays for 'counts' instead of lists	
//	//---- Adding two records
//	@Test
//    public void testRecordAggregation () {
//		
//		TwitterDemoTopicRecord a = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 1, 
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("Eu amo o futebol", _endTimeSecs - 1L)),	// 1 sec prior to end time
//													_endTimeSecs);
//		
//		TwitterDemoTopicRecord b = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 1, 
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("N�s todos amamos o futebol", _endTimeSecs - 3601L)),	// 1 hr + 1 sec prior to end time
//													_endTimeSecs);
//		
//		TwitterDemoTopicRecord c = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 2, 
//													Arrays.asList(2, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(2, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("Eu amo o futebol", _endTimeSecs - 1L),
//																  new Pair<String, Long>("N�s todos amamos o futebol", _endTimeSecs - 3601L)),
//													_endTimeSecs);		
//
//        Assert.assertEquals(c, TwitterDemoTopicRecord.addRecords(a, b));
//    }
//	
//	//---- Adding records with different topics
//	@Test(expected=IllegalArgumentException.class)
//    public void testIllegalRecordAddition () {
//		TwitterDemoTopicRecord a = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 1, 
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("Eu amo o futebol", _endTimeSecs - 1L)),	// 1 sec prior to end time
//													_endTimeSecs);
//		
//		TwitterDemoTopicRecord b = new TwitterDemoTopicRecord("h�quei", "hockey", 1, 
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("Todos n�s gostamos de h�quei", _endTimeSecs - 3601L)),	// 1 hr + 1 sec prior to end time
//													_endTimeSecs);
//		
//        TwitterDemoTopicRecord.addRecords(a, b);
//    }
//	
//	//---- Min of two records
//    @Test
//    public void testMin() {
//		TwitterDemoTopicRecord a = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 15, 
//													Arrays.asList(1, 0, 0, 0, 0, 0, 5, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 2, 3, 0),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("blah1", _endTimeSecs - 1000L),
//																new Pair<String, Long>("blah2", _endTimeSecs - 2000L),
//																new Pair<String, Long>("blah3", _endTimeSecs - 3000L)),
//													_endTimeSecs);
//		
//		TwitterDemoTopicRecord b = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 17, 
//													Arrays.asList(1, 0, 0, 0, 0, 5, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 8),
//													Arrays.asList(0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 5, 0, 0, 0, 0),
//													Arrays.asList(0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("blah3", _endTimeSecs - 1500L),
//																new Pair<String, Long>("blah4", _endTimeSecs - 2500L),
//																new Pair<String, Long>("blah5", _endTimeSecs - 3500L)),
//													_endTimeSecs);
//
//		TwitterDemoTopicRecord c = new TwitterDemoTopicRecord(null, null, 15, 
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8),
//													Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													new ArrayList<Pair<String, Long>>(),
//													0);
//		
//        Assert.assertEquals(c, TwitterDemoTopicRecord.minOfRecords(a, b));
//    }
//    
//	//---- Max of two records
//    @Test
//    public void testMax() {
//		TwitterDemoTopicRecord a = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 15, 
//													Arrays.asList(1, 0, 0, 0, 0, 0, 5, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 2, 3, 0),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("blah1", _endTimeSecs - 1000L),
//																new Pair<String, Long>("blah2", _endTimeSecs - 2000L),
//																new Pair<String, Long>("blah3", _endTimeSecs - 3000L)),
//													_endTimeSecs);
//		
//		TwitterDemoTopicRecord b = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 17, 
//													Arrays.asList(1, 0, 0, 0, 0, 5, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 8),
//													Arrays.asList(0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 5, 0, 0, 0, 0),
//													Arrays.asList(0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("blah3", _endTimeSecs - 1500L),
//																new Pair<String, Long>("blah4", _endTimeSecs - 2500L),
//																new Pair<String, Long>("blah5", _endTimeSecs - 3500L)),
//													_endTimeSecs);
//
//		TwitterDemoTopicRecord c = new TwitterDemoTopicRecord(null, null, 17, 
//													Arrays.asList(1, 0, 0, 0, 0, 5, 5, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 9),
//													Arrays.asList(1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 5, 0, 2, 3, 0),
//													Arrays.asList(1, 0, 0, 0, 0, 0, 1, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													new ArrayList<Pair<String, Long>>(),
//													0);
//		TwitterDemoTopicRecord d = TwitterDemoTopicRecord.maxOfRecords(a, b);
//        Assert.assertEquals(c, TwitterDemoTopicRecord.maxOfRecords(a, b));
//    }  
//    
//    // Check string conversion
//    @Test
//    public void testStringConversion () {
//		TwitterDemoTopicRecord a = new TwitterDemoTopicRecord(_sampleTopic, _sampleTopicEnglish, 2, 
//													Arrays.asList(2, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(2, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0),
//													Arrays.asList(1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//															0, 0, 0, 0),
//													Arrays.asList(new Pair<String, Long>("abcdef", _endTimeSecs - 1000L),
//															new Pair<String, Long>("abc\"\"\\\"\\\\\"\\\\\\\"def", _endTimeSecs - 2000L)),
//													_endTimeSecs);    	
//
//        String as = a.toString();
//        TwitterDemoTopicRecord b = TwitterDemoTopicRecord.fromString(as);
//        //Assert.assertEquals(a, b);		//TODO -- this assert fails, but all the ones below are OK (??)
//        Assert.assertEquals(a.getTopic(), b.getTopic());
//        Assert.assertEquals(a.getTopicEnglish(), b.getTopicEnglish());
//        Assert.assertEquals(a.getCountDaily(), b.getCountDaily());
//        Assert.assertEquals(a.getCountPer6hrs(), b.getCountPer6hrs());
//        Assert.assertEquals(a.getCountPerHour(), b.getCountPerHour());
//        Assert.assertEquals(a.getRecentTweets(), b.getRecentTweets());
//        Assert.assertTrue(a.getCountMonthly() == b.getCountMonthly());
//        Assert.assertTrue(a.getEndTime() == b.getEndTime());
//    }
	
	private static <T> boolean arraysEqual(int[] a, int[] b) {
		if (null == a)
			return null == b;
		if (null == b)
			return false;
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}	
	
}
