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

package org.projectforge.web.core;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.ConfigurationListener;

/**
 * Servlet for displaying a customizable logo image (see config.xml).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LogoServlet extends HttpServlet implements ConfigurationListener
{
  public static final String BASE_URL = "secure/Logo";

  private static final long serialVersionUID = 4091672008912713345L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LogoServlet.class);

  private static boolean initialized = false;

  private static File logoFile;

  /**
   * Extracts the servlet name Logo.png, Logo.jpg or Logo.gif. The extension is only needed for some browsers for detecting the correct logo
   * file format.
   * @return The servlet path or null, if no logo file is given.
   */
  public static String getBaseUrl()
  {
    final String filename = ConfigXml.getInstance().getLogoFile();
    if (StringUtils.isEmpty(filename) == true) {
      return null;
    } else if (filename.endsWith(".png") == true) {
      return BASE_URL + ".png";
    } else if (filename.endsWith(".jpg") == true || filename.endsWith(".jpeg") == true) {
      return BASE_URL + ".jpg";
    } else {
      return BASE_URL + ".gif";
    }
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
  {
    log.debug("Start doPost");
    if (initialized == false) {
      // Synchronization not really needed, multiple initialization works.
      final String logo = ConfigXml.getInstance().getLogoFile();
      if (logo != null) {
        final String logoPath;
        if (new File(logo).isAbsolute() == true) {
          logoPath = logo;
        } else {
          logoPath = ConfigXml.getInstance().getResourcePath() + "/images/" + logo;
        }
        final File file = new File(logoPath);
        if (file.canRead() == true) {
          logoFile = file;
          log.info("Use configured logo: " + logoPath);
        } else {
          log.error("Configured logo not found: " + logoPath);
        }
      }
      ConfigXml.getInstance().register(this);
      initialized = true;
    }
    byte[] bytes = null;
    if (logoFile != null) {
      try {
        bytes = FileUtils.readFileToByteArray(logoFile);
      } catch (final IOException ex) {
        log.error(ex.getMessage(), ex);
      }
      if (bytes == null) {
        log.error("Error while reading logo file.");
      }
    }
    if (bytes == null) {
      // final ClassPathResource cpres = new ClassPathResource("images/default-logo.png");
      // final InputStream in = cpres.getInputStream();
      // bytes = StreamUtils.getBytes(in);
      bytes = new byte[0];
    }
    resp.setContentLength(bytes.length);
    resp.getOutputStream().write(bytes);
    resp.getOutputStream().flush();
  }

  @Override
  public void afterRead()
  {
    initialized = false;
  }
}
