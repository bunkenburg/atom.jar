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
package inspiracio.servlet.http;

import inspiracio.util.ListEnumeration;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;


import org.apache.log4j.Logger;

/** A cache, implementing HttpSession.
 * <p>
 * <h3>Threads</h3>
 * The attributes map is threadsafe.
 * */
public class CachingHttpSession extends DummyHttpSession implements HttpSession {
	private static Logger logger=Logger.getLogger(CachingHttpSession.class);

	//State -------------------------------------------------------------

	/** The attributes of the session.
	 * I use Hashtable because its methods are synchronized, and
	 * in fact they are synchronized on the hashtable instance.
	 * That means, in this class I can also synchronise on the same object,
	 * the hashtable itself.
	 * That way, I can make the methods in this class threadsafe. */
	private Map<String, Object> attributes=new Hashtable<String,Object>();

	/** List of all the listeners, in the order in which they were registered.
	 * Access must be protected by synchronized(this.listeners). */
	private List<HttpSessionAttributeListener> listeners=new ArrayList<HttpSessionAttributeListener>();

	/** Maps registered listeners to the ones in this.listeners, which may be wrapped.
	 * Access must be protected by synchronized(this.listeners). */
	private Map<HttpSessionAttributeListener, HttpSessionAttributeListener> map=new HashMap<HttpSessionAttributeListener, HttpSessionAttributeListener>();

	//Constructors ------------------------------------------------------

	public CachingHttpSession(){}

	//Business methods --------------------------------------------------

	/** Gets a session attribute, or null. */
	@Override public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	/** Gets the keys of all the attributes.
	 * @return Returns them as an old-fashioned enumeration,
	 * because that's what the old-fashioned HttpSession interface says.. */
	@Override public Enumeration<String> getAttributeNames(){
		List<String>names=new ArrayList<String>();
		synchronized(this.attributes){
			for(String key : this.attributes.keySet())
				names.add(key);
		}
		return new ListEnumeration<String>(names);
	}

	/** Gets an enumeration of the session attributes that match a pattern.
	 * For example, if the pattern is "form-.*", will return attributes with
	 * keys "form-11", "form-12", "form-" and so on.
	 * @return Set of String. Client keeps the set and can iterate over it. */
	public Set<String> getAttributeNames(String pattern){
		Set<String>names=new HashSet<String>();
		synchronized(this.attributes){
			for(String key : this.attributes.keySet())
				if(key.matches(pattern))
					names.add(key);
		}
		return names;
	}

	/** Gets the all the attributes of the session.
	 * Like Map.entrySet(), but the set is a copy:
	 * Client can add and remove session attributes
	 * while iterating over the result of this method.
	 * @return Set of Map.Entry. Client must not call setKey or setValue,
	 * 	that would affect the current session attributes.
	 * */
	public Set<Map.Entry<String, Object>> getAttributes(){
		Set<Map.Entry<String, Object>>atts=new HashSet<Map.Entry<String, Object>>();
		synchronized(this.attributes){
			for(Map.Entry<String, Object> e : this.attributes.entrySet())
				atts.add(e);
		}
		return atts;
	}

	/** Gets the all the attributes of the session,
	 * with keys that match a pattern.
	 * Like Map.entrySet(), but the set is a copy:
	 * Client can add and remove session attributes
	 * while iterating over the result of this method.
	 * @param pattern
	 * @return Set of Map.Entry. Client must not call setKey or setValue,
	 * 	that would affect the current session attributes.
	 * */
	public Set<Map.Entry<String, Object>> getAttributes(String pattern){
		Set<Map.Entry<String, Object>>atts=new HashSet<Map.Entry<String, Object>>();
		synchronized(this.attributes){
			for(Map.Entry<String, Object> e : this.attributes.entrySet())
				if(e.getKey().matches(pattern))
					atts.add(e);
		}
		return atts;
	}

	/** Removes a session attribute, and notifies registered listeners. */
	@Override public void removeAttribute(String key) {
		Object value=this.attributes.remove(key);
		HttpSessionBindingEvent e = new HttpSessionBindingEvent(this, key, value);
		List<HttpSessionAttributeListener> copy=this.copyListeners();//copy, avoids concurrent modification
		for(HttpSessionAttributeListener listener : copy)
			listener.attributeRemoved(e);
	}

	/** Removes a session attribute, if it's equal to the non-null old value,
	 * otherwise does nothing.
	 * If it removes, also notifies registered listeners. 
	 * @param key
	 * @param value Will be argument in old.equals(value).
	 * */
	public void removeAttributeIfEqual(String key, Object value) {
		//logger.debug("removeAttributeIfEqual");
		//logger.debug("new value for key [" + key + "] is \"" + value + "\"");

		HttpSessionBindingEvent e=new HttpSessionBindingEvent(this, key, value);

		//No modification must come between get and remove.
		synchronized(this.attributes){
			Object old=this.attributes.get(key);

			//logger.debug("old value for key [" + key + "] is \"" + old + "\"");

			if(old==null)return;
			if(old.equals(value))
				this.attributes.remove(key);
			else
				return;
		}
		//Can notify the listeners after synchronized block.
		List<HttpSessionAttributeListener>copy=this.copyListeners();//copy, avoids concurrent modification
		for(HttpSessionAttributeListener listener : copy)
			listener.attributeRemoved(e);
	}

