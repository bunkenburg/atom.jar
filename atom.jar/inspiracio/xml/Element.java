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
package inspiracio.xml;

import inspiracio.xml.sax.EasyContentHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Simple representation of an XML element. Similar to DOM,
 * but without using DOM. If you want to manipulate a whole
 * XML-document with DOM; use DOM or JDOM. If you want to 
 * make little bits of XML and use toString for formatting,
 * use this. 
 * */
public class Element {

	//State ------------------------------------------------------------
	
	private String tag;
	
	/** The children can be Element or String or Double or Long. 
	 * The strings in here are not escaped, they are the real strings. */
	private List<Object> children=new ArrayList<Object>();
	
	/** The attributes of this element. The values are not escaped, they
	 * are the real strings. */
	private Map<String, String> attributes=new TreeMap<String, String>();
	
	//Constructors -----------------------------------------------------
	
	/** Construct an element with the identified tag.
	 * @param tag
	 * */
	public Element(String tag){this.tag=tag;}
	
	/** Construct an element with the identified tag and
	 * text content. Escapes the text so that it is safe for 
	 * XML.
	 * @param tag
	 * @param text
	 * */
	public Element(String tag, String text){
		this.tag = tag;
		if (text!=null && 0 < text.length()){
			this.children.add(text);
		}
	}
	
	/** Construct an element with the identified tag and
	 * double content. 
	 * @param tag
	 * @param number
	 * */
	public Element(String tag, double number){
		this.tag = tag;
		Double o = new Double(number);
		this.children.add(o);
	}
	
	/** Construct an element with the identified tag and
	 * long content. 
	 * @param tag
	 * @param number
	 * */
	public Element(String tag, long number){
		this.tag = tag;
		Long o = new Long(number);
		this.children.add(o);
	}
	
	/** Construct an element with the identified tag and
	 * boolean content. 
	 * @param tag
	 * @param bool
	 * */
	public Element(String tag, boolean bool){
		this.tag = tag;
		this.children.add(Boolean.toString(bool));
	}
	
	/** Constructs an element from a DOM-element. 
	 * @param element
	 * */
	public Element(org.w3c.dom.Element element){
		this(element.getTagName());
		
		//Copy attributes
		NamedNodeMap attributes = element.getAttributes();
		for (int i=0; i<attributes.getLength(); i++){
			Node n = attributes.item(i);//n instance of Attr
			String name = n.getNodeName();
			String value = n.getNodeValue();
			this.setAttribute(name, value);
		}
		
		//Copy children
		NodeList children = element.getChildNodes();
		for (int i=0; i<children.getLength(); i++){
			Node child = children.item(i);
			//discriminate node type
			short type = child.getNodeType();
			switch (type){
			case Node.ATTRIBUTE_NODE:
				//Never enters here. Attributes don't count as children of an Element?
				String name = child.getNodeName();
				String value = child.getNodeValue();
				this.setAttribute(name, value);
				break;
			case Node.CDATA_SECTION_NODE: 
				//Could implement CDATA section as a third kind of child besides Element and String.
				throw new RuntimeException("not implemented");
				//break;
			case Node.COMMENT_NODE: 
				//We ignore comments
				break;
			case Node.DOCUMENT_FRAGMENT_NODE: 
				throw new RuntimeException("not implemented");
				//break;
			case Node.DOCUMENT_NODE: 
				throw new RuntimeException("not implemented");
				//break;
			case Node.DOCUMENT_TYPE_NODE: 
				throw new RuntimeException("not implemented");
				//break;
			case Node.ELEMENT_NODE: 
				org.w3c.dom.Element childElement = (org.w3c.dom.Element)child;
				Element e = new Element(childElement);
				this.addChild(e);
				break;
			case Node.ENTITY_NODE: 
				throw new RuntimeException("not implemented");
				//break;
			case Node.ENTITY_REFERENCE_NODE: 
				throw new RuntimeException("not implemented");
				//break;
			case Node.NOTATION_NODE: 
				throw new RuntimeException("not implemented");
				//break;
			case Node.PROCESSING_INSTRUCTION_NODE: 
				throw new RuntimeException("not implemented");
				//break;
			case Node.TEXT_NODE: 
				String text = child.getNodeValue();//Text is escaped. Must anti-escape still.
				text = XMLString.unescape(text);
				this.addChild(text);
				break;
			}
		}
	}
	
	//Getters and setters ---------------------------------------------------
	
	/** @return the tag */
	public String getTag(){
		return this.tag;
	}
	
	/** Sets or overrides an attribute.
	 * @param key Assumed to be a valid XML attribute key.
	 * @param value Can be any String, without escaping.
	 * */
	public void setAttribute(String key, String value){
		this.attributes.put(key, value);
	}
	
	/** Adds a child element at the end of the children.
	 * @param child */
	public void addChild(Element child){
		this.children.add(child);
	}
	
	/** Removes a child element.
	 * @param child */
	public void removeChild(Element child){
		this.children.remove(child);
	}
	
	/** Adds a text node at the end of the children.
	 * @param text Unescaped text */
	public void addChild(String text){
		this.children.add(text);
	}
	
	/** Gives an String representation of the element.
	 * Escapes everything. The returned string has no 
	 * indentation.
	 * @return partial XML-String */
	public String toString(){
		StringBuilder buffer=new StringBuilder();
		buffer.append('<');
		buffer.append(this.tag);
		
		//attributes
		Iterator<Map.Entry<String,String>> it=this.attributes.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<String, String> entry = it.next();
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			String valueEscaped = XMLString.escape(value);
			buffer.append(' ');
			buffer.append(key);
			buffer.append("=\"");
			buffer.append(valueEscaped);
			buffer.append('\"');
		}
		
