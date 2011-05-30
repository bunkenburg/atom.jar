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
package atom.gdata;

import inspiracio.util.StringMap;
import inspiracio.xml.XMLDate;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/** Parameters of a GData query in a java bean. 
 * 
 * <p>
 * See http://code.google.com/apis/gdata/reference.html. 
 * Does not include the categories. Does include the entry-id,
 * which is not a URL-parameter.
 * </p>
 * 
 * <p>
 * Usually, a null value for a parameter means the parameter
 * has not been specified in the query and the result should 
 * include all.
 * </p>
 * 
 * <p>
 * There are always setters that accepts Strings, and if necessary,
 * parse dates or integers from the Strings. That way, it is easy 
 * to initialise the object with values read from URL-parameters
 * and decoded.
 * </p>
 * */
public class GDataParameters extends StringMap{
	/* Should override entries() and similar so that the id also appears
	 * in these collections.
	 */

	//State -----------------------------------------------------------------
	
	/** ID of a specific entry to be retrieved. 
	 * <ul>
	 * 	<li>
	 * 		If you specify an entry ID, you can't specify any other parameters.
	 * 		If you do, they will be ignored.
	 * 	</li> 
	 * 	<li>The form of the entry ID is determined by the GData service.</li> 
	 * 	<li>Unlike most of the other query parameters, entry ID is specified as part of the URI, not as a name=value pair.</li>
	 * 	<li>Example: <code>http://www.example.com/feeds/jo/entry1</code>.</li>
	 * </ul>
	 * The default value is null. In searches, null means select all
	 * entries. 
     */
	private String id;

	//Constructors ----------------------------------------------------------
	
	/** Constructs from a Map that represents some URL-parameters.
	 * */
	public GDataParameters() throws RuntimeException {
		super(new TreeMap<String, String>());
	}
		
	/** Constructs from a Map that represents some URL-parameters.
	 * @param ps A map of parameters
	 * @exception RuntimeException
	 * */
	public GDataParameters(Map<String, String> ps) throws RuntimeException {
		super(ps);
		if (ps instanceof GDataParameters){
			//Must copy entry-id explicitly, as it is not a URL parameter.
			//I wanted to distinguish this case by overloading the
			//constructor, but java complains about ambiguity!
			GDataParameters gs = (GDataParameters)ps;
			String id = gs.getId();
			this.setId(id);
		}
	}
	
	//Methods ----------------------------------------------------
	
	/** Clear all parameters */
	@Override public void clear(){
		super.clear();
		this.id = null;
	}

	/** Gets a parameter as int. If the parameter is
	 * missing or is not an int, returns null. */
	public Integer getInt(String key){
		try{
			return Integer.parseInt(this.get(key));
		}catch(NumberFormatException e){
			return null;
		}
	}
	
	/** See setAlt(String).
	 * @return the alt
	 */
	public String getAlt(){
		String alt=this.get("alt");
		return alt==null ? "atom" : alt;
	}

	/** Sets alternative representation type.
	 * <ul>
	 * 	<li>If you don't specify an <code>alt</code> parameter, the service returns an Atom feed. This is equivalent to <code>alt=atom</code>.</li>
	 * 	<li><code>alt=rss</code> returns an RSS 2.0 result feed.</li>
	 * </ul>
	 * We only implement "atom".
	 * @param alt the alt to set
	 */
	public void setAlt(String alt){this.put("alt", alt);}

	public String getAuthor(){return this.get("author");}

	/** Entry author.
	 * The service returns entries where the author name and/or email 
	 * address match your query string.
	 * Default null means select all. 
	 * @param author the author to set
	 */
	public void setAuthor(String author){this.put("author", author);}

	/** See private field id.
	 * @return the id
	 */
	public String getId(){return id;}

	/** Gets the id as an int.
	 * @return If there is not id or it cannot be parsed as int, null.
	 */
	public Integer getIdInt(){
		try{
			return Integer.parseInt(this.id);//NumberFormatException
		}catch(NumberFormatException nfe){
			return null;
		}
	}

