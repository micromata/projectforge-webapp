/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.wizard;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.user.UserSelectPanel;

public class ProjectAdditionalFieldsPanel extends Panel implements ISelectCallerPage
{
  private static final long serialVersionUID = 4089420228243221573L;

  @SpringBean(name="userDao")
  private UserDao userDao;

  private final IModel<ProjectDO> model;

  public ProjectAdditionalFieldsPanel(final String id, final IModel<ProjectDO> model)
  {
    super(id, null);
    this.model = model;
    init();
  }

  private void init()
  {
    final PropertyModel<PFUserDO> userModel = new PropertyModel<PFUserDO>(model, "responsibleUser");
    final UserSelectPanel userSelectPanel = new UserSelectPanel("userSelect", userModel, this, "userId");
    //userSelectPanel.setRequired(true);
    add(userSelectPanel);
    userSelectPanel.init();
  }

  @Override
  public void select(final String property, final Object selectedValue)
  {
    if ("userId".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }

      if (id != null) {
        final PFUserDO user = userDao.getOrLoad(id);
        model.getObject().setResponsibleUser(user);
      }
    }
  }

  @Override
  public void unselect(final String property)
  {
    if ("userId".equals(property) == true) {
      model.getObject().setResponsibleUser(null);
    }
  }

  @Override
  public void cancelSelection(final String property)
  {
    // do nothing
  }

}
