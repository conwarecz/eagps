package net.aineuron.eagps.model.transfer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Vit Veres on 29.03.2018
 * as a part of eagps project.
 */
public class PostponedTime {
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	private String time;
	private int offsetMinutes;

	public PostponedTime(String time, int offsetMinutes) {
		this.time = time;
		this.offsetMinutes = offsetMinutes;
	}

	public static PostponedTime createPostponedTime(Calendar calendar, int offset) {
		return new PostponedTime(PostponedTime.timeFormat.format(calendar.getTime()), offset);
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getOffsetMinutes() {
		return offsetMinutes;
	}

	public void setOffsetMinutes(int offsetMinutes) {
		this.offsetMinutes = offsetMinutes;
	}

	@Override
	public String toString() {
		return "PostponedTime{" +
				"time='" + time + '\'' +
				", offsetMinutes=" + offsetMinutes +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PostponedTime that = (PostponedTime) o;

		if (offsetMinutes != that.offsetMinutes) return false;
		return time != null ? time.equals(that.time) : that.time == null;
	}

	@Override
	public int hashCode() {
		int result = time != null ? time.hashCode() : 0;
		result = 31 * result + offsetMinutes;
		return result;
	}
}
