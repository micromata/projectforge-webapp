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

package org.projectforge.web.mobile;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.projectforge.common.NumberHelper;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.layout.AbstractRenderer;
import org.projectforge.web.wicket.layout.LayoutContext;

public abstract class AbstractMobileViewPage extends AbstractSecuredMobilePage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractMobileViewPage.class);

  protected String i18nPrefix;

  protected Class< ? extends WebPage> editPageClass;

  protected Integer id;

  public AbstractMobileViewPage(final PageParameters parameters, final Class< ? extends WebPage> editPageClass, final String i18nPrefix)
  {
    super(parameters);
    this.editPageClass = editPageClass;
    this.i18nPrefix = i18nPrefix;
    id = getPageParameters().getAsInteger(AbstractEditPage.PARAMETER_KEY_ID);
    if (NumberHelper.greaterZero(id) == true) {
      final AbstractRenderer renderer = createRenderer(new LayoutContext(true, true, false), id);
      if (renderer != null) {
        renderer.add();
      }
    } else {
      log.error("Oups, no object id given. Can't display object.");
    }
  }

  protected abstract AbstractRenderer createRenderer(final LayoutContext layoutContext, final Integer objectId);

  @Override
  protected void addTopRightButton()
  {
    if (editPageClass != null && NumberHelper.greaterZero(id) == true) {
      final PageParameters params = new PageParameters();
      params.put(AbstractEditPage.PARAMETER_KEY_ID, id);
      headerContainer.add(new JQueryButtonPanel(TOP_RIGHT_BUTTON_ID, JQueryButtonType.CHECK, editPageClass, params, getString("edit")));
    } else {
      super.addTopRightButton();
    }
  }

  @Override
  protected String getTitle()
  {
    return getString(i18nPrefix + "title.view");
  }
}
