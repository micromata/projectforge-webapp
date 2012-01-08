/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;

/**
 * Needed for download files generated of byte arrays.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ByteArrayResourceStream implements IResourceStream
{
  private static final long serialVersionUID = 102937904470626593L;

  private String contentType;

  private byte[] content;

  /**
   * @param content
   * @param filename Only needed for determine the mime type.
   */
  public ByteArrayResourceStream(final byte[] content, final String filename)
  {
    this.content = content;
    contentType = DownloadUtils.getContentType(filename);
  }

  /**
   * @param content
   * @param filename Only needed for determine the mime type.
   */
  public ByteArrayResourceStream(final byte[] content, final String filename, final String contentType)
  {
    this.content = content;
    this.contentType = contentType;
  }

  /**
   * @param contentType Mime type.
   */
  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }

  public void close() throws IOException
  {
    // ByteArrayInputStream.close() has no effect.
  }

  public String getContentType()
  {
    return contentType;
  }

  public InputStream getInputStream() throws ResourceStreamNotFoundException
  {
    return new ByteArrayInputStream(content);
  }

  public Locale getLocale()
  {
    throw new UnsupportedOperationException();
  }

  public long length()
  {
    return content.length;
  }

  public void setLocale(Locale locale)
  {
    throw new UnsupportedOperationException();
  }

  public Time lastModifiedTime()
  {
    return null;
  }
}
