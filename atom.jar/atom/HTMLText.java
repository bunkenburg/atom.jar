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

import java.util.HashMap;
import java.util.Map;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/** A text-construct containing html. In the XML, the html will
 * be escaped. This class does the escaping, not the client. */
public class HTMLText extends Text {

	private String body;
	
	/** Constructs a text containing html.
	 * @param html Not escaped, this class does the escaping. */
	HTMLText(String html){
		this.body = html;
	}
	
	/** Converts this text into an XML-DOM element.
	 * @param document where the element will go
	 * @param tag tag name of the element
	 * @return Element
	 * */
	Element toElement(Document document, String tag){
		Element element = document.createElement(tag);
		element.setAttribute("type", "html");
		DOM.addText(document, element, this.body);
		return element;
	}

	/** Converts this text into an XML-DOM element.
	 * @param handler
	 * @param tag tag name of the element
	 * @exception SAXException
	 * */
	void parse(EasyContentHandler handler, String tag) throws SAXException {
		Map<String, String> atts = new HashMap<String, String>();
		atts.put("type", "html");
		handler.element(tag, atts, this.body);
	}

	/** @return The text as plain unicode String */
	public String format(){
		throw new RuntimeException("not implemented");
	}
}