	/** Sets a session attribute, maybe overwriting old value,
	 * and notifies registered listeners of add or replace. */
	@Override public void setAttribute(String key, Object value) {
		HttpSessionBindingEvent e=new HttpSessionBindingEvent(this, key, value);
		Object old=this.attributes.put(key, value);
		List<HttpSessionAttributeListener>copy=this.copyListeners();//copy, avoids concurrent modification
		if(old==null){
			for(HttpSessionAttributeListener listener : copy)
				listener.attributeAdded(e);
		}else{
			for(HttpSessionAttributeListener listener : copy)
				listener.attributeReplaced(e);
		}
	}

	/** Sets a session attribute, if it's equal to the non-null old value,
	 * otherwise does nothing.
	 * If it sets, also notifies registered listeners of replace. */
	public void setAttributeIfEqual(String key, Object value) {
		logger.debug("setAttributeIfEqual(" + key + ", " + value + ")");
		HttpSessionBindingEvent e=new HttpSessionBindingEvent(this, key, value);
		
		//No modification must come between get and put.
		synchronized(this.attributes){
			Object old=this.attributes.get(key);
			logger.debug("   old value for key [" + key + "] is " + old);
			if(old==null)return;
			if(old.equals(value))
				this.attributes.put(key, value);
			else
				return;
		}
		//Can notify the listeners after synchronised block.
		List<HttpSessionAttributeListener> copy=this.copyListeners();//copy, avoids concurrent modification
		for(HttpSessionAttributeListener listener : copy)
			listener.attributeReplaced(e);
	}

	//Session attribute listeners -----------------------------------------

	/** Register a session attribute listener. */
	public void addSessionAttributeListener(HttpSessionAttributeListener listener){
		if(listener instanceof IHttpSessionAttributeListener){
			String key=((IHttpSessionAttributeListener)listener).getKey();
			this.addSessionAttributeListener(listener, key);
			return;
		}
		
		synchronized(this.listeners){
			this.listeners.add(listener);
			this.map.put(listener, listener);
		}
	}

	/** Register a listener for attribute with a certain key, or pattern of keys. */
	public void addSessionAttributeListener(final HttpSessionAttributeListener listener, final String key){
		//Wrap the listener in a listener that listens to everything, and then filters.
		HttpSessionAttributeListener wrapper=new HttpSessionAttributeListener(){
			@Override public void attributeAdded(HttpSessionBindingEvent e) {
				if(e.getName().matches(key))
					listener.attributeAdded(e);//This is a real registered listener.
			}
			@Override public void attributeRemoved(HttpSessionBindingEvent e) {
				if(e.getName().matches(key))
					listener.attributeRemoved(e);//This is a real registered listener.
			}
			@Override public void attributeReplaced(HttpSessionBindingEvent e){
				String name=e.getName();
				if(name.matches(key))
					listener.attributeReplaced(e);//This is a real registered listener.
			}
		};
		synchronized(this.listeners){
			this.listeners.add(wrapper);
			this.map.put(listener,wrapper);
		}
	}

	/** Unregisters a listener. If the listener is not registered, does nothing. */
	public void removeSessionAttributeListener(HttpSessionAttributeListener listener){
		synchronized(listener){
			HttpSessionAttributeListener wrapped=this.map.get(listener);//maybe unwrap it
			this.listeners.remove(wrapped);
			this.map.remove(listener);
		}
	}
	
	/** Returns a fresh array of all registered listeners. */
	public HttpSessionAttributeListener[] getSessionAttributeListeners(){
		Map<HttpSessionAttributeListener,HttpSessionAttributeListener>unwrap=new HashMap<HttpSessionAttributeListener,HttpSessionAttributeListener>();
		for(Map.Entry<HttpSessionAttributeListener, HttpSessionAttributeListener>e : map.entrySet()){
			HttpSessionAttributeListener listener=e.getKey();
			HttpSessionAttributeListener wrapped=e.getValue();
			unwrap.put(wrapped, listener);
		}
		
		HttpSessionAttributeListener[] array=new HttpSessionAttributeListener[this.listeners.size()];
		for(int i=0; i<array.length; i++){
			HttpSessionAttributeListener listener=listeners.get(i);
			listener=unwrap.get(listener);//unwrap
			array[i]=listener;
		}
		return array;
	}

	//Helpers ---------------------------------------------------------

	/** Gets a copy of the list of listeners.
	 * The client may iterate over the list and call the listeners,
	 * which may register or unregister listeners. The changes in
	 * registered listeners will not affect the returned list and
	 * thereby avoid ConcurrentModificationException. */
	private List<HttpSessionAttributeListener>copyListeners(){
		List<HttpSessionAttributeListener>copy=new ArrayList<HttpSessionAttributeListener>();
		synchronized(this.listeners){
			for(HttpSessionAttributeListener listener : this.listeners)
				copy.add(listener);
		}
		return copy;
	}

	/** only debug */
	@Override public String toString(){return this.map.toString();}
}