	/** Sets the entry ID. Since the entry id is not a URL parameter,
	 * when parsing a URL to make an instance of GDataParameter, you must
	 * parse the entry id explicitly and then call this method. 
	 * @param id */
	public void setId(String id){this.id=id;}
	
	/** Sets the entry ID as int. 
	 * Equivalent to setId(Integer.toString(id)).
	 * @param id */
	public void setId(int id){
		this.setId(Integer.toString(id));
	}
	
	/** See setMaxResults(int>
	 * @return the maxResults, or default 35 if not specified
	 */
	public int getMaxResults() {
		String s = this.get("max-results");
		try {
			return Integer.parseInt(s);
		} catch(NumberFormatException e){
			return 35;//not specified: default
		}
	}

	/** Maximum number of results to be retrieved.
	 * For any service that has a default max-results value (to limit 
	 * default feed size), you can specify a very large number if you 
	 * want to receive the entire feed.
	 * Default 35.
	 * Value -1 means select as many entries as possible. The
	 * implementation may impose some upper limit.
	 * Value 0 means return a feed with zero entries, which may be 
	 * useful for clients that want to see the feed elements but are
	 * not interested in entries.
	 * @param maxResults the maxResults to set
	 */
	public void setMaxResults(int maxResults) {
		this.put("max-results", Integer.toString(maxResults));
	}

	/** Maximum number of results to be retrieved.
	 * For any service that has a default max-results value (to limit 
	 * default feed size), you can specify a very large number if you 
	 * want to receive the entire feed.
	 * Default 35.
	 * Value -1 means select all. 
	 * Value 0 means return a feed with zero entries, which may be 
	 * useful to get the feed parameters.
	 * @param maxResults the maxResults to set. Must be String 
	 * representation of -1, 0, or a positive integer, otherwise
	 * the search results are undefined.
	 */
	public void setMaxResults(String maxResults) {
		this.put("max-results", maxResults);
	}

	/** See setPublishedMax. If the published-max has not been set,
	 * returns null. If it has been set to a value that cannot be 
	 * parsed correctly, the result is undefined.
	 * @return the publishedMax
	 */
	public Timestamp getPublishedMax() {
		String s=this.get("published-max");
		if(s==null){
			return null;
		}else{
			return XMLDate.parse(s);
		}
	}

	/** Upper bound on the entry publication date.
	 * <ul>
	 * 	<li>Use the RFC 3339 timestamp format. For example: <code>2005-08-09T10:57:00-08:00</code>.</li> 
	 * 	<li>The lower bound is inclusive, whereas the upper bound is exclusive.</li> 
	 * </ul>
	 * Default null means select all. 
	 * @param publishedMax the publishedMax to set. Not null.
	 */
	public void setPublishedMax(Date publishedMax) {
		this.put("published-max", XMLDate.format(publishedMax));
	}

	/** Upper bound on the entry publication date.
	 * <ul>
	 * 	<li>Use the RFC 3339 timestamp format. For example: <code>2005-08-09T10:57:00-08:00</code>.</li>
	 * 	<li>The lower bound is inclusive, whereas the upper bound is exclusive.</li>
	 * </ul>
	 * Default null means select all. 
	 * @param publishedMax the publishedMax to set,
	 * must be in RTC 3339 format. Not null.
	 */
	public void setPublishedMax(String publishedMax) {
		this.put("published-max", publishedMax);
	}

	/** See setPublishedMin. If publishedMin has not been set,
	 * returns null. If it has been set to a value that cannot be
	 * parsed, undefined.
	 * @return the publishedMin
	 */
	public Timestamp getPublishedMin() {
		String s = this.get("published-min");
		if (s==null){
			return null;
		} else {
			Timestamp d = XMLDate.parse(s);
			return d;
		}
	}

