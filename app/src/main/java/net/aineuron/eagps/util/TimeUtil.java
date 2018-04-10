package net.aineuron.eagps.util;

import net.aineuron.eagps.model.transfer.PostponedTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Vit Veres on 29.03.2018
 * as a part of eagps project.
 */
public class TimeUtil {

	public static List<PostponedTime> generatePostponedTimes(Date date, int maxMinutesOffset) {
		int timeInterval = 5;
		List<PostponedTime> times = new ArrayList<>();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		int minute = calendar.get(Calendar.MINUTE);
		int minuteAddToRound5 = timeInterval - (minute % timeInterval);
		if (minuteAddToRound5 == timeInterval) {
			minuteAddToRound5 = 0;
		}

		for (int i = minuteAddToRound5; i <= maxMinutesOffset + minuteAddToRound5; i += timeInterval) {
			if (i == 0) {
				//noop
			} else if (0 < i && i < timeInterval) {
				times.add(PostponedTime.createPostponedTime(calendar, 0));
				calendar.add(Calendar.MINUTE, minuteAddToRound5);
			} else {
				calendar.add(Calendar.MINUTE, timeInterval);
			}
			times.add(PostponedTime.createPostponedTime(calendar, i));
		}

		return times;
	}
}


