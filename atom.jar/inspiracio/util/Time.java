/*  Copyright 2011 Alexander Bunkenburg alex@inspiracio.com

    This file is part of atom.jar.

    atom.jar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    atom.jar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with atom.jar.  If not, see <http://www.gnu.org/licenses/>.
 */
package inspiracio.util;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Time utilities.
 * @author iparraga
 */
public class Time {
	/**
	 * @param initial Initial time in milliseconds.
	 * @return a message with the shape: "xxx seconds (YYY millis)"
	 */
	public static String getLapseTimeMessage(long initial) {
		TimeUnit secondsUnit = TimeUnit.SECONDS;
		TimeUnit millisUnit = TimeUnit.MILLISECONDS;

		long timeLapse = System.currentTimeMillis() - initial;
		long seconds = secondsUnit.convert(timeLapse, millisUnit);
		long millis = millisUnit.convert(timeLapse, millisUnit);

		return seconds + " seconds (" + millis + " millis)";
	}

	/**
	 * Creates a date based on default Calendar for the specified date.
	 *
	 * @param year year
	 * @param month month as {@link Calendar#MONTH} (you can use constants defined there)
	 * @param day day of month
	 */
	public static Date getDate(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		return c.getTime();
	}

	/**
	 * Creates a date time based on default Calendar for the specified time.
	 * Year, month and day are meaningless and an implementation detail.
	 *
	 * @param hour
	 * @param minute
	 */
	public static Date getTime(int hour, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(0, 0, 0, hour, minute);
		return c.getTime();
	}

	/**
	 * Creates a date based on default Calendar for the specified date.
	 */
	public static Date getDate(int year, int month, int day, int hour, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day, hour, minute);
		return c.getTime();
	}
}