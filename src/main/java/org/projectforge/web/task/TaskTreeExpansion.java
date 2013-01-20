/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.task;

import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.projectforge.task.TaskNode;
import org.projectforge.web.wicket.tree.TableTreeExpansion;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TaskTreeExpansion extends TableTreeExpansion<Integer, TaskNode>
{
  private static final long serialVersionUID = 5151537746424532422L;

  public static TaskTreeExpansion get()
  {
    TaskTreeExpansion expansion = Session.get().getMetaData(KEY);
    if (expansion == null) {
      expansion = new TaskTreeExpansion();

      Session.get().setMetaData(KEY, expansion);
    }
    return expansion;
  }

  @SuppressWarnings("serial")
  public static IModel<Set<TaskNode>> getExpansionModel()
  {
    return new AbstractReadOnlyModel<Set<TaskNode>>() {
      /**
       * @see org.apache.wicket.model.AbstractReadOnlyModel#getObject()
       */
      @Override
      public Set<TaskNode> getObject()
      {
        return get();
      }
    };
  }

  private static MetaDataKey<TaskTreeExpansion> KEY = new MetaDataKey<TaskTreeExpansion>() {
    private static final long serialVersionUID = 1L;
  };
}
