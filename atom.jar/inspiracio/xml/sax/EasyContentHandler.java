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

import inspiracio.xml.XMLDate;
import inspiracio.xml.XMLString;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;


import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/** Adds some convenient methods to a content handler. */
public class EasyContentHandler extends ContentHandlerWrapper{

	//Constructors ------------------------------------------------
	
	/** Construct from a delegate 
	 * @param delegate 
	 * */
	public EasyContentHandler(ContentHandler delegate){
		super(delegate);
	}
	
	//SAX event handling methods ----------------------------------
	
	/** Starts an element. No automatic namespace processing.
	 * @param tag The tag name as it will appear in the XML,
	 * 	whether it's qualified or not.
	 * @param attributes The attributes as Map from key to value.
	 * 	The keys of the map are the keys of the attributes, whether
	 * 	they are qualified or not.
	 * @exception SAXException
	 * */
	public void startElement(String tag, Map<String, String> attributes) throws SAXException {
		//Assume the XMLReader does not perform namespace processing.
		//So, we must always have localName = "" and qName =<desired tag name>.
		AttributesImpl attributesImpl = new AttributesImpl();
		Iterator<Map.Entry<String, String>> entries = attributes.entrySet().iterator();
		while (entries.hasNext()){
			Map.Entry<String, String> entry = entries.next();
			String qName = (String)entry.getKey();
			String value = (String)entry.getValue();
			attributesImpl.addAttribute(
				"" /* namespaceURI: no namespace processing */, 
				"" /* localName: no namespace processing */,
				qName, 
				null /* type: What are the values? */, 
				value
			);
		}
        this.startElement(
        	"", //namespaceURI: no namespace processing
        	"", //localName: no namespace processing
        	tag,
        	attributesImpl
        );//SAXException
	}

	/** Starts an element. No automatic namespace processing.
	 * @param tag The tag name as it will appear in the XML,
	 * 	whether it's qualified or not.
	 * @exception SAXException
	 * */
	public void startElement(String tag) throws SAXException {
		//Assume the XMLReader does not perform namespace processing.
		//So, we must always have localName = "" and qName =<desired tag name>.
		Attributes attributes = new AttributesImpl();
        this.startElement(
        	"", //namespaceURI: no namespace processing
        	"", //localName: no namespace processing
        	tag,
        	attributes
        );//SAXException
	}

	/** Ends an element. 
	 * @param tag The tag name as it will appear in the XML,
	 * 	whether it's qualified or not.
	 * @exception SAXException
	 * */
	public void endElement(String tag) throws SAXException {
		this.endElement(
			"" /* namespaceURI: no namespace processing*/,
			"" /* localName: no namespace processing */, 
			tag
		);//SAXException
	}
	
	/** Adds an empty element. 
	 * @param tag
	 * @exception SAXException
	 * */
	public void element(String tag) throws SAXException {
		this.startElement(tag);
		this.endElement(tag);
	}
	
	/** Adds a simple element with a String as body. 
	 * @param tag
	 * @param body Unescaped.
	 * @exception SAXException
	 * */
	public void element(String tag, String body) throws SAXException {
		this.startElement(tag);
		this.characters(body);
		this.endElement(tag);
	}
	
	/** Adds a simple element with an int as body. 
	 * @param tag
	 * @param body
	 * @exception SAXException
	 * */
	public void element(String tag, int body) throws SAXException {
		this.startElement(tag);
		this.characters(Integer.toString(body));
		this.endElement(tag);
	}
	
	/** Adds a simple element with some attributes and a String as body. 
	 * @param tag
	 * @param attributes
	 * @param body Unescaped.
	 * @exception SAXException
	 * */
	public void element(String tag, Map<String, String> attributes, String body) throws SAXException {
		this.startElement(tag, attributes);
		this.characters(body);
		this.endElement(tag);
	}
	
	/** Adds a simple element with some attributes. 
	 * @param tag
	 * @param attributes
	 * @exception SAXException
	 * */
	public void element(String tag, Map<String, String> attributes) throws SAXException {
		this.startElement(tag, attributes);
		this.endElement(tag);
	}
	
	/** Adds a simple element with a date as body.
	 * @param tag
	 * @param date 
	 * @exception SAXException
	 * */
	public void element(String tag, Date date) throws SAXException {
		String body = XMLDate.format(date);
		this.startElement(tag);
		this.characters(body);
		this.endElement(tag);
	}

	/** Adds a simple element with a date as body.
	 * @param tag
	 * @param date 
	 * @param pattern for formatting the date
	 * @exception SAXException
	 * */
	public void element(String tag, Date date, String pattern) throws SAXException {
		SimpleDateFormat df=new SimpleDateFormat(pattern);
		String body=df.format(date);
		this.startElement(tag);
		this.characters(body);
		this.endElement(tag);
	}
	
	/** Escapes a String for XML and adds it to the content handler.
	 * @param s 
	 * @exception SAXException
	 * */
	public void characters(String s) throws SAXException{
		String escaped=XMLString.escape(s);
		this.characters(escaped.toCharArray(), 0, escaped.length());
	}
}