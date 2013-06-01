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

package org.projectforge.core;

import org.projectforge.common.Logger;
import org.projectforge.common.LoggerBridgeLog4j;
import org.projectforge.export.MyXlsExportContext;

/**
 * Doing some initialization stuff and stuff on shutdown (planned). Most stuff is yet done by WicketApplication.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ProjectForgeApp
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProjectForgeApp.class);

  private static ProjectForgeApp instance;

  public synchronized static void init()
  {
    if (instance != null) {
      log.warn("ProjectForge is already initialized!");
      return;
    }
    instance = new ProjectForgeApp();
    instance.internalInit();
  }

  public static void shutdown()
  {
    if (instance == null) {
      log.error("ProjectForge isn't initialized, can't excecute shutdown!");
      return;
    }
    instance.internalShutdown();
  }

  private void internalInit()
  {
    log.info("Initializing...");
    // Log4j for ProjectForge modules: common, excel and continuous-db.
    Logger.setLoggerBridge(new LoggerBridgeLog4j());
    // Initialize Excel extensions:
    new MyXlsExportContext();
  }

  private void internalShutdown()
  {
    log.info("Shutdown...");
  }
}
