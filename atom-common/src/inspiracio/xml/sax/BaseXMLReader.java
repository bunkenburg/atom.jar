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

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/** Base implementation of XMLReader. 
 * <p>
 * Standard implementations for these methods:
 * <ul> 
 *	<li>set/getContentHandler
 *	<li>set/getDTDHandler
 * 	<li>set/getFeature --maintains state of the two obligatory features
 * 	<li>set/getEntityResolver
 * 	<li>set/getErrorHandler
 * 	<li>parse(systemId) --delegates to parse(InputSource)
 * </ul>
 * The subclass may override set/getFeature and set/getProperty in
 * order to improve them.
 * </p>
 * */
public class BaseXMLReader implements XMLReader{
	
	private static final String FEATURE_NAMESPACES="http://xml.org/sax/features/namespaces";
	private static final String FEATURE_NAMESPACE_PREFIXES="http://xml.org/sax/features/namespace-prefixes";
	
	private boolean isFeatureNamespace=false;
	private boolean isFeatureNamespacePrefixes=false;

	private ContentHandler contentHandler=null;
	private DTDHandler dtdHandler=null;
	private EntityResolver entityResolver=null;
	private ErrorHandler errorHandler=null;
	
	private EasySAXParseable parseable=null;
	
	/** indentation? */
	private boolean prettyprint=false;
	
	//Constructor ---------------------------------------------
	
	/** Construct, giving the object that we want to transform into XML. 
	 * @param parseable
	 * */
	public BaseXMLReader(EasySAXParseable parseable){this.parseable=parseable;}
	
	/** Gets the content handler. 
	 * @return ContentHandler
	 * */
	public ContentHandler getContentHandler(){return contentHandler;}

	/** Gets the DTD handler. 
	 * @return DTDHandler
	 * */
	public DTDHandler getDTDHandler(){return dtdHandler;}

	/** Gets the entity resolver.
	 * @return EntityResolver
	 *  */
	public EntityResolver getEntityResolver(){return entityResolver;}

	/** Gets the error handler.
	 * @return ErrorHandler
	 * @exception ErrorHandler
	 * */
	public ErrorHandler getErrorHandler(){return errorHandler;}

	/** Returns whether the two obligatory features are set.
	 * @param name the feature
	 * @return Is the feature set?
	 * @exception SAXNotRecognizedException for all features except the 
	 * 	two obligatory ones
	 * @exception SAXNotSupportedException never
	 * */
	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		if (name.equals(FEATURE_NAMESPACES)){
			return this.isFeatureNamespace;
		} else if (name.equals(FEATURE_NAMESPACE_PREFIXES)){
			return this.isFeatureNamespacePrefixes;
		} else {
			throw new SAXNotRecognizedException("Feature " + name + " not recognized.");
		}
	}

	/** Recognises no properties. 
	 * @param name
	 * @return Object
	 * @exception SAXNotRecognizedException always 
	 * @exception SAXNotSupportedException
	 * */
	public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotRecognizedException("Property " + name + " not implemented");
	}

	/** Parses input from a system id. 
	 * This method delegates to parse(InputSource). 
	 * @param systemId 
	 * @exception IOException
	 * @exception SAXException
	 * */
	public void parse(String systemId) throws IOException, SAXException {
		InputSource inputSource=new InputSource(systemId);
		this.parse(inputSource);
	}

	/** Calls methods of the content handler that represent the
	 * feed. Ignore the argument input source.
	 * @param source
	 * @throws SAXException
	 * */
	public void parse(InputSource source) throws SAXException {
		ContentHandler handler = this.getContentHandler();
		if(this.prettyprint){
			handler = new IndentingContentHandler(handler);
		}
		EasyContentHandler easyHandler = new EasyContentHandler(handler);
		//handler.setDocumentLocator(locator);//Gives line and column information for parse errors. Irrelevant here.
		handler.startDocument();
		this.parseable.parse(easyHandler);
        handler.endDocument(); 
	}

	/** Sets the content handler. 
	 * @param handler
	 * */
	public void setContentHandler(ContentHandler handler){this.contentHandler=handler;}

	/** Sets the DTD handler. 
	 * @param dtdHandler
	 * */
	public void setDTDHandler(DTDHandler dtdHandler){this.dtdHandler=dtdHandler;}

	/** Sets the entity resolver. 
	 * @param entityResolver
	 * */
	public void setEntityResolver(EntityResolver entityResolver){this.entityResolver=entityResolver;}

	/** Sets the error handler. 
	 * @param errorHandler
	 * */
	public void setErrorHandler(ErrorHandler errorHandler){this.errorHandler=errorHandler;}

	/** Accepts only the two obligatory features and maintains their
	 * state, without really implementing them of course. 
	 * Subclasses can improve.
	 * @param name feature name
	 * @param value boolean
	 * @exception SAXNotRecognizedException for all features except the 
	 * 	two obligatory ones
	 * @exception SAXNotSupportedException never
	 * */
	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
		if (name.equals(FEATURE_NAMESPACES)){
			this.isFeatureNamespace = value;
		} else if (name.equals(FEATURE_NAMESPACE_PREFIXES)){
			this.isFeatureNamespacePrefixes = value;
		} else {
			throw new SAXNotRecognizedException("Feature " + name + " not recognized.");
		}
	}

	/** Recognises no properties. 
	 * @param name
	 * @param value
	 * @exception SAXNotRecognizedException always
	 * @exception SAXNotSupportedException
	 * */
	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotRecognizedException("Property " + name + " not implemented");
	}

	public void setPrettyprint(boolean p){this.prettyprint=p;}
}