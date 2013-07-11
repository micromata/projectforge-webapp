/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import javax.persistence.MappedSuperclass;

import org.projectforge.core.DefaultBaseDO;
import org.projectforge.plugins.chimney.wbs.visitors.IVisitor;

@MappedSuperclass
public abstract class AbstractVisitableBaseDO<T extends IVisitor> extends DefaultBaseDO implements IVisitable<T>
{
  private static final long serialVersionUID = 503207462522700624L;

}
