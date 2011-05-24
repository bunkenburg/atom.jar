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
package atom;

import java.util.List;

/** Service documents according to http://tools.ietf.org/html/rfc5023.
 * <p>
 * For authoring to commence, a client needs to discover the
 * capabilities and locations of the available Collections.  Service
 * Documents are designed to support this discovery process.
 * </p>
 * <p>
 * Service Documents are identified with the "application/atomsvc+xml"
 * media type (see Section 16.2).
 * </p>
 * <p>
 * A Service Document groups Collections into Workspaces.  Operations on
 * Workspaces, such as creation or deletion, are not defined by this
 * specification.  This specification assigns no meaning to Workspaces;
 * that is, a Workspace does not imply any specific processing
 * assumptions.
 * </p>
 * <p>
 * There is no requirement that a server support multiple Workspaces.
 * In addition, a Collection MAY appear in more than one Workspace.
 * */
public class ServiceDocument {

	private List<Workspace> workspaces;
	
	/** Add a workspace 
	 * @param workspace
	 * */
	public void addWorkspace(Workspace workspace){
		this.workspaces.add(workspace);
	}
	
	/** Get the workspaces.
	 * @return List
	 * */
	public List<Workspace> getWorkspaces(){
		return this.workspaces;
	}
}