	/** Lower bound on the entry publication date.
	 * <ul>
	 * 	<li>Use the RFC 3339 timestamp format. For example: <code>2005-08-09T10:57:00-08:00</code>.</li>
	 * 	<li>The lower bound is inclusive, whereas the upper bound is exclusive.</li>
	 * </ul>
	 * Default null means select all. 
	 * @param publishedMin the publishedMin to set. Not null.
	 */
	public void setPublishedMin(Date publishedMin) {
		this.setPublishedMin(XMLDate.format(publishedMin));
	}

	/** Lower bound on the entry publication date.
	 * <ul>
	 * 	<li>Use the RFC 3339 timestamp format. For example: <code>2005-08-09T10:57:00-08:00</code>.</li>
	 * 	<li>The lower bound is inclusive, whereas the upper bound is exclusive.</li>
	 * </ul>
	 * Default null means select all. 
	 * @param publishedMin the publishedMin to set,
	 * must be in RTC 3339 format. Not null.
	 */
	public void setPublishedMin(String publishedMin) {
		this.put("published-min", publishedMin);
	}

	/** See setQ(String).
	 * @return the q or null if none has been set.
	 */
	public String getQ(){return this.get("q");}

	/** Full-text query string. 
	 * <ul>
	 * 	<li>When creating a query, list search terms separated by spaces, in the form <code>q=term1 term2 term3</code>. (As with all of the query parameter values, the spaces must be URL encoded.) The GData service returns all entries that match all of the search terms (like using <code>AND</code> between terms). Like Google's web search, a GData service searches on complete words (and related words with the same stem), not substrings.</li>
	 * 	<li>To search for an exact phrase, enclose the phrase in quotation marks: <code>q="exact&nbsp;phrase".</code></li>
	 * 	<li>To exclude entries that match a given term, use the form <code>q=-term</code>.</li>
	 * 	<li>The search is case-insensitive.</li>
	 * 	<li>Example: to search for all entries that contain the exact phrase "Elizabeth Bennet" and the word "Darcy" but don't contain the word "Austen", use the following query: <code>?q="Elizabeth Bennet" Darcy -Austen</code></li>
	 * </ul>
	 * Default null means select all.
	 * @param q the q to set. Not null.
	 */
	public void setQ(String q){this.put("q", q);}

