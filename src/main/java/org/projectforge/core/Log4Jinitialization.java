/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class Log4Jinitialization implements InitializingBean
{
  private final Properties props = new Properties();

  private Resource[] resources;

  public void setConfigLocations(final Resource[] resources)
  {
    this.resources = resources;
  }

  public void setConfigProperties(final Properties props)
  {
    this.props.putAll(props);
  }

  public void afterPropertiesSet() throws Exception
  {
    if (resources != null) {
      for (Resource element : resources) {
        props.load(element.getInputStream());
      }
    }
    PropertyConfigurator.configure(props);
  }
}
