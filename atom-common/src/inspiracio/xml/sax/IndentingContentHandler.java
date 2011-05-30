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
package inspiracio.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/** A content handler that writes every XML element on a new line,
 * and indents the nested elements.
 * <p>
 * Puts every opening tag and every closing tag on a line by itself,
 * and all the content of the element is indented.
 * <p>
 * You could spend a lot of time making it better. 
 * */
public class IndentingContentHandler extends ContentHandlerWrapper {

	//State ---------------------------------------------------------

	/** current indentation level */
	private int indent=0;
	
	/** At the start of a fresh line? */
	private boolean fresh=true;
	
	//Constructors --------------------------------------------------
	
	/**Construct, wrapping a content handler. 
	 * @param delegate 
	 * */
	public IndentingContentHandler(ContentHandler delegate){
		super(delegate);
	}

	//SAX event handling methods ------------------------------------
	
	/** Starts XML document. 
	 * @exception SAXException
	 * */
	public void startDocument() throws SAXException {
		this.indent=0;
		newline();//to go into next line after <?xml version="1.0" encoding="UTF-8"?>
		super.startDocument();
	}

	/** End of XML document. 
	 * @exception SAXException
	 * */
	public void endDocument() throws SAXException {
		super.endDocument();
		if(this.indent!=0)throw new RuntimeException();
	}

	/** Starts XML element
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param arg3
	 * @exception SAXException
	 * */
	public void startElement(String uri, String localName, String qName, Attributes arg3) throws SAXException {
		if(!fresh)newline();
		indent();
		super.startElement(uri, localName, qName, arg3);//<tag ...>
		indent++;//element content is indented a bit more
		newline();//element content also starts on a fresh line
	}

	/** End of XML element. 
	 * @param uri
	 * @param localName
	 * @param qName
	 * @exception SAXException
	 * */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(!fresh)newline();
		indent--;
		indent();
		super.endElement(uri, localName, qName);//</tag>
		newline();
	}

	/** Write some chars. 
	 * @param ch
	 * @param start
	 * @param length
	 * @exception SAXException
	 * */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(fresh)indent();
		super.characters(ch, start, length);
		fresh=false;
	}

	//helpers -----------------------------------------------------------
	
	/** insert a newline and a indentation level tabs */
	private void newline()throws SAXException{
		char[] cs = new char[1];
		cs[0] = '\n';//this okay as newline on various platforms?
		super.characters(cs, 0, 1);//SAXException
		this.fresh=true;
	}
	
	private void indent()throws SAXException{
		char[] cs = new char[this.indent];
		for(int i=0; i<this.indent; i++)cs[i] = '\t';
		super.characters(cs, 0, this.indent);//SAXException
	}
}