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
