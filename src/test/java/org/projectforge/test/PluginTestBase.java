/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.settings.IResourceSettings;
import org.mockito.Mockito;
import org.projectforge.admin.SystemUpdater;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.plugins.core.PluginsRegistry;
import org.springframework.beans.BeansException;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class PluginTestBase extends AbstractTestBase
{
  public static void init(final AbstractPlugin... plugins) throws BeansException, IOException
  {
    final List<String> persistentEntries = new ArrayList<String>();
    final PluginsRegistry pluginsRegistry = PluginsRegistry.instance();
    for (final AbstractPlugin plugin : plugins) {
      pluginsRegistry.register(plugin);
      for (final Class< ? > persistentEntry : plugin.getPersistentEntities()) {
        persistentEntries.add(persistentEntry.getName());
      }
    }
    preInit();
    pluginsRegistry.set(getTestConfiguration().getBean("systemUpdater", SystemUpdater.class));
    pluginsRegistry.set(getTestConfiguration().getBeanFactory(), Mockito.mock(IResourceSettings.class));
    pluginsRegistry.initialize();
    tablesToDeleteAfterTests = persistentEntries.toArray(new String[0]);
    init(true);
  }

}
