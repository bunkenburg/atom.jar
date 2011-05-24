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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/** Helpers for dealing with XML DOMs from org.w3c.dom.  */
public class DOM {
	/* (Could make this a wrapper for org.w3c.dom.Document.
	 * Then it wouldn't be necessary to pass Document to some
	 * methods.)
	 * */

	/** Creates a new instance of Document, wrapping boring exceptions
	 * in RuntimeException.
	 * @return new Document
	 * @exception RuntimeException wrapping ParserConfigurationException and similar.
	 * 	In a normal Java installation, they should never occur.
	 * */
	public static Document newDocument() throws RuntimeException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();//ParserConfigurationException
			Document document = builder.newDocument();
			return document;
		} catch (ParserConfigurationException pce){
			throw new RuntimeException(pce);
		}
	}

	/** Writes a document to a writer, as XML text with no superfluous
	 * whitespace.
	 * @param os
	 * @param document
	 * @exception RuntimeException wrapping TransformerException. That
	 * 	exception should never happen. It can happen when the transformation
	 * 	is based on an XSL stylesheet. But here, the transformation is the
	 * 	identity transformation streaming a document to a writer. There
	 * 	should never be a problem with that.
	 * */
	public static void write(OutputStream os, Document document){
		try {
			Transformer transformer = newTransformer();
		    DOMSource source = new DOMSource(document);
		    StreamResult result = new StreamResult(os);
		    transformer.transform(source, result);//TransformerException
		} catch (TransformerException te){
			throw new RuntimeException(te);
		}
	}

	/** Creates a new XSL transformer, hiding the boring exceptions.
	 * @return transformer
	 * @exception RuntimeException wrapping TransformerConfigurationException.
	 * 	In a normal Java installation that should never happen.
	 * */
	public static Transformer newTransformer(){
		try{
			String s="com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";
			ClassLoader loader=Object.class.getClassLoader();
			TransformerFactory factory=TransformerFactory.newInstance(s, loader);
			Transformer transformer=factory.newTransformer();//TransformerConfigurationException
			return transformer;
		}catch(TransformerConfigurationException tce){
			throw new RuntimeException(tce);
		}
	}

	/** Formats a DOM to a String
	 * @param document
	 * @return String
	 * @exception TransformerConfigurationException
	 * @exception TransformerException
	 * */
	public static String toString(Document document) throws TransformerConfigurationException, TransformerException {
		//Format the document into a String
		TransformerFactory tFactory = TransformerFactory.newInstance();
	    Transformer transformer = tFactory.newTransformer();//TransformerConfigurationException
	    DOMSource source = new DOMSource(document);
	    StringWriter sw = new StringWriter();
	    StreamResult result = new StreamResult(sw);
	    transformer.transform(source, result);//TransformerException
	    return sw.toString();
	}

	/** Create a script-element that contains javascript.
	 * Creates an Element with tag name "script" that contains
	 * an XML-commment and in the comment, between line breaks,
	 * the javascript code.
	 * That way, the javascript code will not be destroyed by
	 * XML-escaping of "&lt;". The line breaks ensure that the
	 * browser sees the javascript code.
	 * Example:
	 * createScript("if (0<1) alert('Hola')") creates
	 * <pre>&lt;script>
	 * 	&lt;!--
	 * 	if (0<1) alert('Hola')
	 * 	//-->
	 * &lt;/script></pre>
	 * @param document The Document in which to create the element
	 * @param javascript The javascript source code
	 * @return en element ready to go into html
	 * */
	public static Element createScript(Document document, String javascript){
		Element script = document.createElement("script");
		String s = "\n" + javascript + "\n//";
		Comment comment = document.createComment(s);
		script.appendChild(comment);
		return script;
	}

	/** Appends a new text-node to element. The text-node will
	 * contain the String text, suitably escaped for XML.
	 * @param document The document containing element
	 * @param element The element that should receive a new text node
	 * @param text Unescaped text
	 * */
	public static void addText(Document document, Element element, String text){
		String escapedText = XMLString.escape(text);
		Text textNode = document.createTextNode(escapedText);
		element.appendChild(textNode);
	}

	/** Appends a new element containing text to an element.
	 * @param document The document containing element
	 * @param element The element that should receive a new child element
	 * @param tag The tag name of the new child element
	 * @param text Unescaped text
	 * */
	public static void appendTextElement(Document document, Element element, String tag, String text){
		Element child = document.createElement(tag);
		String escapedText = XMLString.escape(text);
		Text textNode = document.createTextNode(escapedText);
		child.appendChild(textNode);
		element.appendChild(child);
	}

	/** Appends a new element containing a long number to an element.
	 * @param document The document containing element
	 * @param element The element that should receive a new child element
	 * @param tag The tag name of the new child element
	 * @param number
	 * */
	public static void appendLongElement(Document document, Element element, String tag, long number){
		Element child = document.createElement(tag);
		String escapedText = Long.toString(number);
		Text textNode = document.createTextNode(escapedText);
		child.appendChild(textNode);
		element.appendChild(child);
	}

	/** Appends a new element containing a date to an element.
	 * The date will be formatted according to RTC 3339, which is
	 * fairly standard for XML applications.
	 * @param document The document containing element
	 * @param element The element that should receive a new child element
	 * @param tag The tag name of the new child element
	 * @param date
	 * */
	public static void appendDateElement(Document document, Element element, String tag, Date date){
		Element child = document.createElement(tag);
		String formattedDate = XMLDate.format(date);
		Text textNode = document.createTextNode(formattedDate);
		child.appendChild(textNode);
		element.appendChild(child);
	}

	/** Gets the one child element with identified tag, or null if there isn't any
	 * with that tag. Undefined if there are many.
	 * @param element
	 * @param tagName
	 * @return Element
	 * */
	public static Element getElementByTagName(Element element, String tagName){
		Element child = null;
		NodeList tagElements = element.getElementsByTagName(tagName);
		if (1 <= tagElements.getLength()){
			child = (Element)tagElements.item(1);
		}
		return child;
	}

	/** Gets an attribute value from an element. Returns null if the element
	 * does not have the attribute,not "" like Element.getAttribute, which
	 * is not very useful.
	 * @param element
	 * @param name
	 * @return attribute
	 * */
	public static String getAttribute(Element element, String name){
		//Could generalize to getAttribute(Node, String)
		String value = element.getAttribute(name);
		if (0<value.length()){
			return value;
		}
		return null;
	}

	/**
	 * Returns a List containing the attributes of the given element.
	 * @param element The element
	 * @return A List containing the attributes of the given element.
	 */
	public static List<Attr> getAttributes(Element element){
		List<Attr> elementAttributes = new ArrayList<Attr>();
		NamedNodeMap attributes = element.getAttributes();
		for(int i = 0; i < attributes.getLength(); i++){
			Node attributeNode = attributes.item(i);
			if(attributeNode instanceof Attr){
				elementAttributes.add((Attr)attributeNode);
			}
		}
		return elementAttributes;
	}

	/** Converts input stream into Document.
	 * <p>
	 * The implementation is based on JAXP (javax.xml.transform.Transformer).
	 * I don't know how to add XSD validation with respect to the XSD file referenced in
	 * the XML.
	 * <p>
	 *
	 * @param in
	 * @return the root element of a newly created document read from input stream
	 * @throws TransformerException
	 */
	public static Element getDocumentRoot(InputStream in) throws TransformerException {
		//DOM-based implementation for the xml parser
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		    Document doc = dBuilder.parse(new InputSource(in));
		    return doc.getDocumentElement();
		} catch (Exception e) {
			throw new RuntimeException("Problem parsing the xml document",e);
		}
	}

	/** This method imitates org.w3c.dom.Node.getTextContent(), which is new in
	 * Java 5. The description below is copied from there.
	 * <p>
	 * This attribute returns the text content of this node and its descendants.
	 * When it is defined to be null, setting it has no effect. On setting, any
	 * possible children this node may have are removed and, if it the new string
	 * is not empty or null, replaced by a single Text node containing the string
	 * this attribute is set to.
	 * <p>
	 * On getting, no serialisation is performed, the returned string does not contain
	 * any markup. No whitespace normalisation is performed and the returned string
	 * does not contain the white spaces in element content (see the attribute
	 * Text.isElementContentWhitespace). Similarly, on setting, no parsing is performed
	 * either, the input string is taken as pure textual content.
	 * <p>
	 * The string returned is made of the text content of this node depending on its
	 * type, as defined below:
	 * <table border="1">
	 * 	<tr><th>Node type</th><th>Content</th></tr>
	 *	<tr>
	 *		<td>
	 *			ELEMENT_NODE, ATTRIBUTE_NODE, ENTITY_NODE, ENTITY_REFERENCE_NODE,
	 *			DOCUMENT_FRAGMENT_NODE
	 *		</td>
	 *		<td>
	 *			concatenation of the <code>textContent</code> attribute value of
	 *			every child node, excluding COMMENT_NODE and PROCESSING_INSTRUCTION_NODE
	 *			nodes. This is the empty string if the node has no children.
	 *		</td>
	 *	</tr>
	 *	<tr>
	 *		<td>
	 *			TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE, PROCESSING_INSTRUCTION_NODE
	 *		</td>
	 *		<td><code>nodeValue</code></td>
	 *	</tr>
	 *	<tr>
	 *		<td>DOCUMENT_NODE, DOCUMENT_TYPE_NODE, NOTATION_NODE</td>
	 *		<td><em>null</em></td>
	 *	</tr>
	 *	</table>
	 * @param node
	 * @return text content
	 * @throws DOMException - DOMSTRING_SIZE_ERR: Raised when it would return more
	 * 	characters than fit in a DOMString variable on the implementation platform.
	 * @since DOM Level 3
	 * @deprecated When we have Java 5, eliminate this method and use
	 * 	Node.getTextContent() directly instead.
	 * */
	@Deprecated
	public static String getTextContent(Node node) throws DOMException {
		short type = node.getNodeType();
		switch (type){

		case Node.ELEMENT_NODE:
		case Node.ATTRIBUTE_NODE:
		case Node.ENTITY_NODE:
		case Node.ENTITY_REFERENCE_NODE:
		case Node.DOCUMENT_FRAGMENT_NODE:
			/* concatenation of the textContent attribute value of every child node,
			 * excluding COMMENT_NODE and PROCESSING_INSTRUCTION_NODE nodes. This is
			 * the empty string if the node has no children. */
			StringBuffer buffer = new StringBuffer();//Java 5: StringBuilder
			NodeList childNodes = node.getChildNodes();
			for (int i=0; i<childNodes.getLength(); i++){
				Node child = childNodes.item(i);
				short childType = child.getNodeType();
				if (childType!=Node.COMMENT_NODE && childType!=Node.PROCESSING_INSTRUCTION_NODE){
					String s = DOM.getTextContent(child);
					buffer.append(s);
				}
			}
			String textContent = buffer.toString();
			return textContent;

		case Node.TEXT_NODE:
		case Node.CDATA_SECTION_NODE:
		case Node.COMMENT_NODE:
		case Node.PROCESSING_INSTRUCTION_NODE:
			return node.getNodeValue();

		case Node.DOCUMENT_NODE:
		case Node.DOCUMENT_TYPE_NODE:
		case Node.NOTATION_NODE:
			return null;

		default:
			throw new RuntimeException("unexpected node type " + type);
		}//switch
	}

}