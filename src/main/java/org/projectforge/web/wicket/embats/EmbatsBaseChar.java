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

package org.projectforge.web.wicket.embats;

import org.projectforge.web.wicket.ImageDef;

/**
 * Defines embats symbols with their fall-back images if the browser doesn't support embedded fonts.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public enum EmbatsBaseChar implements EmbatsChar
{
  ARROW_RIGHT("S", ImageDef.STAR);

  private String fontString;

  private ImageDef fallbackImage;

  @Override
  public String getCssClass()
  {
    return "embats";
  }

  @Override
  public ImageDef getFallbackImage()
  {
    return fallbackImage;
  }

  @Override
  public String getFontString()
  {
    return fontString;
  }

  private EmbatsBaseChar(final String fontString, final ImageDef fallbackImage)
  {
    this.fontString = fontString;
    this.fallbackImage = fallbackImage;
  }
}
