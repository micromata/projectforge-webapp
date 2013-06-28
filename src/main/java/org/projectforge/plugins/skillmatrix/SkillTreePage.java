/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractReindexTopRightMenu;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillTreePage extends AbstractSecuredPage
{

  private static final long serialVersionUID = -3902220283833390881L;

  public static final String USER_PREFS_KEY_OPEN_SKILLS = "openSkills";

  public static final String I18N_KEY_SKILLTREE_TITLE = "plugins.skillmatrix.title.list";

  public static final String I18N_KEY_SKILLTREE_INFO = "plugins.skillmatrix.skilltree.info";

  private SkillTreeForm form;

  private ISelectCallerPage caller;

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  private String selectProperty;

  private SkillListPage skillListPage;

  /**
   * @param parameters
   */
  public SkillTreePage(final PageParameters parameters)
  {
    super(parameters);
    init();
  }

  /**
   * Called if the user clicks on button "tree view".
   * @param skillListPage
   * @param parameters
   */
  public SkillTreePage(final SkillListPage skillListPage, final PageParameters parameters)
  {
    super(parameters);
    this.skillListPage = skillListPage;
    init();
  }

  public SkillTreePage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(new PageParameters());
    this.caller = caller;
    this.selectProperty = selectProperty;
    init();
  }

  @SuppressWarnings("serial")
  private void init()
  {
    if (isSelectMode() == false) {
      final ContentMenuEntryPanel menuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
        @Override
        public void onClick()
        {
          final PageParameters params = new PageParameters();
          final AbstractEditPage< ? , ? , ? > editPage = new SkillEditPage(params);
          editPage.setReturnToPage(SkillTreePage.this);
          setResponsePage(editPage);
        };
      }, IconType.PLUS);
      addContentMenuEntry(menuEntry);

      new AbstractReindexTopRightMenu(contentMenuBarPanel, accessChecker.isLoggedInUserMemberOfAdminGroup()) {
        @Override
        protected void rebuildDatabaseIndex(final boolean onlyNewest)
        {
          if (onlyNewest == true) {
            skillDao.rebuildDatabaseIndex4NewestEntries();
          } else {
            skillDao.rebuildDatabaseIndex();
          }
        }

        @Override
        protected String getString(final String i18nKey)
        {
          return SkillTreePage.this.getString(i18nKey);
        }
      };
    }
    form = new SkillTreeForm(this);
    body.add(form);
    form.init();
    final SkillTreeBuilder skillTreeBuilder = new SkillTreeBuilder().setCaller(caller).setSelectProperty(selectProperty);
    form.add(skillTreeBuilder.createTree("tree", this, form.getSearchFilter()));

    body.add(new Label("info", new Model<String>(getString(I18N_KEY_SKILLTREE_INFO))));
  }

  public void refresh()
  {
    form.getSearchFilter().resetMatch();
  }

  /**
   * @return true, if this page is called for selection by a caller otherwise false.
   */
  public boolean isSelectMode()
  {
    return this.caller != null;
  }

  protected void onSearchSubmit()
  {
    refresh();
  }

  protected void onResetSubmit()
  {
    form.getSearchFilter().reset();
    refresh();
    form.clearInput();
  }

  protected void onListViewSubmit()
  {
    if (skillListPage != null) {
      setResponsePage(skillListPage);
    } else {
      setResponsePage(new SkillListPage(this, getPageParameters()));
    }
  }

  protected void onCancelSubmit()
  {
    if (isSelectMode() == true) {
      WicketUtils.setResponsePage(this, caller);
      caller.cancelSelection(selectProperty);
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString(I18N_KEY_SKILLTREE_TITLE);
  }

}
