/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.util.Set;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.projectforge.web.user.UserPreferencesHelper;
import org.projectforge.web.wicket.tree.TableTreeExpansion;

/**
 * @author Billy Duong (b.duong@micromata.de)
 *
 */
public class SkillTreeExpansion extends TableTreeExpansion<Integer, SkillNode>
{

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SkillTreeExpansion.class);

  private static final long serialVersionUID = 5151537746424532422L;

  private static SkillTreeExpansion get()
  {
    final SkillTreeExpansion expansion = new SkillTreeExpansion();
    try {
      @SuppressWarnings("unchecked")
      final Set<Integer> ids = (Set<Integer>) UserPreferencesHelper.getEntry(SkillTreePage.USER_PREFS_KEY_OPEN_SKILLS);
      if (ids != null) {
        expansion.setIds(ids);
      } else {
        // Persist the open entries in the data-base.
        UserPreferencesHelper.putEntry(SkillTreePage.USER_PREFS_KEY_OPEN_SKILLS, expansion.getIds(), true);
      }
    } catch (final Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    return expansion;
  }

  /**
   * @return The expansion model. Any previous persisted state of open rows will be restored from {@link UserPreferencesHelper}.
   */
  @SuppressWarnings("serial")
  public static IModel<Set<SkillNode>> getExpansionModel()
  {
    return new AbstractReadOnlyModel<Set<SkillNode>>() {
      /**
       * @see org.apache.wicket.model.AbstractReadOnlyModel#getObject()
       */
      @Override
      public Set<SkillNode> getObject()
      {
        return get();
      }
    };
  }

}
