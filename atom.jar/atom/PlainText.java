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

import inspiracio.xml.DOM;
import inspiracio.xml.sax.EasyContentHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/** Plain text in Atom 1.0 */
public class PlainText extends Text {

	//State ------------------------------------
	
	/** the text, no escaping here */
	private String body;
	
	//Constructors -----------------------------
	
	/** Constructs a plain text containing some text.
	 * @param text Without escaping, this class does the escaping.
	 * */
	PlainText(String text){
		this.body=text;
	}
	
	//Methods ----------------------------------
	
	/** Converts this text into an XML-DOM element.
	 * @param document where the element will go
	 * @param tag tag name of the element
	 * @return Element
	 * */
	Element toElement(Document document, String tag){
		Element element = document.createElement(tag);
		this.addCommonAttributes(element);
		//Do not output "text" because it is the default anyway.
		//element.setAttribute("type", "text");
		DOM.addText(document, element, this.body);
		return element;
	}


	/** Converts this text into an XML-DOM element.
	 * @param handler
	 * @param tag tag name of the element
	 * @exception SAXException
	 * */
	void parse(EasyContentHandler handler, String tag) throws SAXException {
		//Map<String, String> atts = new HashMap<String, String>();
		//atts.put("type", "text");//Do not output because it is the default.
		handler.element(tag, this.body);
	}

	/** @return The text as plain unicode String */
	public String format(){return body;}

	/** For debug.
	 * @return The text as plain unicode String */
	public String toString(){return this.body;}
}