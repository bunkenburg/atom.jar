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

import inspiracio.servlet.http.HttpURL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import atom.Category;

/** A URL according to GData spec. 
 * See http://code.google.com/apis/gdata/reference.html#query-requests
 * */
public class GDataURL extends HttpURL {
	
	/** Construct the URL for the current request. */
	//public GDataURL(){
		//This would be useful. Now it is wrong.
		//Must get the query string from the request and add it.
		//super();
	//}
	
	/** Construct the URL of a HttpServletRequest 
	 * @param request Initialise with the URL of request */
	public GDataURL(HttpServletRequest request){
		super();
		StringBuffer buffer=request.getRequestURL();
		String queryString=request.getQueryString();
		if (queryString!=null){
			buffer.append('?');
			buffer.append(queryString);
		}
		String s=buffer.toString();
		this.setCompleteURL(s);
	}
	
	/** Construct the URL from a String 
	 * @param url String of complete URL */
	protected GDataURL(String url){
		super(url);
	}
	
	/** Gets the categories in the URL. 
	 * @return list of categories, may be empty */
	public List<Category> getCategories(){
		
		List<Category> categories = new ArrayList<Category>();
		boolean finished = false;
		String url = this.toString();
		
		//find "/-/"
		int separatorStart = url.indexOf("/-/");
		int categoriesStart = separatorStart + 3;
		
		//match all the "/something" excluding "entryN" which is a special parameter
		while(!finished){
			if (3 < categoriesStart){
				int categoryEnd = url.indexOf("/", categoriesStart);
				if(categoryEnd < 0){ // Might be the last category
					categoryEnd = url.indexOf("?", categoriesStart);
					if(categoryEnd < 0) // No more categories
						categoryEnd = url.length();
					finished = true;
				}
				String term = url.substring(categoriesStart,categoryEnd);
				if(!term.startsWith("entry")){	// exclude "entryID" from categories
					//Build the Category
					Category category = new Category();
					category.setTerm(term);
					categories.add(category);
				}
				
				// Update offset
				categoriesStart = categoryEnd+1;
			} else
				finished = true;
		}
		return categories;
	}
	
	/** Gets the gdata parameters parsed from the URL.
	 * They do not include the categories which are available
	 * from a separate method. 
	 * @return fresh instance of Parameters 
	 * @exception RuntimeException containing ParseException 
	 * 	Dates must be in RTC 3339 format. 
	 * */
	//public Map getParameters() throws RuntimeException {
	public GDataParameters getParameters() throws RuntimeException {
		Map<String, String> ps=super.getParameters();
		GDataParameters gdp=new GDataParameters(ps);
		
		//Parse entry-id, which is not a URL parameter
		//Example:
		//http://domain/atom/-/offer/entry613201229313895273802234030495?style=short&author=empresaSILKEN
		//According to GData spec, if entry-id is present, no other parameters
		//are allowed, but we weaken that. URL parameters are allowed: it makes
		//sense for us, because some of our parameters are not search parameters,
		//but rather affect presentation, like "style".
		StringBuilder url=this.toStringBuilder();
		//search in the URL for "/entryNNN" or "/entryNNN?"
		int i=url.indexOf("/entry");
		if(0 <= i){
			int start = i + "/entry".length();
			int end = url.indexOf("?", i);
			if (end < 0){
				end = url.length();
			}
			String id = url.substring(start, end);
			gdp.setId(id);
		}
		
		return gdp;
	}
	
	//testing ------------------------------------------------
	
	/** testing 
	 * @param args 
	 * @exception Exception */
	public static void main(String[] args) throws Exception {
		String s = 
			"http://localhost:8500/atom/-/offer?" +
			"updated-min=2007-04-19T15:30:00&" +
			"updated-max=2007-04-19T15:30:00&" +
			"q=Bunken&" + 
			"author=Shakespeare&" +
			"alt=rss&" +
			"published-min=2007-04-19T15:30:00&" +
			"published-max=2007-04-19T15:30:00&" +
			"start-index=17&" +
			"max-results=30&" +
			"style=short";
		GDataURL url = new GDataURL(s);
		GDataParameters p = url.getParameters();
		say(p);
		Map<String, String> ad = url.getParameters();
		say(ad);
	}

	/** @param o */
	static void say(Object o){System.out.println(o);}
}