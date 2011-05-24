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
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/** Delegates everything. Extend in order to add or override methods. */
public class ContentHandlerWrapper implements ContentHandler{
	
	private ContentHandler delegate=null;
	
	/**Construct, wrapping a content handler. 
	 * @param delegate 
	 * */
	public ContentHandlerWrapper(ContentHandler delegate){
		this.delegate=delegate;
	}

	/** Write some chars. 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @exception SAXException
	 * */
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		this.delegate.characters(arg0, arg1, arg2);
	}

	/** End of XML document. 
	 * @exception SAXException
	 * */
	public void endDocument() throws SAXException {
		this.delegate.endDocument();
	}

	/** End of XML element. 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @exception SAXException
	 * */
	public void endElement(String arg0, String arg1, String arg2) throws SAXException {
		this.delegate.endElement(arg0, arg1, arg2);
	}

	/** end prefix mapping
	 * @param arg0 
	 * @exception SAXException
	 * */
	public void endPrefixMapping(String arg0) throws SAXException {
		this.delegate.endPrefixMapping(arg0);
	}

	/** @param arg0
	 * @param arg1
	 * @param arg2
	 * @exception SAXException
	 * */
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
		this.delegate.ignorableWhitespace(arg0, arg1, arg2);
	}

	/** @param arg0
	 * @param arg1
	 * @exception SAXException
	 * */
	public void processingInstruction(String arg0, String arg1) throws SAXException {
		this.delegate.processingInstruction(arg0, arg1);
	}

	/** @param arg0 */
	public void setDocumentLocator(Locator arg0) {
		this.delegate.setDocumentLocator(arg0);
	}

	/** @param arg0 
	 * @exception SAXException
	 * */
	public void skippedEntity(String arg0) throws SAXException {
		this.delegate.skippedEntity(arg0);
	}

	/** Starts XML document. 
	 * @exception SAXException
	 * */
	public void startDocument() throws SAXException {
		this.delegate.startDocument();
	}

	/** Starts XML element
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @exception SAXException
	 * */
	public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
		this.delegate.startElement(arg0, arg1, arg2, arg3);
	}

	/** @param arg0
	 * @param arg1
	 * @exception SAXException
	 * */
	public void startPrefixMapping(String arg0, String arg1) throws SAXException {
		this.delegate.startPrefixMapping(arg0, arg1);
	}

}