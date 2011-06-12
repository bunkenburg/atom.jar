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

import inspiracio.lang.NotImplementedException;
import inspiracio.servlet.http.ETag;
import inspiracio.servlet.http.HttpURL;
import inspiracio.xml.DOM;
import inspiracio.xml.Element;
import inspiracio.xml.XMLDate;
import inspiracio.xml.sax.EasyContentHandler;
import inspiracio.xml.sax.EasySAXParseable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Entry of an Atom 1.0 feed.
 * The specification is at http://atompub.org/rfc4287.html.
 * */
public class Entry extends RootElement implements EasySAXParseable{

	//Some fields are not yet implemented. Here I list all. Uncomment and implement as needed.

	//Attributes
	private String base;
	private String lang;

	private List<Person> authors = new ArrayList<Person>();
	//private List<Category> categories = new ArrayList<Category>();

	/** Non-textual content and src not implemented */
	private Text content;

	//private List<Person> contributors = new ArrayList<Person>();
	private String id;
	private List<Link> links = new ArrayList<Link>();//future
	private Date published;
	private Text rights;
	//private Source source;
	private Text summary;

	/** Default type="text", other types not implemented */
	private Text title;

	private Date updated;
	private List<Element> extensions = new ArrayList<Element>();

	/** The URI of the entry. To view, edit, or delete this entry, the client
	 * must send a request to this URI. The format of the URI is not fixed by
	 * the Atom spec. I recommend http://www.domain.com/atom/-/collection/entryID
	 * where "collection" is the name of the collection of the entry, and "ID" is the
	 * id of the entry. That format is compatible with GData.
	 * */
	private String uri;

	/** The String inside the strong ETag that represents the version of the entry.
	 * Unescaped and unquoted. */
	private String etag;

	//Constructors -----------------------------------------

	public Entry(){
		this.updated=new Date();//default, very likely client will set it
	}

