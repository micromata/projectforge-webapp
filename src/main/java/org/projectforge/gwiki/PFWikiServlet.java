/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.gwiki;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.util.file.File;
import org.projectforge.web.WebConfiguration;
import org.springframework.core.io.ClassPathResource;

import de.micromata.genome.gwiki.web.GWikiServlet;

public class PFWikiServlet extends GWikiServlet
{
  private static final long serialVersionUID = 3105890991358969791L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PFWikiServlet.class);

  @Override
  public void init(final ServletConfig config) throws ServletException
  {
    if (WebConfiguration.isGWikiAvailable() == false) {
      // GWiki shouldn't be available, so don't init it.
      return;
    }
    super.init(config);

    // copies the PFContents.zip to base dir and extracts each file, if necessary
    try {
      final String baseDirName = config.getServletContext().getInitParameter("base.dir") + "/gwiki/";
      final String contentsName = "/gwiki/PFContents.zip";

      final ClassPathResource res = new ClassPathResource(contentsName);
      final ZipFile zip = new ZipFile(res.getFile());

      final Enumeration< ? extends ZipEntry> entries = zip.entries();

      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        final File file = new File(baseDirName + entry.getName());

        if (file.exists()) {
          continue;
        }

        if (entry.isDirectory()) {
          file.mkdir();
        } else {
          IOUtils.copy(zip.getInputStream(entry), new FileOutputStream(baseDirName + entry.getName()));
        }
      }

      zip.close();
    } catch (final IOException ex) {
      log.fatal("Exception encountered " + ex, ex);
    }

    log.info("copying init files if not present");
  }
}
