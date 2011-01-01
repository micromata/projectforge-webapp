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

package org.projectforge.web.wicket;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;

public class DownloadUtils
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DownloadUtils.class);

  public static final String TYPE_JPEG = "image/jpeg";

  public static final String TYPE_MS_PROJECT = "application/vnd.ms-project";

  public static final String TYPE_OCTET_STREAM = "application/octet-stream";

  public static final String TYPE_PDF = "application/pdf";

  public static final String TYPE_PNG = "image/png";

  public static final String TYPE_SVG = "image/svg+xml";

  public static final String TYPE_TEXT = "text";

  public static final String TYPE_XLS = "application/vnd.ms-excel";

  public static final String TYPE_XML = "application/xml";

  /**
   * Mime type etc. is done automatically.
   * @param content The content of the file to download.
   * @param filename
   */
  public static void setDownloadTarget(final byte[] content, final String filename)
  {
    setDownloadTarget(content, filename, null);
  }

  /**
   * @param content The content of the file to download.
   * @param filename
   * @param contentType For setting contentType manually.
   */
  public static void setDownloadTarget(final byte[] content, final String filename, final String contentType)
  {
    final ByteArrayResourceStream byteArrayResourceStream;
    if (contentType != null) {
      byteArrayResourceStream = new ByteArrayResourceStream(content, filename, contentType);
    } else {
      byteArrayResourceStream = new ByteArrayResourceStream(content, filename);
    }
    final ResourceStreamRequestTarget target = new ResourceStreamRequestTarget(byteArrayResourceStream);
    target.setFileName(filename);
    RequestCycle.get().setRequestTarget(target);
    log.info("Starting download for file. filename:" + filename + ", content-type:" + byteArrayResourceStream.getContentType());
  }

  /**
   * Determines content type dependent on the file name suffix. Yet supported: application/pdf (*.pdf), application/vnd.ms-excel (*.xls),
   * image/jpeg (*.jpg, *.jpeg), image/svg+xml (*.svg), image/png (*.xml), application/xml (*.xml) and text (*.txt, *.csv).
   * @param filename
   * @return
   */
  public static String getContentType(final String filename)
  {
    if (filename == null) {
      return TYPE_OCTET_STREAM;
    } else if (filename.endsWith(".jpg") == true || filename.endsWith(".jpeg") == true) {
      return TYPE_JPEG;
    } else if (filename.endsWith(".pdf") == true) {
      return TYPE_PDF;
    } else if (filename.endsWith(".svg") == true) {
      return TYPE_SVG;
    } else if (filename.endsWith(".png") == true) {
      return TYPE_PNG;
    } else if (filename.endsWith(".xml") == true) {
      return TYPE_XML;
    } else if (filename.endsWith(".xls") == true) {
      return TYPE_XLS;
    } else if (filename.endsWith(".txt") == true || filename.endsWith(".csv") == true) {
      return TYPE_TEXT;
    } else if (filename.endsWith(".mpx") == true) {
      return TYPE_MS_PROJECT;
    }
    log.error("Unknown file type: " + filename);
    return "";
  }
}
