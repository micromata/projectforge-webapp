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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebResponse;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.projectforge.export.JFreeChartImageType;

public class JFreeChartImage extends Image
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JFreeChartImage.class);

  private static final long serialVersionUID = 7083627817914127250L;

  private int width;

  private int height;

  private JFreeChartImageType imageType;

  public JFreeChartImage(final String id, final JFreeChart chart, final int width, final int height)
  {
    this(id, chart, null, width, height);
  }

  public JFreeChartImage(final String id, final JFreeChart chart, final JFreeChartImageType imageType, final int width, final int height)
  {
    super(id, new Model<JFreeChart>(chart));
    this.width = width;
    this.height = height;
    this.imageType = imageType;
  }

  @SuppressWarnings("serial")
  @Override
  protected Resource getImageResource()
  {
    final String format = this.imageType == JFreeChartImageType.JPEG ? "jpg" : "png";
    return new DynamicImageResource(format) {
      @Override
      protected byte[] getImageData()
      {
        try {
          final JFreeChart chart = (JFreeChart) getDefaultModelObject();
          final ByteArrayOutputStream baos = new ByteArrayOutputStream();
          if (imageType == JFreeChartImageType.JPEG) {
            ChartUtilities.writeChartAsJPEG(baos, chart, width, height);
          } else {
            ChartUtilities.writeChartAsPNG(baos, chart, width, height);
          }
          final byte[] ba = baos.toByteArray();
          return ba;
        } catch (final IOException ex) {
          log.error(ex.getMessage(), ex);
          return null;
        }
      }

      @Override
      protected void setHeaders(WebResponse response)
      {
        if (isCacheable()) {
          super.setHeaders(response);
        } else {
          response.setHeader("Pragma", "no-cache");
          response.setHeader("Cache-Control", "no-cache");
          response.setDateHeader("Expires", 0);
        }
      }
    };
  }

}
