/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs.visitors;

import org.projectforge.core.I18nEnum;

/**
 * Enum for signalling why a node cannot be a child of another
 * @author Sweeps <pf@byte-storm.com>
 */
public enum InvalidChildErrorType implements I18nEnum
{

  /** Project cannot be a child */
  PROJECT_CHILD_ILLEGAL("projectillegalchild"),
  /** Milestone can only be child of project */
  MILESTONE_CHILD_ILLEGAL("milestoneillegalchild"),
  /** Subtask can only be child of project or subtask */
  SUBTASK_CHILD_ILLEGAL("subtaskillegalchild"),
  /** Subtask can only be child of project or subtask */
  WORKPACKAGE_CHILD_ILLEGAL("workpackageillegalchild"),
  /** Subtask can only be child of project or subtask */
  PHASE_CHILD_ILLEGAL("phaseillegalchild"),
  /** Error for unsupported child types */
  UNSUPPORTED("unsupported"),
  /** Setting the parent to one of the node's children would create a loop */
  LOOP_DETECTED("loopillegal"),
  /** Setting the parent to one of the node's children would create a loop */
  CHILD_TO_ITSELF_ILLEGAL("childtoitselfillegal");

  private String key;

  /**
   * @return The key suffix will be used e. g. for i18n.
   */
  public String getKey()
  {
    return key;
  }

  /**
   * @return The full i18n key including the i18n prefix "plugins.chimney.enum.invalidchilderror.".
   */
  @Override
  public String getI18nKey()
  {
    return "plugins.chimney.enum.invalidchilderror." + key;
  }

  InvalidChildErrorType(final String key)
  {
    this.key = key;
  }

}
