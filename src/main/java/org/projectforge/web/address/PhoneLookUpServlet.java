/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.address;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.QueryFilter;
import org.projectforge.registry.Registry;

/**
 * Assign a phone number to a full name and organization using ProjectForge address database.
 * 
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Path("/phonelookup")
public class PhoneLookUpServlet extends HttpServlet
{
  private static final long serialVersionUID = 8042634752943344080L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PhoneLookUpServlet.class);

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
  {
    final String number = req.getParameter("nr");
    if (StringUtils.isBlank(number) == true || StringUtils.containsOnly(number, "+1234567890 -/") == false) {
      log.warn("Bad request, request parameter nr not given or contains invalid characters (only +0123456789 -/ are allowed): " + number);
      resp.sendError(HttpStatus.SC_BAD_REQUEST);
      return;
    }

    final String key = req.getParameter("key");
    final String expectedKey = ConfigXml.getInstance().getPhoneLookupKey();
    if (StringUtils.isBlank(expectedKey) == true) {
      log.warn("Servlet call for receiving phonelookups ignored because phoneLookupKey is not given in config.xml file.");
      resp.sendError(HttpStatus.SC_BAD_REQUEST);
      return;
    }
    if (expectedKey.equals(key) == false) {
      log.warn("Servlet call for phonelookups ignored because phoneLookupKey does not match given key: " + key);
      resp.sendError(HttpStatus.SC_FORBIDDEN);
      return;
    }

    final String searchNumber = NumberHelper.extractPhonenumber(number);
    final AddressDao addressDao = (AddressDao) Registry.instance().getDao(AddressDao.class);

    final BaseSearchFilter alf = new BaseSearchFilter();
    alf.setSearchString("*" + searchNumber);
    final QueryFilter queryFilter = new QueryFilter(alf);

    final StringBuffer buf = new StringBuffer();
    final List<AddressDO> list = addressDao.internalGetList(queryFilter);
    if (list.size() >= 1) {
      AddressDO result = list.get(0);
      if (list.size() > 1) {
        // More than one result, therefore find the newest one:
        buf.append("+"); // Mark that more than one entry does exist.
        for (final AddressDO matchingUser : list) {
          if (matchingUser.getLastUpdate().after(result.getLastUpdate()) == true) {
            result = matchingUser;
          }
        }
      }
      resp.setContentType("text/plain");
      final String fullname = result.getFullName();
      boolean first = true;
      if (StringUtils.isNotBlank(fullname) == true) {
        buf.append(fullname);
        first = false;
      }
      final String organization = result.getOrganization();
      if (StringUtils.isNotBlank(organization) == true) {
        if (first == false) {
          buf.append("; ");
        }
        buf.append(organization);
      }
      resp.getOutputStream().print(buf.toString());
    } else {
      /* mit Thomas abgesprochen. */
      resp.getOutputStream().print(0);
    }
  }
}
