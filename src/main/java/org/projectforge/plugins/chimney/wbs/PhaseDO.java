/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.projectforge.core.Priority;
import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeVisitor;
import org.projectforge.plugins.chimney.wbs.visitors.WbsNodeChildValidationVisitor;
import org.projectforge.task.TaskStatus;
import org.projectforge.user.PFUserDO;

@Entity
@DiscriminatorValue("Phase")
public class PhaseDO extends AbstractWbsNodeDO
{

  private String phaseName;

  private String phaseDescription;

  private static final long serialVersionUID = -5002206219029512227L;

  @Override
  protected boolean childIsOfValidType(final AbstractWbsNodeDO newNode)
  {
    final WbsNodeChildValidationVisitor visitor = new WbsNodeChildValidationVisitor(this);
    newNode.accept(visitor);
    return visitor.isValidChild();
  }

  @Override
  public void accept(final IWbsNodeVisitor visitor)
  {
    visitor.visit(this);
  }

  @Override
  @Transient
  public String getTitle()
  {
    return getPhaseName();
  }

  @Override
  public void setTitle(final String title)
  {
    setPhaseName(title);
  }

  public String getPhaseName()
  {
    return phaseName;
  }

  public void setPhaseName(final String phaseName)
  {
    this.phaseName = phaseName;
  }

  @Override
  protected void setNewParent(final AbstractWbsNodeDO newParent)
  {
    parent = newParent;
  }

  @Override
  public void setShortDescription(final String shortDescription)
  {
  }

  @Override
  @Transient
  public String getShortDescription()
  {
    return null;
  }

  @Override
  public void setDescription(final String description)
  {
    this.phaseDescription = description;
  }

  @Override
  @Transient
  public String getDescription()
  {
    return this.phaseDescription;
  }

  public String getPhaseDescription()
  {
    return phaseDescription;
  }

  public void setPhaseDescription(final String phaseDescription)
  {
    this.phaseDescription = phaseDescription;
  }

  @Override
  @Transient
  public TaskStatus getStatus()
  {
    return null;
  }

  @Override
  public void setStatus(final TaskStatus status)
  {
  }

  @Override
  @Transient
  public Priority getPriority()
  {
    return null;
  }

  @Override
  public void setPriority(final Priority priority)
  {
  }

  @Override
  @Transient
  public int getProgress()
  {
    return 0;
  }

  @Override
  public void setProgress(final int progress)
  {
  }

  @Override
  @Transient
  public PFUserDO getResponsibleUser()
  {
    return null;
  }

  @Override
  public void setResponsibleUser(final PFUserDO newUser)
  {
  }

}