	/** Parse an entry from an input stream.
	 * @param in InputStream that contains XML for one entry.
	 * @return entry
	 * */
	public static Entry parse(InputStream in) {
		try {
			//Parse DOM from stream. Maybe parsing SAX is better?
			org.w3c.dom.Element entryElement=DOM.getDocumentRoot(in);

			//Parse an entry from a DOM element.
			Entry entry=Entry.parse(entryElement);
			return entry;
		} catch (Exception e){
			//e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/** Parse an entry from an entry element.
	 * @param entryElement
	 * @return entry
	 * */
	public static Entry parse(org.w3c.dom.Element entryElement){
		//This method seems very clumsy. Can it be done better?
		Entry entry=new Entry();

		//Attributes. Can they have prefix "xml:"?
		//entry.base = DOM.getAttribute(entryElement, "base");
		//entry.lang = DOM.getAttribute(entryElement, "lang");

		// Parse entry attributes
		for(Attr attribute : DOM.getAttributes(entryElement)){
			String name=attribute.getName();
			String value=attribute.getValue();
			if("gd:etag".equals(name)){
				String etag=ETag.parseStrong(value);
				entry.setETag(etag);
			}else{
				entry.addAttribute(name, value);
				if(attribute.getPrefix()!=null && attribute.getNamespaceURI()!=null)
					entry.addNamespace(attribute.getPrefix(), attribute.getNamespaceURI());
			}
		}

		//Loop over all the children.
		//Each child element, treat it with the right type
		NodeList children = entryElement.getChildNodes();
		for (int i=0; i<children.getLength(); i++){
			Node child=children.item(i);
			//ignore all node types except elements: attributes, text.
			if (child.getNodeType()==Node.ELEMENT_NODE){
				org.w3c.dom.Element childElement=(org.w3c.dom.Element)child;

				// namespaces
				if(childElement.getPrefix()!=null && childElement.getNamespaceURI()!=null)
					entry.addNamespace(childElement.getPrefix(), childElement.getNamespaceURI());
				for(Attr attribute : DOM.getAttributes(childElement)){
					entry.addAttribute(attribute.getName(), attribute.getValue());
					if(attribute.getPrefix()!=null && attribute.getNamespaceURI()!=null)
						entry.addNamespace(attribute.getPrefix(), attribute.getNamespaceURI());
				}

				//discriminate on tag name. Can this be done better?
				String tagName = childElement.getTagName();
				if ("author".equals(tagName)){
					//* authors
					Person author = Person.parse(childElement);
					entry.addAuthor(author);
				} else if ("category".equals(tagName)){
					//categories
					throw new NotImplementedException("not implemented");
				} else if ("content".equals(tagName)){
					//optional content
					Text content = Text.parse(childElement);
					entry.setContent(content);
				} else if ("contributor".equals(tagName)){
					//* contributors
					throw new NotImplementedException("not implemented");
				} else if ("id".equals(tagName)){
					//id
					String id = childElement.getTextContent();
					entry.setId(id);
				} else if ("link".equals(tagName)){
					//* links
					Link link=Link.parse(childElement);
					entry.addLink(link);
				} else if ("published".equals(tagName)){
					//optional published
					String textContent = childElement.getTextContent();
					Date d = XMLDate.parse(textContent);
					entry.setPublished(d);
				} else if ("rights".equals(tagName)){
					//optional rights
					Text rights = Text.parse(childElement);
					entry.setRights(rights);
				} else if ("source".equals(tagName)){
					//optional source
					throw new NotImplementedException("not implemented");
				} else if ("summary".equals(tagName)){
					//optional summary
					Text summary = Text.parse(childElement);
					entry.setSummary(summary);
				} else if ("title".equals(tagName)){
					//required title
					Text title = Text.parse(childElement);
					entry.setTitle(title);
				} else if ("updated".equals(tagName)){
					//optional updated
					String textContent = childElement.getTextContent();
					Date d = XMLDate.parse(textContent);
					entry.setUpdated(d);
				} else {
					//* extension elements
					Element extensionElement = new Element(childElement);
					entry.addExtElement(extensionElement);
				}
			}
		}

		return entry;
	}

	//Accessors ---------------------------------------------

	/** Adds an author to the authors.
	 * @param author */
	public void addAuthor(Person author){authors.add(author);}

	public Text getContent(){return content;}

	/** @param content Just text. No XML, no HTML, no escaping.*/
	public void setContent(String content) {
		this.content = new PlainText(content);
	}

	/** Sets the content.
	 * @param content */
	public void setContent(Text content) {this.content=content;}

	public String getId(){return id;}

	/** The ID should be String.
	 * But often atom beans have Integer-IDs.
	 * So here the parameter's type is Object so that
	 * you can simply call entry.setId(Object).
	 * @param id the id to set
	 */
	public void setId(Object id){this.id=id.toString();}

	public String getETag(){return this.etag;}
	public void setETag(String etag){this.etag=etag;}

	/** Adds a link.
	 * @param link */
	public void addLink(Link link){this.links.add(link);}

	/** Gets a titled link, or null. */
	public Link getLink(String title){
		for(Link link : this.links){
			if(title.equals(link.getTitle()))
				return link;
		}
		return null;
	}

	/** Gets all links, or null.
	 * The returned list remains property of the entry. */
	public List<Link> getLinks(){return this.links;}

	/** @return the published */
	public Date getPublished() {return this.published;}

	/** @param published the published to set */
	public void setPublished(Date published) {this.published = published;}

	/** @return the rights */
	Text getRights() {return this.rights;}

	/** @param rights Just plain text, no XML, no HTML, no escaping */
	void setRights(String rights) {this.rights = new PlainText(rights);}

	void setRights(Text rights) {this.rights = rights;}
	Text getSummary() {return summary;}

	/** @param summary Just text, no XML, no HTML, no escaping */
	public void setSummary(String summary) {this.summary = new PlainText(summary);}

	public void setSummary(Text summary) {this.summary = summary;}
	public Text getTitle(){return this.title;}

	/** @param title Just text, no XML, no HTML, no escaping */
	public void setTitle(String title) {this.title = new PlainText(title);}

	public void setTitle(Text title) {this.title = title;}
	public Date getUpdated() {return updated;}

	/** Gets the URI that identifies this entry.
	 * @return URI */
	public String getURI(){return uri;}

	public void setURI(String uri){this.uri=uri;}
	public void setURI(HttpURL uri){this.uri=uri.toString();}
	public void setUpdated(Date updated) {this.updated = updated;}

	/** Adds an extension element.
	 * @param element */
	public void addExtElement(Element element){
		this.extensions.add(element);
	}

	/** Gets the extension elements with a specific tag.
	 * @param tag
	 * @return fresh list of the extension elements with that tag,
	 * 	in the same order as they are in the entry
	 * */
	public List<Element> getExtensionElements(String tag){
		List<Element> found = new ArrayList<Element>();
		Iterator<Element> it = this.extensions.iterator();
		while (it.hasNext()){
			Element element = it.next();
			if (tag.equals(element.getTag())){
				found.add(element);
			}
		}
		return found;
	}

	/** Gets the first extension element with a specific tag, or null.
	 * @param tag
	 * @return first extension element with that tag, or null if there is none */
	public Element getExtensionElement(String tag){
		Element result=this.getOptionalExtensionElement(tag);
		return result;
	}

	/** Gets the text content of the first extension element with a specific tag, or null.
	 * @param tag
	 * @return first extension element with that tag, or null if there is none */
	public String getExtensionElementTextContent(String tag){
		Element result=this.getExtensionElement(tag);
		if(result==null)return null;
		return result.getTextContent();
	}

	/** Gets the long content of the first extension element with a specific tag, or null.
	 * @param tag
	 * @return first extension element with that tag, or null if there is none */
	public Long getExtensionElementLongContent(String tag){
		Element result=this.getExtensionElement(tag);
		if(result==null)return null;
		return result.getLongContent();
	}

	/** Gets the boolean content of the first extension element with a specific tag, or null.
	 * @param tag
	 * @return first extension element with that tag, or null if there is none */
	public Boolean getExtensionElementBooleanContent(String tag){
		Element result=this.getExtensionElement(tag);
		if(result==null)return null;
		return result.getBooleanContent();
	}

	/** Gets the first extension element with a specific tag, or null.
	 * @param tag
	 * @return first extension element with that tag, null if not found. */
	public Element getOptionalExtensionElement(String tag) {
		for(Element element: this.extensions){
			if(tag.equals(element.getTag()))
				return element;
			
		}
		return null;
	}

	/** Adds an extension element containing only text.
	 * The extension element is a simple extension element according
	 * Atom 1.0 spec at http://atompub.org/rfc4287.html.
	 * It has no attributes and no subelements.
	 * Therefore it is similar to a property: the tag is like the
	 * key and the body is like the value.
	 * @param tag The tag name of the new child element
	 * @param body Textual content of the extension element, not escaped.
	 * 	Does nothing if the body is null.
	 * */
	public void addSimpleExtElement(String tag, String body){
		if(body==null)return;
		Element child=new Element(tag);
		child.addChild(body);
		this.addExtElement(child);
	}

	/** Adds an extension element containing only text representing an
	 * integer number.
	 * @param tag The tag name of the new child element
	 * @param number Does nothing if number is null.
	 * */
	public void addSimpleExtElement(String tag, Integer number){
		if(number==null)return;
		this.addSimpleExtElement(tag, Integer.toString(number));
	}

	/** Adds an extension element containing only text representing a long number.
	 * @param tag The tag name of the new child element
	 * @param number Does nothing if number is null.
	 * */
	public void addSimpleExtElement(String tag, Long number){
		if(number==null)return;
		this.addSimpleExtElement(tag, Long.toString(number));
	}

	/** Adds an extension element containing only text representing a timestamp.
	 * The timestamp will be represented according to RTC 3339.
	 * @param tag The tag name of the new child element
	 * @param date The method does nothing if the date is null.
	 * */
	public void addSimpleExtElement(String tag, Date date){
		if(date==null)return;
		String s=XMLDate.format(date);
		this.addSimpleExtElement(tag, s);
	}

	/** Adds an extension element containing only text representing a boolean,
	 * which will appear as "true" or "false".
	 * @param tag The tag name of the new child element
	 * @param b Does nothing if b is null. */
	public void addSimpleExtElement(String tag, Boolean b){
		if(b==null)return;
		String s=Boolean.toString(b);
		this.addSimpleExtElement(tag, s);
	}

	/** Converts the Entry into an XML-DOM Element.
	 * @param document The document where the element will go.
	 * @return Element */
	org.w3c.dom.Element toElement(Document document){
		org.w3c.dom.Element entry = document.createElement("entry");

		//atomCommonAttributes
		if (this.base!=null){
			entry.setAttribute("base", this.base);
		}
		if (this.lang!=null){
			entry.setAttribute("lang", this.lang);
		}

		// gd:etag attribute
		String etag=this.getETag();
		etag=ETag.makeStrong(etag);
		entry.setAttribute("gd:etag", etag);

		//& atomAuthor*
		Iterator<Person> it = this.authors.iterator();
		while (it.hasNext()){
			Person person = it.next();
			org.w3c.dom.Element author = person.toElement(document, "author");
			entry.appendChild(author);
		}

		//atomCategory* future

		//& atomContent?
		if (this.content!=null){
			org.w3c.dom.Element elem = this.content.toElement(document, "content");
			entry.appendChild(elem);
		}

		//& atomContributor* future

		//& atomId
		//The ID really must be an IRI.
		DOM.appendTextElement(document, entry, "id", this.id);

		// self link
		if(getURI()!=null) {
			Link selfLink = new Link();
			selfLink.setHref(getURI());
			selfLink.setRel("self");
			selfLink.setType("application/atom+xml");
			entry.appendChild(selfLink.toElement(document, "link"));
		}

		//& atomLink*
		Iterator<Link> links = this.links.iterator();
		while (links.hasNext()){
			Link link = links.next();
			org.w3c.dom.Element elem = link.toElement(document, "link");
			entry.appendChild(elem);
		}

		//& atomPublished?
		if (this.published!=null){
			DOM.appendDateElement(document, entry, "published", this.published);
		}

		//& atomRights? future
		//& atomSource? future
		//& atomSummary? future

		//& atomTitle
		entry.appendChild(this.title.toElement(document, "title"));

		//& atomUpdated
		DOM.appendDateElement(document, entry, "updated", this.updated);

		//& extensionElement*
		Iterator<Element> extensions = this.extensions.iterator();
		while (extensions.hasNext()){
			Element element = extensions.next();
			org.w3c.dom.Element elem = element.toElement(document);
			entry.appendChild(elem);
		}

		return entry;
	}

	/** Writes the Entry into an XML-SAX content handler.
	 * @param handler
	 * @exception SAXException
	 * */
	public void parse(EasyContentHandler handler) throws SAXException {

		//Attributes
		Map<String, String> atts=this.getAttributes();

		//atomCommonAttributes
		if(this.base!=null)
			atts.put("base", this.base);
		if(this.lang!=null)
			atts.put("lang", this.lang);

		//gd:etag
		String etag=this.getETag();
		if(etag!=null){
			//The ETag may be correctly null if the request is POST for creation.
			//The bean does not have an ETag yet.
			//The DAO will insert it into DB and return it with an ETag set.
			etag=ETag.makeStrong(etag);
			atts.put("gd:etag", etag);
		}

		handler.startElement("entry", atts);

		//& atomAuthor*
		Iterator<Person>it=this.authors.iterator();
		while(it.hasNext()){
			Person author=it.next();
			author.parse(handler, "author");
		}

		//atomCategory* future

		//& atomContent?
		if(this.content!=null)
			this.content.parse(handler, "content");

		//& atomContributor* future

		//& atomId
		//The ID really must be an IRI.
		if(this.id!=null)
			handler.element("id", this.id);

		// self link
		if(getURI()!=null) {
			Link selfLink = new Link();
			selfLink.setHref(getURI());
			selfLink.setType("application/atom+xml");
			selfLink.setRel("self");
			selfLink.parse(handler);
		}

		//& atomLink*
		Iterator<Link> links=this.links.iterator();
		while(links.hasNext()){
			Link link=links.next();
			link.parse(handler);
		}

		//& atomPublished?
		if(this.published!=null)
			handler.element("published", this.published);

		//& atomRights? future
		//& atomSource? future
		//& atomSummary? future

		//& atomTitle
		if(this.title!=null)
			this.title.parse(handler, "title");

		//& atomUpdated
		if(this.updated!=null)
			handler.element("updated", this.updated);

		//& extensionElement*
		Iterator<Element> extensions=this.extensions.iterator();
		while(extensions.hasNext()){
			Element element=extensions.next();
			element.parse(handler);
		}

		handler.endElement("entry");
	}

}