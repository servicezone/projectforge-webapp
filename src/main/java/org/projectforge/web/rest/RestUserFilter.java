/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.MDC;
import org.projectforge.common.NumberHelper;
import org.projectforge.rest.Authentication;
import org.projectforge.rest.ConnectionSettings;
import org.projectforge.rest.converter.DateTimeFormat;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.WebConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Does the authentication stuff for restfull requests.
 * @author Daniel Ludwig (d.ludwig@micromata.de)
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class RestUserFilter implements Filter
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RestUserFilter.class);

  @Autowired
  UserDao userDao;

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException
  {
    // NOOP
  }

  /**
   * Authentication via request header.
   * <ol>
   * <li>Authentication userId (authenticationUserId) and authenticationToken (authenticationToken) or</li>
   * <li>Authentication username (authenticationUsername) and password (authenticationPassword) or</li>
   * </ol>
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException,
  ServletException
  {
    if (WebConfiguration.isUpAndRunning() == false) {
      log.error("System isn't up and running, rest call denied. The system is may-be in start-up phase or in maintenance mode.");
      final HttpServletResponse resp = (HttpServletResponse) response;
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return;
    }
    final HttpServletRequest req = (HttpServletRequest) request;
    String userString = getAttribute(req, Authentication.AUTHENTICATION_USER_ID);
    PFUserDO user = null;
    if (userString != null) {
      final Integer userId = NumberHelper.parseInteger(userString);
      if (userId != null) {
        final String authenticationToken = getAttribute(req, Authentication.AUTHENTICATION_TOKEN);
        if (authenticationToken != null) {
          if (authenticationToken.equals(userDao.getCachedAuthenticationToken(userId)) == true) {
            user = userDao.getUserGroupCache().getUser(userId);
          } else {
            log.error(Authentication.AUTHENTICATION_TOKEN
                + " doesn't match for "
                + Authentication.AUTHENTICATION_USER_ID
                + " '"
                + userId
                + "'. Rest call forbidden.");
          }
        } else {
          log.error(Authentication.AUTHENTICATION_TOKEN + " not given for userId '" + userId + "'. Rest call forbidden.");
        }
      } else {
        log.error(Authentication.AUTHENTICATION_USER_ID + " is not an integer: '" + userString + "'. Rest call forbidden.");
      }
    } else {
      userString = getAttribute(req, Authentication.AUTHENTICATION_USERNAME);
      final String password = getAttribute(req, Authentication.AUTHENTICATION_PASSWORD);
      if (userString != null && password != null) {
        final String encryptedPassword = userDao.encryptPassword(password);
        user = userDao.authenticateUser(userString, encryptedPassword);
        if (user == null) {
          log.error("Authentication failed for "
              + Authentication.AUTHENTICATION_USERNAME
              + "='"
              + userString
              + "' with given password. Rest call forbidden.");
        }
      } else {
        log.error("Neither "
            + Authentication.AUTHENTICATION_USER_ID
            + " nor "
            + Authentication.AUTHENTICATION_USERNAME
            + "/"
            + Authentication.AUTHENTICATION_PASSWORD
            + " is given. Rest call forbidden.");
      }
    }
    if (user == null) {
      try {
        // Avoid brute force attack:
        Thread.sleep(1000);
      } catch (final InterruptedException ex) {
        log.fatal("Exception encountered while Thread.sleep(1000): " + ex, ex);
      }
      final HttpServletResponse resp = (HttpServletResponse) response;
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    try {
      PFUserContext.setUser(user);
      final ConnectionSettings settings = getConnectionSettings(req);
      ConnectionSettings.set(settings);
      final String ip = request.getRemoteAddr();
      if (ip != null) {
        MDC.put("ip", ip);
      } else {
        // Only null in test case:
        MDC.put("ip", "unknown");
      }
      MDC.put("user", user.getUsername());
      log.info("Rest-call: " + ((HttpServletRequest) request).getRequestURI());
      chain.doFilter(request, response);
    } finally {
      PFUserContext.setUser(null);
      ConnectionSettings.set(null);
      MDC.remove("ip");
      MDC.remove("user");
    }
  }

  private ConnectionSettings getConnectionSettings(final HttpServletRequest req)
  {
    final ConnectionSettings settings = new ConnectionSettings();
    final String dateTimeFormatString = getAttribute(req, ConnectionSettings.DATE_TIME_FORMAT);
    if (dateTimeFormatString != null) {
      final DateTimeFormat dateTimeFormat = DateTimeFormat.valueOf(dateTimeFormatString.toUpperCase());
      if (dateTimeFormat != null) {
        settings.setDateTimeFormat(dateTimeFormat);
      }
    }
    return settings;
  }

  private String getAttribute(final HttpServletRequest req, final String key)
  {
    String value = req.getHeader(key);
    if (value == null) {
      value = req.getParameter(key);
    }
    return value;
  }

  @Override
  public void destroy()
  {
    // NOOP
  }

  public UserDao getUserDao()
  {
    return userDao;
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

}