		if (this.children.size()==0){
			//maybe <element/> abbreviated form
			buffer.append("/>");
		} else {
			//there are some children
			buffer.append('>');
			Iterator<Object> cit = this.children.iterator();
			while (cit.hasNext()){
				Object child = cit.next();
				if(child instanceof String){
					String escaped=XMLString.escape(child.toString());
					buffer.append(escaped);
				}else if(child instanceof Element){
					buffer.append(child.toString());
				} else {
					//Double or Long
					buffer.append(child.toString());
				}
			}
			buffer.append("</");
			buffer.append(this.tag);
			buffer.append('>');
		}
		return buffer.toString();
	}
	
	/** Converts this element to an Element of XML-DOM.
	 * @param document The document where the element will go.
	 * @return org.w3c.dom.Element
	 * */
	public org.w3c.dom.Element toElement(Document document){
		org.w3c.dom.Element element = document.createElement(this.tag);

		//attributes
		Iterator<Map.Entry<String,String>> it = this.attributes.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			//Must escape the value.
			String valueEscaped = XMLString.escape(value);
			element.setAttribute(key, valueEscaped);
		}
		
		//child elements and strings
		Iterator<Object> children = this.children.iterator();
		while (children.hasNext()){
			Object child = children.next();
			org.w3c.dom.Node childNode = null;
			if (child instanceof Element){
				childNode = ((Element)child).toElement(document);
			} else if (child instanceof String){
				String s = child.toString();
				childNode = document.createTextNode(s);
			} else {
				//Double or Long
				String s = child.toString();
				childNode = document.createTextNode(s);
			}
			element.appendChild(childNode);
		}
		return element;
	}

	/** Writes this element to a XML-SAX content handler.
	 * @param handler
	 * @exception SAXException
	 * */
	public void parse(EasyContentHandler handler) throws SAXException {
		handler.startElement(this.tag, this.attributes);

		//child elements and strings
		Iterator<Object> it = this.children.iterator();
		while (it.hasNext()){
			Object child = it.next();
			if (child instanceof Element){
				Element e = (Element)child;
				e.parse(handler);
			} else if (child instanceof String){
				String s = child.toString();
				handler.characters(s);//Escapes
			} else if (child instanceof Double){
				String s = child.toString();
				handler.characters(s);
			} else if (child instanceof Long){
				String s = child.toString();
				handler.characters(s);
			} else {
				new Exception(child.toString()).printStackTrace();
			}
		}

		handler.endElement(this.tag);
	}
	
	/** Gets the first child element with this tag.
	 * Careful: throws RuntimeException.
	 * @param tag
	 * @return an Element
	 * @throws RuntimeException No such child element.
	 * */
	public Element getChildElement(String tag){
		Iterator<Object> it = this.children.iterator();
		while (it.hasNext()){
			Object child = it.next();
			if (child instanceof Element){
				Element childElement = (Element)child;
				if (tag.equals(childElement.getTag())){
					return childElement;
				}
			}
		}
		throw new RuntimeException("No child element with tag " + tag);//Improve this?
	}
	
	/** Gets all child elements with this tag
	 * @param tag
	 * @return a list with 0 or more Elements
	 * */
	public List<Element> getAllChildElements(String tag){
		List<Element> result = new LinkedList<Element>();
		Iterator<Object> it = this.children.iterator();
		while (it.hasNext()){
			Object child = it.next();
			if (child instanceof Element){
				Element childElement = (Element)child;
				if (tag.equals(childElement.getTag())){
					result.add(childElement);
				}
			}
		}
		return result;
	}
	
	/** @return The content of the element as String.
	 * 	If the element contains only one String child, that child.
	 * 	Other possibilities not yet developed.
	 * */
	public String getTextContent(){
		StringBuilder buffer=new StringBuilder();
		Iterator<Object> it=this.children.iterator();
		while(it.hasNext()){
			Object child=it.next();
			//Here could discriminate type of the child.
			buffer.append(child);
		}
		String s=buffer.toString();
		return s;
	}

	/** @return The content of the element as long.
	 * 	If the element contains only one String child, that child.
	 * 	Other possibilities not yet developed.
	 * @throws NumberFormatException
	 * */
	public long getLongContent() throws NumberFormatException {
		String s = this.getTextContent();
		Long big = Long.valueOf(s);//NumberFormatException
		return big.longValue();
	}
	
	/** @return The content of the element as int.
	 * 	If the element contains only one String child, that child.
	 * 	Other possibilities not yet developed.
	 * @throws NumberFormatException
	 * */
	public int getIntContent()throws NumberFormatException{
		String s=this.getTextContent();
		Integer big=Integer.valueOf(s);//NumberFormatException
		return big.intValue();
	}
	
	/** @return The content of the element as boolean.
	 * "true" is true and everything else is false.
	 * 	If the element contains only one String child, that child.
	 * 	Other possibilities not yet developed.
	 * */
	public boolean getBooleanContent(){
		String s=this.getTextContent();
		boolean b=Boolean.valueOf(s);
		return b;
	}
	
	/** Gets an attribute.value
	 * @param key Assumed to be a valid XML attribute key.
	 * @return value or null. Can be any String, without escaping.
	 * */
	public String getAttribute(String key){
		return (String)this.attributes.get(key);
	}
}