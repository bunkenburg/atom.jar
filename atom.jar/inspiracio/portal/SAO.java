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
package inspiracio.portal;

/** SAO = Service Access Object.
 * <p>
 * A service is a functionality that is accessibly remotely,
 * via Atom, RSS, web service, or similar. There is a SAO for
 * each business object.
 * </p>
 * <p>
 * Since SAOs should be useful for many different technologies,
 * here we impose almost no restriction on them. This is a marker
 * interface. Possible implementations should include:
 * EJB Entity Home, EJB Stateless Session Bean, plain Java.
 * </p>
 * */
public interface SAO {}