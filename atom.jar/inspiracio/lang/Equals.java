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
package inspiracio.lang;

import java.util.Collection;

/** Alternatives implementations of == and Object.equals(..)*/
public class Equals {

	/** Forbid instantiation. */
	private Equals(){}
	
	/** Compares for equality, equating null with null, and treating
	 * Number instances almost mathematically.
	 * <p>
	 * Algorithm:
	 * <ol>
	 * 	<li>If one argument is null, the other must be null too.
	 * 	<li>If both are integral (Byte, Short, Integer, Long), exact mathematical equality.
	 * 	<li>If both are numeric (integral or Float, Double), approximate double-equality.
	 * 	<li>normal Object.equals(..)
	 * </ol>
	 * */
	public static boolean equals(Object a, Object b){
		if(a==b)
			return true;//Optimisation
		if(a==null)
			return b==null;
		if(b==null)
			return a==null;
		if(a instanceof Number && b instanceof Number){
			//Two numbers. I cannot use Object.equals(..)
			//because maybe they are the same number mathematically,
			//but instances of different classes, for example Integer and Long.
			//new Integer(0).equals(new Long(0)) is false.
			
			//Are both integral numbers?
			if(
				(a instanceof Byte || a instanceof Short || a instanceof Integer || a instanceof Long)
			&&	(b instanceof Byte || b instanceof Short || b instanceof Integer || b instanceof Long)
			){
				//Both are integral numbers. Let's go to long, without losing precision.
				long aLong=((Number)a).longValue();
				long bLong=((Number)b).longValue();
				return aLong==bLong;
			}
			
			//At least one of them is floating-point. Go to double, maybe loss of precision.
			double aDouble=((Number)a).doubleValue();
			double bDouble=((Number)b).doubleValue();
			//Look at the bits. See http://www.javapractices.com/topic/TopicAction.do?Id=17.
			long aLong=Double.doubleToLongBits(aDouble);
			long bLong=Double.doubleToLongBits(bDouble);
			return aLong==bLong; 
		}
		return a.equals(b);//Normal Object.equals(..).
	}
	
	/** Are two collections equal as sets?
	 * Copes with null. */
	public static <T> boolean equalSets(Collection<T>a, Collection<T>b){
		if(a==null)
			return b==null;
		if(b==null)
			return a==null;
		for(T t : a){
			if(!contains(b,t))
				return false;
		}
		for(T t : b){
			if(!contains(a,t))
				return false;
		}
		return true;
	}
	
	/** Checks whether a collection contains an element, based on Equals.equals. 
	 * Iterates over the collection. */
	public static <T> boolean contains(Collection<T> collection, T element){
		for(T t: collection)
			if(equals(t,element))
				return true;
		return false;
	}

}