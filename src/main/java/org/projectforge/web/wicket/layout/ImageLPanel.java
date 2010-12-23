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

package org.projectforge.web.wicket.layout;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.ContextImage;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.components.TooltipImage;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ImageLPanel extends AbstractLPanel
{
  private static final long serialVersionUID = -2323278625643532689L;

  /**
   * Wicket id.
   */
  public static final String IMAGE_ID = "image";

  private ContextImage image;

  public ImageLPanel(final String id, final ImageDef imageDef)
  {
    super(id, null);
    addImage(new PresizedImage(IMAGE_ID, getResponse(), imageDef.getPath()));
  }

  public ImageLPanel(final String id, final ImageDef imageDef, final String tooltip)
  {
    super(id, null);
    addImage(new TooltipImage(IMAGE_ID, getResponse(), imageDef.getPath(), tooltip));
  }

  public ImageLPanel(final String id, final ContextImage image)
  {
    super(id, null);
    this.classAttributeAppender = "select";
    add(image);
  }

  /**
   * @param image
   * @return this for chaining.
   */
  public ImageLPanel addImage(final ContextImage image)
  {
    this.image = image;
    add(image);
    return this;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return image;
  }
}
