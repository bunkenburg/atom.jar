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
package inspiracio.security;

import java.security.Principal;

/** A class that can make principals with more detail.
 * @author alex
 */
public interface PrincipalMaker {

	/** Gets the current user.
	 * If this maker is not appropriate now, return null.
	 * If this maker is appropriate, but there is not current user, also returns null. */
	Principal getCallerPrincipal();
}