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
import inspiracio.xml.DOM;
import inspiracio.xml.Namespace;
import inspiracio.xml.XMLDate;
import inspiracio.xml.sax.EasyContentHandler;
import inspiracio.xml.sax.EasySAXParseable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** An Atom feed. 
 * Methods are all getters and setters, and some transformers.
 * <p>
 * There are two usage models for entries: one similar to DOM,
 * the other similar to SAX.
 * <ol>
 * 	<li>
 * 	In the first, you create the feed,
 * 	and add the entries explicitly one by one by calling addEntry.
 * 	You will be using memory for all the bean objects.
 * 	<li>
 * 	In the SAX-like model, you create a feed and set its entry iterator
 * 	once by calling setEntryIterator. The iterator you pass can read
 * 	directly from DB row by row and convert to entries. It is your 
 * 	responsibility to think about when the DB connection is closed.
 * 	A call to hasNext() that returns false can close the Connection.
 * </ol>
 * Either way, when you transform the feed to XML by calling 
 * print(PrintWriter), that method first writes the feed meta data,
 * then the entries added explicitly, and then all the entries from
 * the iterator.
 * </p>
 * */
public class Feed extends RootElement implements EasySAXParseable{
	
	//The fields visible in the feed --------------------
	private List<Person> authors=new ArrayList<Person>();
	//private List<Category> categories = new ArrayList<Categories>();//future
	//private List<Person> contributors = new ArrayList<Contributors>();//future
	private Generator generator;//Need this to identify version
	//private String icon;//future URL of a square image
	private String id="";//Must be an IRI. We violate that.
	private List<Link> links=new ArrayList<Link>();
	//private String logo;//future. URL to a 2x1 image.
	//private Text rights;//future
	//private Text subtitle;//future
	private Text title=new PlainText("");
	private Date updated=new Date();
	private List<inspiracio.xml.Element> extensionElements=new ArrayList<inspiracio.xml.Element>();
	
	/** the entries that have been added one by one */
	private List<Entry> entries=new ArrayList<Entry>();
	
	/** the entries that come from a set iterator */
	private Iterator<Entry> entryIterator=new ArrayList<Entry>().iterator();
	
	//Constructors ------------------------------------------------------------
	
	/** Construct a Feed */
	public Feed(){
		//According to http://www.w3.org/TR/REC-xml-names/#ns-decl declaring namespace
		//"xml" is optional, but stupid IE7 treats it as error.
		//this.addNamespace("xmlns:xml", "http://www.w3.org/XML/1998/namespace");//XML namespace for attributes "lang" and "base"
		try{
			HttpURL atom=new HttpURL("/xsd/atom.xsd");//"http://ws.infojobs.net/xsd/atom.xsd"
			this.addNamespace("", "http://www.w3.org/2005/Atom", atom.toString());
			this.addNamespace("gd", "http://schemas.google.com/g/2005");
		}catch(NullPointerException e){
			//We are not in the server and therefore cannot make HttpURLs.
			this.addNamespace("", "http://www.w3.org/2005/Atom");
		}
	}

