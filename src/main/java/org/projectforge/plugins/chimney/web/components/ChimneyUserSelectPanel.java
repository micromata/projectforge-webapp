/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.user.UserSelectPanel;

/**
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public class ChimneyUserSelectPanel extends UserSelectPanel
{

  /**
   * 
   */
  private static final long serialVersionUID = 945510585755186547L;

  private static final String SELECT_PROPERTY = "userId";

  @SpringBean(name = "userDao")
  private static UserDao userDao;

  /**
   * @param id
   * @param model
   * @param caller
   * @param selectProperty
   */
  public ChimneyUserSelectPanel(final String id, final IModel<PFUserDO> model)
  {
    super(id, model, SelectHandler.getInstance(), SELECT_PROPERTY);
    SelectHandler.getInstance().setUserSelectPanel(this);
  }

  private static class SelectHandler implements Serializable, ISelectCallerPage {

    /***/
    private static final long serialVersionUID = 8057927767941040210L;

    private static SelectHandler instance = null;

    public static SelectHandler getInstance() {
      if (instance == null)
        instance = new SelectHandler();
      return instance;
    }

    private UserSelectPanel usp;

    private void setUserSelectPanel(final UserSelectPanel usp) {
      this.usp = usp;
    }



    @Override
    public void select(final String property, final Object selectedValue)
    {
      if (SELECT_PROPERTY.equals(property) == true) {
        final Integer id;
        if (selectedValue instanceof String) {
          id = NumberHelper.parseInteger((String) selectedValue);
        } else {
          id = (Integer) selectedValue;
        }

        if (id != null) {
          final PFUserDO user = userDao.getOrLoad(id);
          usp.setModelObject(user);
        }
      }
    }

    @Override
    public void unselect(final String property) {}

    @Override
    public void cancelSelection(final String property) {}
  }



}
