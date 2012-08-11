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
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.ConfigXml;
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
    if (StringUtils.isBlank(req.getParameter("nr")) || StringUtils.containsOnly(req.getParameter("nr"), "+1234567890 -/") == false) {
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

    final String searchNumber = cleanNumber(req.getParameter("nr").trim());
    final AddressDao addressDao = (AddressDao) Registry.instance().getDao(AddressDao.class);

    final BaseSearchFilter alf = new BaseSearchFilter();
    alf.setSearchString(searchNumber);

    final List<AddressDO> list = addressDao.getList(alf);
    if (list.size() >= 1) {
      AddressDO result = list.get(0);
      if (list.size() > 1) {
        for (final AddressDO matchingUser : list) {
          if (matchingUser.getLastUpdate().after(result.getLastUpdate())) {
            result = matchingUser;
          }
        }
      }

      resp.setContentType("text/plain");
      if (result.getOrganization() != null && result.getFullName() != null){
        resp.getOutputStream().print(result.getFullName() + "; " + result.getOrganization());
      } else {
        if (result.getOrganization() == null)
          resp.getOutputStream().print(result.getFullName());
        if (result.getFullName() == null)
          resp.getOutputStream().print(result.getOrganization());
      }
    } else {
      /* mit Thomas abgesprochen. */
      resp.getOutputStream().print(0);
    }
  }

  /**
   * Delete '0' (zero), '+' or ' '
   * 
   * @param number
   * @return
   */
  private String cleanNumber(String number)
  {
    if (number.startsWith("+") || number.startsWith("0")){
      while(number.length() > 0){
        if (number.charAt(0) == '0' || number.charAt(0) == '+' || number.charAt(0) == ' ')
          number = number.substring(1);
        else
          break;
      }
    }
    System.out.println(number);
    return number;
  }
}