	/** Parse a feed from an input stream.
	 * @param in InputStream that contains XML for the feed.
	 * @return feed
	 */
	public static Feed parse(InputStream in){
		try {
			//Parse DOM from stream. Maybe parsing SAX is better?
			org.w3c.dom.Element feedElement=DOM.getDocumentRoot(in);

			//Parse an entry from a DOM element.
			Feed feed=Feed.parse(feedElement);
			return feed;
		} catch (Exception e){
			//e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/** Parse an entry from an entry element. 
	 * @param entryElement
	 * @return entry
	 * */
	public static Feed parse(org.w3c.dom.Element entryElement){
		//This method seems very clumsy. Can it be done better?
		Feed feed = new Feed();
		
		// Parse feed attributes
		for(Attr attribute : DOM.getAttributes(entryElement)){
			feed.addAttribute(attribute.getName(), attribute.getValue());
			if(attribute.getPrefix()!=null && attribute.getNamespaceURI()!=null)
				feed.addNamespace(attribute.getPrefix(), attribute.getNamespaceURI());
		}
		
		//Loop over all the children.
		//Each child element, treat it with the right type
		NodeList children = entryElement.getChildNodes();
		for (int i=0; i<children.getLength(); i++){
			Node child = children.item(i);
			//ignore all node types except elements: attributes, text.
			if (child.getNodeType()==Node.ELEMENT_NODE){
				org.w3c.dom.Element childElement = (org.w3c.dom.Element)child;
				//discriminate on tag name. Can this be done better?
				String tagName = childElement.getTagName();
				if ("author".equals(tagName)){
					//* authors
					Person author = Person.parse(childElement);
					feed.addAuthor(author);					
				} else if ("id".equals(tagName)){
					//id
					//String id = childElement.getTextContent();
					String id = childElement.getTextContent();
					feed.setId(id);
				} else if ("link".equals(tagName)){
					//* links
					Link link = Link.parse(childElement);
					feed.addLink(link);
				} else if ("title".equals(tagName)){
					//required title
					String title = childElement.getTextContent();
					feed.setTitle(title);
				} else if ("updated".equals(tagName)){
					//optional updated
					//String textContent = childElement.getTextContent();
					String textContent = childElement.getTextContent();
					Date d = XMLDate.parse(textContent);
					feed.setUpdated(d);
				} else if ("entry".equals(tagName)){
					//* entry elements
					Entry entry=Entry.parse(childElement);
					feed.getEntries().add(entry);
				}
			}
		}
		return feed;
	}
	
	/** Sets the natural language of this feed. 
	 * Subelements, for example, single entries, may override.
	 * @param lang Language value according to http://www.ietf.org/rfc/rfc3066.txt.
	 * */
	public void setLang(String lang){this.addAttribute("xml:lang", lang);}
	
	/** Gets the natural language of this feed. 
	 * @return language identifier or null if none is set
	 * */
	String getLang(){return this.getAttribute("xml:lang");}
	
	/** Returns the entries in the feed in order.
	 * @return entries */
	public List<Entry> getEntries(){return entries;}
	
	/** Adds an extension element.
	 * @param element */
	public void addExtensionElement(inspiracio.xml.Element element){
		this.extensionElements.add(element);
	}
	
	/** Adds entries to the feed.
	 * Their namespaces will be added to the feed.
	 * @param entries */
	public void addEntry(Entry... entries){
		for(Entry entry : entries){
			this.entries.add(entry);
			List<Namespace>ns=entry.getNamespaces();
			for(Namespace n : ns){
				this.addNamespace(n);//Optimise: ignore atom, gd, xsi, as they are already in the feed.
				//Optimise: remove the namespace from the entry.
			}
		}
	}
	
	/** Sets the entryIterator, overwriting any previous set value.
	 * @param entryIterator
	 * */
	public void setEntryIterator(Iterator<Entry> entryIterator){this.entryIterator=entryIterator;}
	
	public String getId(){return id;}
	public void setId(String id){this.id=id;}
	
	/** Sets the ID of the feed, first calling toString on the argument. */
	public void setId(Object id){this.id=id.toString();}
	
	public List<Link> getLinks(){return links;}
	public void addLink(Link link){links.add(link);}
	public Text getTitle(){return title;}

	/** Sets the title of the feed 
	 * @param title Just plain text, no XML, no HTML, no escaping.
	 * */
	public void setTitle(String title) {
		this.title = new PlainText(title);
	}

	public Date getUpdated(){return updated;}
	public void setUpdated(Date updated){this.updated=updated;}

	/** Sets the updated timestamp
	 * @param updated simply as String 
	 * */
	public void setUpdated(String updated) {
		Date d=XMLDate.parse(updated);
		this.updated=d;
	}

	public void addAuthor(Person author){this.authors.add(author);}
	
	/** Adds an author that only has a name.
	 * Convenience for addAuthor(new Person(name)). */
	public void addAuthor(String name){this.addAuthor(new Person(name));}
	
	Generator getGenerator(){return generator;}
	public void setGenerator(Generator generator){this.generator=generator;}
	
	/** Writes all the feed to the given content handler.
	 * First writes the entries that have been added one by one,
	 * then the entries that come from a set entry iterator.
	 * @param handler Write the XML to this content handler.
	 * @exception SAXException
	 *  */
	public void parse(EasyContentHandler handler) throws SAXException {
		handler.startElement("feed", this.getAttributes());
	
		if (this.generator!=null){
			this.generator.parse(handler);
		}
		
		//obligatory
		this.title.parse(handler, "title");
		
		//obligatory
		handler.element("updated", this.updated);
		
		//obligatory
		handler.element("id", this.id);
		
		//authors
		Iterator<Person> it = this.authors.iterator();
		while (it.hasNext()){
			Person author = it.next();
			author.parse(handler, "author");
		}
			
		for(Link link : this.links){
			link.parse(handler);
		}

		//extension elements
		Iterator<inspiracio.xml.Element> extensions = this.extensionElements.iterator();
		while (extensions.hasNext()){
			inspiracio.xml.Element element = extensions.next();
			element.parse(handler);
		}
		
		//loop over the explicitly added entries
		Iterator<Entry> entries = this.entries.iterator();
		while (entries.hasNext()){
			Entry entry = entries.next();
			entry.parse(handler);
		}
		//suck entries from a set entry iterator, if there is one
		if (this.entryIterator!=null){
			while (this.entryIterator.hasNext()){
				Entry entry = this.entryIterator.next();
				entry.parse(handler);
			}
		}

		handler.endElement("feed");
	}	

}