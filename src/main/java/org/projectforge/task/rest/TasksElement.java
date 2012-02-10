/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.task.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.projectforge.task.TaskDO;

/**
 * Simpler Container für eine Liste von {@link TaskDOElement}. Benötigt man da
 * man ArrayLists per JAXB nicht so ohne weiteres serialisieren kann.
 * 
 * @author Daniel Ludwig (d.ludwig@micromata.de)
 * 
 */
@XmlRootElement(name = "tasks")
public class TasksElement extends ArrayList<TaskDOElement> {
	private static final long serialVersionUID = -2612008151293451504L;

	public TasksElement() {
		super();
	}

	public TasksElement(final Collection<? extends TaskDOElement> arg0) {
		super(arg0);
	}

	public TasksElement(final int arg0) {
		super(arg0);
	}

	public void convertAll(final Collection<TaskDO> tasks) {
		if (tasks == null)
			return;
		for (final TaskDO ta : tasks) {
			add(new TaskDOElement(ta));
		}
	}

	@XmlElement(name = "task")
	public List<TaskDOElement> getTasks() {
		return this;
	}
}
