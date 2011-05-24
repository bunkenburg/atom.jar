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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Wraps a given map and delegates everything to it. 
 * This class exists so that subclasses can add to the functionality
 * of Map. 
 * 
 * <p>
 * I like small interesting classes and big boring classes. This is a big boring class.
 * </p>
 * 
 * @param <K> - generic type for the key
 * @param <V> - generic type for the value
 * */
public class MapWrapper<K,V> implements Map<K,V> {

	//State ---------------------------------------------
	
	private Map<K,V> delegate;
	
	//Constructors -------------------------------------
	
	/** Construct a MapWrapper. 
	 * @param delegate
	 * */
	protected MapWrapper(Map<K,V> delegate){this.delegate=delegate;}

	//Methods ----------------------------------------
	
	@Override public int size(){return delegate.size();}
	@Override public boolean isEmpty(){return delegate.isEmpty();}
	@Override public boolean containsKey(Object key){return delegate.containsKey(key);}
	@Override public boolean containsValue(Object value){return delegate.containsValue(value);}
	@Override public V get(Object key){return delegate.get(key);}
	@Override public V put(K key, V value){return delegate.put(key, value);}
	@Override public V remove(Object key){return delegate.remove(key);}
	@Override public void putAll(Map<? extends K,? extends V> t){delegate.putAll(t);}
	@Override public void clear(){delegate.clear();}
	@Override public Set<K> keySet(){return delegate.keySet();}
	@Override public Collection<V> values(){return delegate.values();}
	@Override public Set<Entry<K,V>> entrySet(){return delegate.entrySet();}
	@Override public boolean equals(Object o){return delegate.equals(o);}
	@Override public int hashCode(){return delegate.hashCode();}
	@Override public String toString(){return delegate.toString();}
	
}