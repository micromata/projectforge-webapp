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

package org.projectforge.web.mobile;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.components.LabelBookmarkablePageLinkPanel;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class AbstractMobileEditPage<O extends AbstractBaseDO< ? >, F extends AbstractMobileEditForm< ? , ? >, D extends BaseDao<O>>
    extends AbstractSecuredMobilePage
{
  protected F form;

  protected String i18nPrefix;

  public AbstractMobileEditPage(final PageParameters parameters, final String i18nPrefix)
  {
    super(parameters);
    this.i18nPrefix = i18nPrefix;
    leftNavigationRepeater.add(new LabelBookmarkablePageLinkPanel(leftNavigationRepeater.newChildId(), getListPageClass(),
        getString("list")));
  }

  protected abstract Class< ? extends AbstractMobileListPage< ? , ? , ? >> getListPageClass();

  protected void init()
  {
    init(null);
  }

  @SuppressWarnings("unchecked")
  protected void init(O data)
  {
    final Integer id = getPageParameters().getAsInteger(AbstractEditPage.PARAMETER_KEY_ID);
    if (data == null) {
      if (NumberHelper.greaterZero(id) == true) {
        data = getBaseDao().getById(id);
      }
      if (data == null) {
        data = (O) getPageParameters().get(AbstractEditPage.PARAMETER_KEY_DATA_PRESET);
        if (data == null) {
          data = getBaseDao().newInstance();
        }
      }
    }
    form = newEditForm(this, data);

    add(form);
    form.init();
    // add(new Label("title", getString(AbstractEditPage.getTitleKey(i18nPrefix, isNew()))));
  }

  /**
   * @see AbstractEditPage#isNew
   */
  protected boolean isNew()
  {
    if (form == null) {
      getLogger().error("Data of form is null. Maybe you have forgotten to call AbstractEditPage.init() in constructor.");
    }
    return (form.getData() == null || form.getData().getId() == null);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getTitleKey(String, boolean)
   */
  @Override
  protected String getTitle()
  {
    return getString(AbstractEditPage.getTitleKey(i18nPrefix, isNew()));
  }

  protected abstract D getBaseDao();

  protected abstract Logger getLogger();

  protected abstract F newEditForm(AbstractMobileEditPage< ? , ? , ? > parentPage, O data);
}
