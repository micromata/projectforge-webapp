/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.access;

import java.util.ResourceBundle;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.projectforge.core.MessageParam;
import org.projectforge.core.MessageParamType;
import org.projectforge.core.ProjectForgeException;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;

/**
 * This class will be thrown by AccessChecker, if no access is given for the demanded action by an user.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class AccessException extends ProjectForgeException
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccessException.class);
  
  private static final long serialVersionUID = 147795804616526958L;

  public final static String I18N_KEY_STANDARD = "access.exception.standard";

  public final static String I18N_KEY_STANDARD_WITH_TASK = "access.exception.standardWithTask";

  private TaskTree taskTree;

  protected PFUserDO user = null;

  protected Integer taskId = null;

  protected AccessType accessType = null;

  protected String message = null;

  protected String i18nKey = null;

  protected Object[] params = null;

  protected OperationType operationType = null;

  protected Class< ? > clazz = null;

  public AccessException(String i18nKey, Object... params)
  {
    this.i18nKey = i18nKey;
    this.params = params;
    log.info("AccessException: " + this);
  }

  public AccessException(AccessType accessType, OperationType operationType)
  {
    final PFUserDO user = PFUserContext.getUser();
    this.user = user;
    this.accessType = accessType;
    this.operationType = operationType;
    this.i18nKey = I18N_KEY_STANDARD;
    log.info("AccessException: " + this);
  }

  public AccessException(Integer taskId, AccessType accessType, OperationType operationType)
  {
    final PFUserDO user = PFUserContext.getUser();
    this.user = user;
    this.taskId = taskId;
    this.accessType = accessType;
    this.operationType = operationType;
    this.i18nKey = I18N_KEY_STANDARD_WITH_TASK;
    log.info("AccessException: " + this);
  }

  /**
   * The order of the args is task id, accessType and operationType.
   * @return the arguments for the message formatter from type Object[3].
   */
  public Object[] getMessageArgs(ResourceBundle bundle)
  {
    Object[] result = new Object[3];
    if (taskTree != null && this.taskId != null) {
      TaskDO task = taskTree.getTaskById(taskId);
      if (task != null) {
        result[0] = task.getTitle();
      } else {
        result[0] = taskId;
      }
    } else {
      result[0] = taskId;
    }
    if (accessType != null) {
      result[1] = bundle.getString("access.type." + accessType);
    }
    if (operationType != null) {
      result[2] = bundle.getString("access.type." + operationType);
    }
    return result;
  }
  
  public MessageParam[] getMessageArgs() {
    MessageParam[] result = new MessageParam[3];
    if (taskTree != null && this.taskId != null) {
      TaskDO task = taskTree.getTaskById(taskId);
      if (task != null) {
        result[0] = new MessageParam(task.getTitle());
      } else {
        result[0] = new MessageParam(taskId);
      }
    } else {
      result[0] = new MessageParam(taskId);
    }
    if (accessType != null) {
      result[1] = new MessageParam("access.type." + accessType, MessageParamType.I18N_KEY);
    }
    if (operationType != null) {
      result[2] = new MessageParam("access.type." + operationType, MessageParamType.I18N_KEY);
    }
    return result;
  }

  public String getI18nKey()
  {
    return this.i18nKey;
  }

  public void setI18nKey(String i18nKey)
  {
    this.i18nKey = i18nKey;
  }

  /**
   * The optional params of the i18n message.
   */
  public Object[] getParams()
  {
    return this.params;
  }

  public PFUserDO getUser()
  {
    return this.user;
  }

  public Integer getTaskId()
  {
    return this.taskId;
  }

  public AccessType getAccessType()
  {
    return accessType;
  }

  public void setAccessType(AccessType accessType)
  {
    this.accessType = accessType;
  }

  public String getMessage()
  {
    return this.message;
  }

  public TaskNode getTaskNode()
  {
    return getTaskTree().getTaskNodeById(this.taskId);
  }

  /**
   * Class infos about class, for which the AccessException was thrown, e. g. for logging.
   * @return Returns the clazz.
   */
  public Class< ? > getClazz()
  {
    return clazz;
  }

  /**
   * @param clazz The clazz to set.
   */
  public void setClazz(Class< ? > clazz)
  {
    this.clazz = clazz;
  }

  /**
   * @return Returns the operationType.
   */
  public OperationType getOperationType()
  {
    return operationType;
  }

  /**
   * @param operationType The operationType to set.
   */
  public void setOperationType(OperationType operationType)
  {
    this.operationType = operationType;
  }

  /**
   * @param message The message to set.
   */
  public void setMessage(String message)
  {
    this.message = message;
  }

  /**
   * @param user The user to set.
   */
  public void setUser(PFUserDO user)
  {
    this.user = user;
  }

  public TaskTree getTaskTree()
  {
    return taskTree;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  @Override
  public String toString()
  {
    ToStringBuilder builder = new ToStringBuilder(this);
    if (user != null) {
      builder.append("user", String.valueOf(user.getId()) + ":" + user.getUsername());
    }
    if (taskId != null) {
      TaskDO task = taskTree != null ? taskTree.getTaskById(taskId) : null;
      String ts = task != null ? ":" + task.getShortDisplayName() : "";
      builder.append("task", String.valueOf(taskId) + ts);
    }
    if (accessType != null) {
      builder.append("accessType", accessType.toString());
    }
    if (operationType != null) {
      builder.append("operationType", operationType.toString());
    }
    if (clazz != null) {
      builder.append("class", clazz.toString());
    }
    if (i18nKey != null) {
      builder.append("i18nKey", i18nKey);
    }
    if (message != null) {
      builder.append("message", message);
    }
    if (params != null) {
      builder.append("params", params);
    }
    return builder.toString();
  }
}
