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

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.EditPageSupport;
import org.projectforge.web.wicket.IEditPage;
import org.projectforge.web.wicket.WicketUtils;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class AbstractMobileEditPage<O extends AbstractBaseDO< ? >, F extends AbstractMobileEditForm<O, ? >, D extends BaseDao<O>>
    extends AbstractSecuredMobilePage implements IEditPage<O, D>
{
  protected F form;

  protected String i18nPrefix;

  private EditPageSupport<O, D> editPageSupport;

  public AbstractMobileEditPage(final PageParameters parameters, final String i18nPrefix)
  {
    super(parameters);
    this.i18nPrefix = i18nPrefix;
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
    this.editPageSupport = new EditPageSupport<O, D>(this, getBaseDao(), getData());
  }

  /**
   * User has clicked the submit button, so create or update the data object.
   */
  protected void save()
  {
    if (isNew() == true) {
      create();
    } else {
      update();
    }
  }

  /**
   * User has clicked the save button for storing a new item.
   */
  protected void create()
  {
    this.editPageSupport.create();
  }

  /**
   * User has clicked the update button for updating an existing item.
   */
  protected void update()
  {
    this.editPageSupport.update();
  }

  protected void undelete()
  {
    this.editPageSupport.undelete();
  }

  protected void markAsDeleted()
  {
    this.editPageSupport.markAsDeleted();
  }

  protected void delete()
  {
    this.editPageSupport.delete();
  }

  @Override
  public WebPage afterSave()
  {
    return null;
  }

  @Override
  public WebPage afterSaveOrUpdate()
  {
    return null;
  }

  @Override
  public WebPage afterUpdate(boolean modified)
  {
    return null;
  }

  @Override
  public void clearIds()
  {
    getData().setId(null);
  }

  @Override
  public boolean isAlreadySubmitted()
  {
    return this.alreadySubmitted;
  }

  @Override
  public WebPage onDelete()
  {
    return null;
  }

  @Override
  public WebPage onSaveOrUpdate()
  {
    return null;
  }

  @Override
  public WebPage onUndelete()
  {
    return null;
  }

  @Override
  public void setAlreadySubmitted(boolean alreadySubmitted)
  {
    this.alreadySubmitted = alreadySubmitted;
  }

  @Override
  public void setResponsePage()
  {
    if (this.returnToPage != null) {
      setResponsePageAndHighlightedRow(this.returnToPage);
    } else {
      final EditMobilePage ann = getClass().getAnnotation(EditMobilePage.class);
      final Class< ? extends AbstractSecuredMobilePage> redirectPage;
      if (ann != null && ann.defaultReturnPage() != null) {
        redirectPage = getClass().getAnnotation(EditMobilePage.class).defaultReturnPage();
      } else {
        redirectPage = WicketUtils.getDefaultMobilePage();
      }
      final PageParameters params = new PageParameters();
      params.put(AbstractListPage.PARAMETER_HIGHLIGHTED_ROW, getData().getId());
      setResponsePage(redirectPage, params);
    }
  }

  /**
   * Does nothing (not yet supported). Is this use-ful on a mobile device?
   * @see org.projectforge.web.wicket.IEditPage#setResponsePageAndHighlightedRow(org.apache.wicket.markup.html.WebPage)
   */
  @Override
  public void setResponsePageAndHighlightedRow(WebPage page)
  {
  }

  protected O getData()
  {
    if (form == null || form.getData() == null) {
      getLogger().error("Data of form is null. Maybe you have forgotten to call AbstractEditPage.init() in constructor.");
    }
    return form.getData();
  }

  /**
   * @see AbstractEditPage#isNew
   */
  public boolean isNew()
  {
    if (form == null) {
      getLogger().error("Data of form is null. Maybe you have forgotten to call AbstractEditPage.init() in constructor.");
    }
    return (getData() == null || getData().getId() == null);
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
