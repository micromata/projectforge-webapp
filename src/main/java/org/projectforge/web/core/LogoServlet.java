/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.lf5.util.StreamUtils;
import org.projectforge.core.Configuration;
import org.springframework.core.io.ClassPathResource;


/**
 * Servlet for displaying a customizable logo image (see config.xml).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LogoServlet extends HttpServlet
{
  public static final String URL = "secure/Logo";

  private static final long serialVersionUID = 4091672008912713345L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LogoServlet.class);

  private static boolean initialized = false;

  private static File LOGO_FILE;

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
  {
    log.debug("Start doPost");
    if (initialized == false) {
      // Synchronization not really needed, multiple initialization works.
      final String logo = Configuration.getInstance().getLogoFile();
      if (logo != null) {
        final String logoPath = Configuration.getInstance().getResourcePath() + "/images/" + logo;
        final File logoFile = new File(logoPath);
        if (logoFile.canRead() == true) {
          LOGO_FILE = logoFile;
          log.info("Use configured logo: " + logoPath);
        } else {
          log.error("Configured logo not found: " + logoPath);
        }
      }
      initialized = true;
    }
    byte[] bytes = null;
    if (LOGO_FILE != null) {
      try {
        bytes = FileUtils.readFileToByteArray(LOGO_FILE);
      } catch (final IOException ex) {
        log.error(ex.getMessage(), ex);
      }
      if (bytes == null) {
        log.error("Error while reading logo file. Fall back to default logo.");
      }
    }
    if (bytes == null) {
      final ClassPathResource cpres = new ClassPathResource("images/default-logo.png");
      final InputStream in = cpres.getInputStream();
      bytes = StreamUtils.getBytes(in);
    }
    resp.setContentLength(bytes.length);
    resp.getOutputStream().write(bytes);
    resp.getOutputStream().flush();
  }
}
