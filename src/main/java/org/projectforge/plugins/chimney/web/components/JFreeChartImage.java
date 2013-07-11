/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebResponse.CacheScope;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.time.Duration;
import org.jfree.chart.JFreeChart;

public class JFreeChartImage extends Image
{

  /**
   * 
   */
  private static final long serialVersionUID = -975133334888372555L;

  private final int width;
  private final int height;

  public JFreeChartImage(final String id, final JFreeChart chart, final int width, final int height){
    super(id, new Model<JFreeChart>(chart));
    this.width = width;
    this.height = height;
  }

  @Override
  protected IResource getImageResource() {
    final DynamicImageResource resource = new DynamicImageResource() {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      protected byte[] getImageData(final Attributes attributes) {
        final JFreeChart chart = (JFreeChart) getDefaultModelObject();
        return toImageData(chart.createBufferedImage(width, height));
      }

      @Override
      protected void configureResponse(final ResourceResponse response, final Attributes attributes) {
        super.configureResponse(response, attributes);

        //if (isCacheAble() == false) {
        //if (getCachingStrategy() != null) {
        response.setCacheDuration(Duration.NONE);
        response.setCacheScope(CacheScope.PRIVATE);
        //}
      }

    };

    return resource;
  }

}
