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
package inspiracio.transaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

/** Within a container that has a JTA implementation (for example a JEE container),
 * produce UserTransaction objects. */
public class TransactionFactory {

	/** prevent instantiation */
	private TransactionFactory(){}
	
	/** Gets a UserTransaction object. 
	 * Needs deploy/transaction-service.xml. */
	public static UserTransaction getUserTransaction()throws NamingException{
		InitialContext ctx=new InitialContext();//NamingException
		String name="java:comp/UserTransaction";
		Object o=ctx.lookup(name);//NamingException
		UserTransaction tx=(UserTransaction)o;
		return tx;
	}
}