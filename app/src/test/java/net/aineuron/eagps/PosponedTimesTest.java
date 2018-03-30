package net.aineuron.eagps;

import net.aineuron.eagps.model.transfer.PostponedTime;
import net.aineuron.eagps.util.TimeUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class PosponedTimesTest {
	@Test
	public void timesCorrectOverHour() throws Exception {
		List times = TimeUtil.generatePostponedTimes(new Date(1522335849954L), 60); // 22:04:30
		List expected = new ArrayList<PostponedTime>() {{
			add(new PostponedTime("22:04", 0));
			add(new PostponedTime("22:05", 1));
			add(new PostponedTime("22:10", 6));
			add(new PostponedTime("22:15", 11));
			add(new PostponedTime("22:20", 16));
			add(new PostponedTime("22:25", 21));
			add(new PostponedTime("22:30", 26));
			add(new PostponedTime("22:35", 31));
			add(new PostponedTime("22:40", 36));
			add(new PostponedTime("22:45", 41));
			add(new PostponedTime("22:50", 46));
			add(new PostponedTime("22:55", 51));
			add(new PostponedTime("23:00", 56));
			add(new PostponedTime("23:05", 61));
		}};

		assertThat(times, is(expected));
	}

	@Test
	public void timesCorrectOverMidnight() throws Exception {
		List times = TimeUtil.generatePostponedTimes(new Date(1522341264000L), 60); // 23:34:30
		List expected = new ArrayList<PostponedTime>() {{
			add(new PostponedTime("23:34", 0));
			add(new PostponedTime("23:35", 1));
			add(new PostponedTime("23:40", 6));
			add(new PostponedTime("23:45", 11));
			add(new PostponedTime("23:50", 16));
			add(new PostponedTime("23:55", 21));
			add(new PostponedTime("00:00", 26));
			add(new PostponedTime("00:05", 31));
			add(new PostponedTime("00:10", 36));
			add(new PostponedTime("00:15", 41));
			add(new PostponedTime("00:20", 46));
			add(new PostponedTime("00:25", 51));
			add(new PostponedTime("00:30", 56));
			add(new PostponedTime("00:35", 61));
		}};

		assertThat(times, is(expected));
	}

	@Test
	public void timesCorrectOverMidnight2() throws Exception {
		List times = TimeUtil.generatePostponedTimes(new Date(1522341324000L), 30); // 23:35:30
		List expected = new ArrayList<PostponedTime>() {{
			add(new PostponedTime("23:35", 0));
			add(new PostponedTime("23:40", 5));
			add(new PostponedTime("23:45", 10));
			add(new PostponedTime("23:50", 15));
			add(new PostponedTime("23:55", 20));
			add(new PostponedTime("00:00", 25));
			add(new PostponedTime("00:05", 30));
		}};

		assertThat(times, is(expected));
	}
}