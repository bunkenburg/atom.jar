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

import inspiracio.servlet.http.HttpURL;
import inspiracio.xml.sax.EasyContentHandler;

import java.util.HashMap;
import java.util.Map;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/** A link according to Atom 1.0 spec at http://atompub.org/rfc4287.html.
 * */
public class Link {

	//State -----------------------------------------------------------

	private String base;
	private String lang;
	private String href;
	private String rel;
	private String type;
	private String hreflang;
	private String title;
	private int length=-1;

	//Constructors ------------------------------------------------------

	public Link(){}

	@Override public Link clone(){
		Link clone=new Link();
		clone.base=this.base;
		clone.href=this.href;
		clone.hreflang=this.hreflang;
		clone.lang=this.lang;
		clone.length=this.length;
		clone.rel=this.rel;
		clone.title=this.title;
		clone.type=this.type;
		return clone;
	}

	//Accessors ---------------------------------------------------------

	public String getHref(){return href;}

	/** @param href the href to set, not escaped */
	public void setHref(String href){this.href=href;}
	public void setHref(HttpURL href){this.href=href.toString();}

	String getHreflang(){return hreflang;}
	public void setHreflang(String hreflang){this.hreflang=hreflang;}
	int getLength(){return length;}
	void setLength(int length){this.length=length;}
	public String getRel(){return rel;}
	public void setRel(String rel){this.rel=rel;}
	String getTitle(){return title;}
	public void setTitle(String title){this.title=title;}
	String getType() {return type;}
	public void setType(String type) {this.type = type;}

	/** only debug */
	@Override public String toString(){
		return "Link href=" + this.href;
	}
	
	//XML conversion ----------------------------------------------------
	
	/** Converts this link into an XML-DOM element with
	 * given tag.
	 * @param document where the element will go
	 * @param tag
	 * @return Element
	 */
	Element toElement(Document document, String tag){
		Element element = document.createElement(tag);
		//atomCommonAttributes,
		if (this.base!=null) {
			element.setAttribute("base", this.base);
		}
		if (this.lang!=null){
			element.setAttribute("lang", this.lang);
		}

		//attribute href { atomUri },
		element.setAttribute("href", this.href);

		//attribute rel { atomNCName | atomUri }?,
		if (this.rel!=null){
			element.setAttribute("rel", this.rel);
		}

		//attribute type { atomMediaType }?,
		if (this.type!=null){
			element.setAttribute("type", this.type);
		}

		//attribute hreflang { atomLanguageTag }?,
		if (this.hreflang!=null){
			element.setAttribute("hreflang", this.hreflang);
		}

		//attribute title { text }?,
		if(this.title!=null){
			element.setAttribute("title", this.title);
		}

		//attribute length { text }?,
		if (0 <= this.length){
			element.setAttribute("length", Integer.toString(this.length));
		}
		return element;
	}

	/** Writes this link into an XML-SAX content handler.
	 * @param handler
	 * @exception SAXException
	 */
	void parse(EasyContentHandler handler) throws SAXException {
		Map<String, String> atts=new HashMap<String, String>();

		//atomCommonAttributes,
		if(this.base!=null) {
			atts.put("base", this.base);
		}
		if(this.lang!=null){
			atts.put("lang", this.lang);
		}

		//attribute href { atomUri },
		atts.put("href", this.href);

		//attribute rel { atomNCName | atomUri }?,
		if(this.rel!=null){
			atts.put("rel", this.rel);
		}

		//attribute type { atomMediaType }?,
		if(this.type!=null){
			atts.put("type", this.type);
		}

		//attribute hreflang { atomLanguageTag }?,
		if(this.hreflang!=null){
			atts.put("hreflang", this.hreflang);
		}

		//attribute title { text }?,
		if(this.title!=null){
			atts.put("title", this.title);
		}

		//attribute length { text }?,
		if(0<=this.length){
			atts.put("length", Integer.toString(this.length));
		}
		handler.element("link", atts);
	}

	/** Parse a link from an element.
	 * @param element
	 * @return Link
	 *  */
	static Link parse(Element element){
		Link link=new Link();
		Attr attr;
		if((attr=element.getAttributeNode("base"))!=null) link.base = attr.getTextContent();
		if((attr=element.getAttributeNode("lang"))!=null) link.lang = attr.getTextContent();
		link.href=element.getAttributeNode("href").getTextContent(); // this one is compulsory
		if((attr=element.getAttributeNode("rel"))!=null) link.rel = attr.getTextContent();
		if((attr=element.getAttributeNode("type"))!=null) link.type = attr.getTextContent();
		if((attr=element.getAttributeNode("hreflang"))!=null) link.hreflang = attr.getTextContent();
		if((attr=element.getAttributeNode("title"))!=null) link.title = attr.getTextContent();
		if((attr=element.getAttributeNode("length"))!=null) link.length = Integer.parseInt(attr.getTextContent());
		return link;
	}
}