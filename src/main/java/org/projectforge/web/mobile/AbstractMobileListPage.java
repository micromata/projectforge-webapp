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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.core.BaseDO;
import org.projectforge.web.address.AddressMobileEditPage;
import org.projectforge.web.address.AddressMobileViewPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.components.LabelBookmarkablePageLinkPanel;
import org.springframework.util.CollectionUtils;

public abstract class AbstractMobileListPage<F extends AbstractMobileListForm< ? , ? >, D extends org.projectforge.core.IDao< ? >, O extends BaseDO< ? >>
    extends AbstractSecuredMobilePage
{
  protected static final int MAX_ROWS = 50;

  protected F form;

  protected List<O> list;

  protected String i18nKey;

  protected WebMarkupContainer resultList;

  protected RepeatingView resultRepeater;

  public AbstractMobileListPage(final String i18nKey, final PageParameters parameters)
  {
    super(parameters);
    this.i18nKey = i18nKey;
    form = newListForm(this);
    add(form);
    form.init();
    add(resultList = new WebMarkupContainer("resultList"));
    resultList.setVisible(false);
    search();
  }

  @SuppressWarnings("unchecked")
  protected void search()
  {
    if (resultRepeater != null) {
      resultList.remove(resultRepeater);
    }
    if (StringUtils.isBlank(form.filter.getSearchString()) == true) {
      list = null;
    } else {
      list = (List<O>) getBaseDao().getList(form.filter);
    }
    if (CollectionUtils.isEmpty(list) == true) {
      resultList.setVisible(false);
      return;
    }
    resultList.setVisible(true);
    resultList.add(resultRepeater = new RepeatingView("resultRepeater"));

    int counter = 0;
    for (final O entry : list) {
      final PageParameters params = new PageParameters();
      params.put(AbstractEditPage.PARAMETER_KEY_ID, entry.getId());
      resultRepeater.add(new PageItemEntryMenuPanel(resultRepeater.newChildId(), AddressMobileViewPage.class, params, null,
          getEntryName(entry), getEntryComment(entry)));
      if (++counter >= MAX_ROWS) {
        break;
      }
    }
  }

  /**
   * @return The value to show in the list.
   */
  protected abstract String getEntryName(final O entry);

  /**
   * @return The value to show as a comment in the list or null at default.
   */
  protected String getEntryComment(final O entry)
  {
    return null;
  }

  protected abstract D getBaseDao();

  @Override
  protected void addRightButton()
  {
    add(new LabelBookmarkablePageLinkPanel(RIGHT_BUTTON_ID, AddressMobileEditPage.class, " + "));
  }

  protected abstract F newListForm(AbstractMobileListPage< ? , ? , ? > parentPage);

  @Override
  protected String getTitle()
  {
    return getString(i18nKey + ".title.heading");
  }
}
