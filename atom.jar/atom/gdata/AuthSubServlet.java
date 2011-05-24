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
package atom.gdata;

import inspiracio.servlet.http.HttpURL;
import inspiracio.servlet.http.NotAuthorizedException;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Implement Authentication Service, AuthSub,
 * following the specification of Google AuthSub at
 * http://code.google.com/apis/accounts/docs/AuthForWebApps.html.
 * This class receives and parses http requests, delegates to
 * methods of the subclass, and returns http responses.
 * 
 * @author BARCELONA\alexanderb
 *
 */
public abstract class AuthSubServlet extends HttpServlet {
	
	/** The two chars that mark an end of line in http communications. */
	private static final String CRLF = "\r\n";
	
	/** So that programmatic clients may instantiate and call getTokenInfo(). */
	public AuthSubServlet(){}
	
	//The servlet methods -------------------------------------------
	
	/** Accepts request for AuthSubRequest.
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Oblige https
		if (!request.isSecure()){
			response.sendError(401, "Must use https.");
			return;
		}
		
		//Parse request
		String pathInfo = request.getPathInfo();
		if ("/AuthSubRequest".equals(pathInfo)){
			//Parse parameters
			String next = request.getParameter("next");//required
			String hd = request.getParameter("hd");//optional
			String scope = request.getParameter("scope");//required
			String s = request.getParameter("secure");//optional, "0" or "1", default "0"
			boolean secure = "1".equals(s);
			s = request.getParameter("session");//optional, "0" or "1", default "0"
			boolean session = "1".equals(s);
			
			//forward to authentication page
			String authenticationPage = this.getAuthenticationPage(next, hd, scope, secure, session);
			RequestDispatcher rd = request.getRequestDispatcher(authenticationPage);
			rd.forward(request, response);
			
		} else if ("/AuthSubSessionToken".equals(pathInfo)){
			String value = this.parseToken(request);
			OneTimeToken oneTimeToken = new OneTimeToken(value);
			
			//delegate to subclass
			try {
				value = oneTimeToken.getValue();
				if(value==null || value.length()==0){
					throw new NotAuthorizedException("Not Authorized");
				}
				SessionToken sessionToken = this.generateSessionToken(oneTimeToken);//NotAuthorizedException
				
				//Send generated session token in http headers in the response body
				String sessionTokenValue = sessionToken.getValue();
				String expirationString = sessionToken.getExpirationString();
				PrintWriter writer = response.getWriter();
				writer.write("Token=" + sessionTokenValue + CRLF);
				writer.write("Expiration=" + expirationString + CRLF);
			} catch (NotAuthorizedException nae){
				response.sendError(401, "Not Authorized");
			}

		} else if ("/AuthSubRevokeToken".equals(pathInfo)){
			String value = this.parseToken(request);
			Token token = new Token(value);
			
			//delegate to subclass
			try {
				this.revokeToken(token);//NotAuthorizedException				
				//Send just 200 Ok: do nothing here.
			} catch (NotAuthorizedException nae){
				response.sendError(401, "Not Authorized");
			}

		} else if ("/AuthSubTokenInfo".equals(pathInfo)){
			String value = this.parseToken(request);
			Token token = new Token(value);
			
			//delegate to subclass
			try {
				TokenInfo tokenInfo = this.getTokenInfo(token);//NotAuthorizedException
				
				//Send token info in http headers in the response body
				String target = tokenInfo.getTarget();
				String scope = tokenInfo.getScope();
				boolean secure = tokenInfo.getSecure();
				PrintWriter writer = response.getWriter();
				writer.write("Target=" + target + CRLF);
				writer.write("Scope=" + scope + CRLF);
				writer.write("Secure=" + secure + CRLF);
			} catch (NotAuthorizedException nae){
				response.sendError(401, "Not Authorized");
			}

		} else {
			//invalid request: do nothing
		}
	}
	
	/** Receives the form submission of the authentication page,
	 * parse the data, delegates authentication and token generation
	 * to the subclass, and returns an http response.
	 * @param request
	 * @param response
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!request.isSecure()){
			response.sendError(401, "Must use https.");
			return;
		}

		//Parse parameters
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String next = request.getParameter("next");//required
		String hd = request.getParameter("hd");//optional, URL-encoded
		String scope = request.getParameter("scope");//required
		String s = request.getParameter("secure");//optional, "0" or "1", default "0"
		boolean secure = "1".equals(s);
		s = request.getParameter("session");//optional, "0" or "1", default "0"
		boolean session = "1".equals(s);
		
		//delegate authentication
		boolean ok = this.authenticate(username, password, next, hd, scope, secure, session);
		
		if (ok){
			//generate and send token
			OneTimeToken token = this.generateOneTimeToken(username, next, hd, scope, secure, session);
			String tokenValue = token.getValue();
			HttpURL url = new HttpURL(next);
			url.setParameter("token", tokenValue);
			String redirect = url.toString();
			response.sendRedirect(redirect);
		} else {
			//username,password are bad.
			//forward to authentication page again. (Forwarding a POST is a bit unusual, but ok.)
			String authenticationPage = this.getAuthenticationPage(next, hd, scope, secure, session);
			RequestDispatcher rd = request.getRequestDispatcher(authenticationPage);
			rd.forward(request, response);

		}
	}
	
	//Private helpers ---------------------------------------------------------
	
	/** Parse the token from an Authorization header in the request
	 * @param request
	 * @return token value
	 * */
	private String parseToken(HttpServletRequest request){
		String value = request.getHeader("Authorization");
		String[] ss = value.split("\"");
		if(ss.length > 1){
			value = ss[1];
			return value;
		}else{
			return null;
		}
	}

