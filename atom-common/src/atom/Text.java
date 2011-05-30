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

import inspiracio.xml.XMLString;
import inspiracio.xml.sax.EasyContentHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/** A text-construct according to Atom 1.0 spec at 
 * http://atompub.org/rfc4287.html#text.constructs.
 * */
public abstract class Text {

	private String base;
	private String lang;
	
	/** Converts this text into an XML-DOM element.
	 * @param document where the element will go
	 * @param tag tag name of the element
	 * @return Element
	 * */
	abstract Element toElement(Document document, String tag);
	
	/** Writes this text into an XML-SAX content handler.
	 * @param handler where the element will go
	 * @param tag tag name of the element
	 * @exception SAXException
	 * */
	abstract void parse(EasyContentHandler handler, String tag) throws SAXException;
	
	/** Adds the common attributes to the Element that will
	 * represent this Text. 
	 * @param element */
	protected void addCommonAttributes(Element element){
		if (this.base!=null){
			element.setAttribute("base", this.base);
		}
		if (this.lang!=null){
			element.setAttribute("lang", this.lang);
		}
	}
	
	/** Parse a Text from an element. 
	 * @param element
	 * @return Text
	 * */
	static Text parse (Element element){
		Text text = null;
		//discriminate between plain text, html text, xhtml text:
		String type = element.getAttribute("type");
		if ("".equals(type) || "text".equals(type)){
			//type="text" ---the default
			//The element must have no subelements.
			String s = element.getTextContent();
			String unescaped=XMLString.unescape(s);
			text = new PlainText(unescaped);
		} else if ("html".equals(type)){
			//type="html" --HTMLText
			throw new RuntimeException("type=html not implemented");
		} else if ("xhtml".equals(type)){
			//type="xhtml" ---XHTMLText
			throw new RuntimeException("type=xhtml not implemented");
		}
		return text;
	}
	
	/** @return The text as plain unicode String */
	public abstract String format();
}