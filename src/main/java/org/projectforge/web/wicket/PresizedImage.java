/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Response;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.image.ContextImage;
import org.projectforge.core.Configuration;
import org.projectforge.web.core.ImageDimension;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlObjectReader;

public class PresizedImage extends ContextImage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PresizedImage.class);

  private static final long serialVersionUID = -1456397971086283625L;

  private static final Map<String, Dimension> registry;

  static {
    log.info("Reading image dimensions from file " + WebConstants.FILE_IMAGE_DIMENSIONS + " ...");
    registry = new HashMap<String, Dimension>();
    final ClassLoader cLoader = Configuration.class.getClassLoader();
    final InputStream is = cLoader.getResourceAsStream(WebConstants.FILE_IMAGE_DIMENSIONS);
    final XmlObjectReader reader = new XmlObjectReader();
    final AliasMap aliasMap = new AliasMap();
    aliasMap.put(List.class, "images");
    reader.setAliasMap(aliasMap).initialize(ImageDimension.class);
    String xml = null;
    try {
      xml = IOUtils.toString(is);
    } catch (IOException ex) {
      log.error("Couldn't read image dimensions file: " + ex.getMessage(), ex);
    }
    if (xml == null) {
      log.error("Couldn't read image dimensions file.");
    } else {
      @SuppressWarnings("unchecked")
      final List<ImageDimension> list = (List<ImageDimension>) reader.read(xml);
      for (final ImageDimension dimension : list) {
        registry.put(dimension.getPath(), dimension.getDimension());
      }
      log.info("Reading image dimensions from file was successful: " + WebConstants.FILE_IMAGE_DIMENSIONS);
    }
  }

  /**
   * @param id
   * @param request
   * @param relativePath path relative to web apps image dir.
   */
  public PresizedImage(final String id, final Response response, final String relativePath)
  {
    super(id, WicketUtils.getImageUrl(response, relativePath));
    Dimension dimension = registry.get(relativePath);
    if (dimension == null) {
      log.error("Image " + relativePath + " not found (please update image dimension file via GetImageDimensionsTest).");
    } else {
      add(new SimpleAttributeModifier("height", String.valueOf(dimension.height)));
      add(new SimpleAttributeModifier("width", String.valueOf(dimension.width)));
    }
  }
}
