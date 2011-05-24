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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/** A Map useful for String->String, which is a common case.
 * The map can only contain acceptable entries. An entry is
 * acceptable if the value is not null and not "".
 * */
public class StringMap extends MapWrapper<String,String> {

	/** Construct from the acceptable entries in a map.
	 * The unacceptable entries are skipped.
	 * @param map the underlying map.
	 * */
	public StringMap(Map<String,String> map){
		super(new TreeMap<String,String>());
		this.putAll(map);
	}

	/** Puts an acceptable entry in the map.
	 * @param key
	 * @param value
	 * @return Object 
	 * @exception IllegalArgumentException The entry is unacceptable.
	 */
	public String put(String key, String value){
		if (value!=null){
			return super.put(key, value);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	/** Puts all the entries of a map in this map.
	 * The entries must be acceptable.
	 * @param t
	 * */
	public void putAll(Map<? extends String,? extends String> t){
		Iterator<? extends Map.Entry<? extends String, ? extends String>> it = t.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<? extends String, ? extends String> entry = it.next();
			String value = entry.getValue();
			if (value!=null && !"".equals(value)){
				String key = entry.getKey();
				this.put(key, value);
			} else {
				//skip the unacceptable entry
			}
		}
	}
}