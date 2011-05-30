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
package inspiracio.xml;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

import com.google.gdata.data.DateTime;

/** A helper class for parsing and formatting dates
 * according to specification RTC 3339, which is used for
 * many internet protocols.
 * The specification is at <a href="http://www.ietf.org/rfc/rfc3339.txt">http://www.ietf.org/rfc/rfc3339.txt</a>.
 * */
public final class XMLDate{

	/** Date format according to RFC 3339.
	 * <p>
	 * It is approximate: Missing proper treatment of time zone.
	 * <p>
	 * If we have to avoid the Google class, use this pattern with a
	 * SimpleDateFormat. That's good enough.
	 * */
	public static final String RFC_3339="yyyy-MM-dd'T'HH:mm:ss'Z'";

	//Constructors --------------------------------------------------

	private XMLDate(){}

	//Methods -------------------------------------------------------

	/** Formats a date according to RTC 3339. Threadsafe.
	 * Interprets the date in the default time zone.
	 * @param date
	 * @return formatted Date
	 * */
	public static String format(Date date) {
		TimeZone zone=TimeZone.getDefault();
		String s=format(date, zone);
		return s;
	}

	/** Formats a date according to RTC 3339. Threadsafe.
	 * @param date
	 * @param zone Interpret the date in this time zone.
	 * @return formatted Date
	 * */
	public static String format(Date date, TimeZone zone) {
		DateTime dateTime=new DateTime(date, zone);
		String s=dateTime.toString();
		return s;
	}

	/** Parses a date according to RFC3339, which means roughly pattern
	 * "yyyy-MM-dd'T'HH:mm:ssZ", where 'Z' may be substituted by a time
	 * zone offset.
	 * Threadsafe.
	 * <p>
	 * @param s Date as String in correct format.
	 * @return parsed Date as java.sql.Timestamp, because that is often more
	 * 	useful for passing to a PreparedStatement
	 *
	 * @throws NumberFormatException Invalid RFC 3339 date or date/time string.
	 * */
	public static Timestamp parse(String s)throws NumberFormatException {
		DateTime dateTime=DateTime.parseDateTimeChoice(s);
		long value=dateTime.getValue();
		Timestamp timestamp=new Timestamp(value);
		return timestamp;
	}

	// Helpers ----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		String[] ss = {
				//from http://www.ietf.org/rfc/rfc3339.txt
				"1985-04-12T23:20:50.52Z", "1996-12-19T16:39:57-08:00", "1990-12-31T23:59:60Z",
				"1990-12-31T15:59:60-08:00", "1937-01-01T12:00:27.87+00:20",
				"2010-12-14T09:25:59Z",
				//others:
				"2008-02-12T13:23:34", "2008-02-12T13:23:34Z", "2008-02-12T13:23:34+01:00"};
		say("hi");
		for (String s : ss){
			Date d = parse(s);
			String t = format(d);
			say(s + " ---> " + d + " ---> " + t);
		}
		say("bye");
	}

	private static void say(Object o){System.out.println(o);}
}