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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Feed validation utility.
 * <p>It intends to help debugging your feeds. Call {@link #validate(Feed, String)} for your 
 * resulting feeds in order to check they comply with the XSDs you give.
 * <p>Usage example (in your {@link AtomServlet} implementation):<pre>
 *Feed result = sao.get(url, user);
 *if(DEBUG) {
 *	try {
 *		FeedValidator.validate(result,null);
 *	} catch (Exception e) { // just prints, for programmer's information
 *		e.printStackTrace();
 *	}
 *}
 *return result;
 *</pre>
 * Note the example above just prints errors to the standard output, then 
 * returns invalid content anyway. It is advisable to do it like this, because
 * you are able to check the program's output against the validator errors.
 * <p>It is advisable to do this at debugging time only, as it consumes
 * big deal of CPU time.</p>
 * 
 * @author jgimenez
 */
public class FeedValidator {

	/**
	 * Validates a feed
	 * @param feed Feed to validate
	 * @param schemaSource Schema location for noNamespaceSchemaLocation. Can be null if
	 * the feed contains all necessary information (recommended).
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void validate(Feed feed, String schemaSource) throws TransformerException, ParserConfigurationException,
			SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
	
		SAXParser parser = factory.newSAXParser();
		parser.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");
		if(schemaSource!=null)
			parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
					schemaSource);
		XMLReader reader = parser.getXMLReader();
		reader.setErrorHandler(new MyErrorHandler());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		feed.write(bos);
		bos.close();
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		reader.parse(new InputSource(bis));
		bis.close();
	}
	
	/**
	 * Error handler that prints up to MAX_ERRORS errors to the standard output stream.
	 * @author jgimenez
	 *
	 */
	private static class MyErrorHandler extends DefaultHandler {
		/** Number of errors up to now */
		private int errorCount = 0;
		/** Maximum number of errors to print */
		private final int MAX_ERRORS = 10;

		/**
		 * Prints a warning message
		 */
		public void warning(SAXParseException e) {
			if(errorCount++<MAX_ERRORS) {
				System.out.println("Warning: ");
				printInfo(e);
				if(errorCount>=MAX_ERRORS)
					System.out.println("MAX_ERRORS reached: No more errors will be printed.");
			}
		}

		/**
		 * Prints an error message
		 */
		public void error(SAXParseException e) {
			if(errorCount++<MAX_ERRORS) {
				System.out.println("Error: ");
				printInfo(e);
				if(errorCount>=MAX_ERRORS)
					System.out.println("MAX_ERRORS reached: No more errors will be printed.");
			}
		}

		/**
		 * Prints a fatal error message
		 */
		public void fatalError(SAXParseException e) {
			if(errorCount++<MAX_ERRORS) {
				System.out.println("Fatal error: ");
				printInfo(e);
				if(errorCount>=MAX_ERRORS)
					System.out.println("MAX_ERRORS reached: No more errors will be printed.");
			}
		}

		/**
		 * Helper utility that prints to the stdout
		 * @param e
		 */
		private void printInfo(SAXParseException e) {
			System.out.println("   Public ID: " + e.getPublicId());
			System.out.println("   System ID: " + e.getSystemId());
			System.out.println("   Line number: " + e.getLineNumber());
			System.out.println("   Column number: " + e.getColumnNumber());
			System.out.println("   Message: " + e.getMessage());
		}
	}
	
}