package net.aineuron.eagps.util;

import net.aineuron.eagps.model.transfer.PostponedTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Vit Veres on 29.03.2018
 * as a part of eagps project.
 */
public class TimeUtil {
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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

	public static Date fixDateTimeZones(Date date) {
		try {
			simpleDateFormat.setTimeZone(TimeZone.getDefault());
			String string = simpleDateFormat.format(date);
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date newDate = simpleDateFormat.parse(string);
			return newDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

}


