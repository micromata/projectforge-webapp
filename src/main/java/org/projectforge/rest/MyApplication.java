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

package org.projectforge.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.projectforge.task.rest.TaskDaoRest;

/**
 * Initial servlet for initiating the restful services.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@ApplicationPath("/")
public class MyApplication extends Application
{
  /**
   * @return all restful service classes.
   * @see javax.ws.rs.core.Application#getClasses()
   */
  @Override
  public Set<Class< ? >> getClasses()
  {
    final Set<Class< ? >> classes = new HashSet<Class< ? >>();
    // register root resource
    classes.add(TaskDaoRest.class);
    return classes;
  }
}
