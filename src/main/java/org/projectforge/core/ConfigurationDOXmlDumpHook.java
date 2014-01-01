/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.core;

import org.projectforge.database.XmlDumpHook;
import org.projectforge.database.xstream.XStreamSavingConverter;
import org.projectforge.task.TaskDO;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ConfigurationDOXmlDumpHook implements XmlDumpHook
{
  /**
   * @see org.projectforge.database.XmlDumpHook#onBeforeRestore(org.projectforge.database.xstream.XStreamSavingConverter, java.lang.Object)
   */
  @Override
  public void onBeforeRestore(final XStreamSavingConverter xstreamSavingConverter, final Object obj)
  {
    if (obj instanceof ConfigurationDO) {
      final ConfigurationDO configurationDO = (ConfigurationDO) obj;
      if (configurationDO.getConfigurationType() != ConfigurationType.TASK) {
        return;
      }
      final Integer oldTaskId = configurationDO.getTaskId();
      final Integer newTaskId = xstreamSavingConverter.getNewIdAsInteger(TaskDO.class, oldTaskId);
      configurationDO.setTaskId(newTaskId);
      return;
    }
  }
}