	/** See setStartIndex(int).
	 * @return the startIndex which is 1 if none has been set.
	 */
	public int getStartIndex() {
		String s = this.get("start-index");
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e){
			return 1;//default
		}
	}

	/** If the URL has a style parameter, its value. Else the default style plain. */
	public Style getStyle(){
		String s=this.get("style");
		return Style.parseStyle(s);
	}
		
	/** 1-based index of the first result to be retrieved.
	 * <p>
	 * Note that this isn't a general cursoring mechanism. If you first 
	 * send a query with ?start-index=1&max-results=10 and then send 
	 * another query with ?start-index=11&max-results=10, the service 
	 * cannot guarantee that the results are equivalent to 
	 * ?start-index=1&max-results=20, because insertions and deletions 
	 * could have taken place in between the two queries.
	 * </p>
	 * Default value = 1. 
	 * @param startIndex the startIndex to set
	 */
	public void setStartIndex(int startIndex) {
		this.setStartIndex(Integer.toString(startIndex));
	}

	/** 1-based index of the first result to be retrieved.
	 * <p>
	 * Note that this isn't a general cursoring mechanism. If you first 
	 * send a query with ?start-index=1&max-results=10 and then send 
	 * another query with ?start-index=11&max-results=10, the service 
	 * cannot guarantee that the results are equivalent to 
	 * ?start-index=1&max-results=20, because insertions and deletions 
	 * could have taken place in between the two queries.
	 * </p>
	 * Default value = 1. 
	 * @param startIndex the startIndex to set. Must be parsable as a
	 * positive integer. Not null.
	 */
	public void setStartIndex(String startIndex) {
		this.put("start-index", startIndex);
	}

	public void setStyle(Style style){
		String s = style.toString();
		this.put("style", s);
	}
		
	public void setStyle(String style){this.put("style", style);}
		
	/** See setUpdatedMax(Date).
	 * If updatedMax has been set to an invalid string, undefined.
	 * @return the updatedMax
	 */
	public Timestamp getUpdatedMax() {
		String s = this.get("updated-max");
		if (s==null){
			return null;
		} else {
			return XMLDate.parse(s);
		}
	}

	/** Upper bound on the entry update date.
	 * <ul>
	 * 	<li>Use the RFC 3339 timestamp format. For example: <code>2005-08-09T10:57:00-08:00</code>.</li>
	 * 	<li>The lower bound is inclusive, whereas the upper bound is exclusive.</li>
	 * </ul>
	 * Default null means select all. 
	 * @param updatedMax the updatedMax to set. Not null.
	 */
	public void setUpdatedMax(Date updatedMax) {
		this.setUpdatedMax(XMLDate.format(updatedMax));
	}

	/** Upper bound on the entry update date.
	 * <ul>
	 * 	<li>Use the RFC 3339 timestamp format. For example: <code>2005-08-09T10:57:00-08:00</code>.</li>
	 * 	<li>The lower bound is inclusive, whereas the upper bound is exclusive.</li>
	 * </ul>
	 * Default null means select all. 
	 * @param updatedMax the updatedMax to set,
	 * must be in RTC 3339 format. Not null.
	 */
	public void setUpdatedMax(String updatedMax) {
		this.put("updated-max", updatedMax);
	}

	/** See setUpdatedMin.
	 * If updated-min has not been set, null.
	 * If updated-min has been set to an invalid string, undefined.
	 * @return the updatedMin
	 */
	public Timestamp getUpdatedMin() {
		String s = this.get("updated-min");
		if (s==null){
			return null;
		} else {
			return XMLDate.parse(s);
		}
	}

	/** Lower bound on the entry update date.
	 * <ul>
	 * 	<li>Use the RFC 3339 timestamp format. For example: <code>2005-08-09T10:57:00-08:00</code>.</li>
	 * 	<li>The lower bound is inclusive, whereas the upper bound is exclusive.</li>
	 * </ul>
	 * Default null means select all. 
	 * @param updatedMin the updatedMin to set. Not null.
	 */
	public void setUpdatedMin(Date updatedMin) {
		this.setUpdatedMin(XMLDate.format(updatedMin));
	}

	/** Lower bound on the entry update date.
	 * <ul>
	 * 	<li>Use the RFC 3339 timestamp format. For example: <code>2005-08-09T10:57:00-08:00</code>.</li>
	 * 	<li>The lower bound is inclusive, whereas the upper bound is exclusive.</li>
	 * </ul>
	 * Default null means select all. 
	 * @param updatedMin the updatedMin to set,
	 * must be in RTC 3339 format. Not null.
	 */
	public void setUpdatedMin(String updatedMin){
		this.put("updated-min", updatedMin);
	}

	/** Returns an XML response with indentations and line breaks.
	 * If prettyprint=true, the XML returned by the server will be 
	 * human readable (pretty printed). Default: prettyprint=false
	 * */
	public boolean getPrettyprint(){
		String s=this.get("prettyprint");
		return Boolean.parseBoolean(s);//default: false
	}

	/** Returns an XML response with indentations and line breaks.
	 * If prettyprint=true, the XML returned by the server will be 
	 * human readable (pretty printed). Default: prettyprint=false
	 * */
	public void setPrettyprint(boolean prettyprint){
		this.put("prettyprint", Boolean.toString(prettyprint));
	}
	
	/** Sets an int parameter.
	 * Just a shortcut for put(key, Integer.toString(value)).
	 * @param key
	 * @param value
	 * @return Object 
	 */
	public String put(String key, int value){
		String s = Integer.toString(value);
		return this.put(key, s);
	}
	
	/** only debug */
	@Override public String toString(){
		//takes care of id
		if(this.id==null)return super.toString();
		return "id=" + this.id + ", " + super.toString();
	}
}