/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.task.rest;

import javax.xml.bind.annotation.XmlType;

import org.projectforge.task.TaskDO;

@XmlType
public class TaskDOElement {
	private String title;

	private String description;

	/**
	 * Für JAXB relevant.
	 */
	public TaskDOElement() {
		super();
	}

	public TaskDOElement(final TaskDO task) {
		title = task.getTitle();
		description = task.getDescription();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
}