/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.user.UserSelectPanel;

public class ResponsibleUserPanel<T extends AbstractWbsNodeDO> extends Panel implements ISelectCallerPage
{

  private static final long serialVersionUID = 7858710113107692012L;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private final IModel<T> wbsNodeModel;

  public ResponsibleUserPanel(final String id, final IModel<T> wbsNodeModel)
  {
    super(id, wbsNodeModel);
    this.wbsNodeModel = wbsNodeModel;

    init();
  }

  private void init()
  {
    addNodeResponsibleUser();
    addProjectResponsibleUser();
  }

  protected void addNodeResponsibleUser()
  {
    final PropertyModel<PFUserDO> userModel = new PropertyModel<PFUserDO>(wbsNodeModel, "responsibleUser");
    final UserSelectPanel userSelect = new UserSelectPanel("userSelect", userModel, this, "userId");
    userSelect.init();
    add(userSelect);
  }

  protected void addProjectResponsibleUser()
  {
    final ProjectDO project = WicketWbsUtils.getProject(wbsNodeModel.getObject());
    String userName = "-";

    if (project != null && project.getResponsibleUser() != null) {
      userName = project.getResponsibleUser().getFullname();
    }

    add(new Label("projectResponsible", userName));
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
        wbsNodeModel.getObject().setResponsibleUser(user);
      }
    }
  }

  @Override
  public void unselect(final String property)
  {
    if ("userId".equals(property) == true) {
      wbsNodeModel.getObject().setResponsibleUser(null);
    }
  }

  @Override
  public void cancelSelection(final String property)
  {
    // do nothing
  }

}
