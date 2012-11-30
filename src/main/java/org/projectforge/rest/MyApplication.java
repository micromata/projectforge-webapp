/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
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