	//Abstract methods, to be implemented by subclass --------------------------

	/** Exchanges a one-time token for a session token.
	 * @param token The one-time token
	 * @return SessionToken, if the one-time token is valid
	 * @exception NotAuthorizedException If the token is not valid.
	 * */
	protected abstract SessionToken generateSessionToken(OneTimeToken token) throws NotAuthorizedException;
	
	/** Gives the URL of the authentication page that should be sent to the user.
	 * @param next
	 * @param hd
	 * @param scope
	 * @param secure
	 * @param session
	 * @return a relative URL that can be used in request.getRequestDispatcher(path)
	 * */
	protected abstract String getAuthenticationPage(String next, String hd, String scope, boolean secure, boolean session);
	
	/** Tries to authenticate user credentials, that is, checks username and password.
	 * @param username as submitted by user
	 * @param password as submitted by user
	 * @param next
	 * @param hd
	 * @param scope
	 * @param secure
	 * @param session
	 * @return Is username-password correct for a login?
	 * */
	protected abstract boolean authenticate(String username, String password, String next, String hd, String scope, boolean secure, boolean session);

	/** Generates and stores a new one-time token.
	 * @param username already checked
	 * @param next
	 * @param hd
	 * @param scope
	 * @param secure
	 * @param session
	 * @return new one-time token, to be returned to the client
	 * */
	protected abstract OneTimeToken generateOneTimeToken(String username, String next, String hd, String scope, boolean secure, boolean session);

	/** Gets info about a token, such as if it is valid.
	 * @param token The token to be validated. May be OneTimeToken or SessionToken.
	 * @return token info
	 * @throws NotAuthorizedException
	 * */
	public abstract TokenInfo getTokenInfo(Token token) throws NotAuthorizedException;
	
	/** Revokes a token, and returns 200 Ok.
	 * @param token The method only really makes sense for a session token, 
	 * 	but it accepts session tokens and one-time tokens.
	 * @throws NotAuthorizedException The token is not valid.
	 * */
	protected abstract void revokeToken(Token token) throws NotAuthorizedException;
}