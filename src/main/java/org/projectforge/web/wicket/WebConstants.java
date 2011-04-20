/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.wicket;

import org.apache.wicket.behavior.SimpleAttributeModifier;

public class WebConstants
{
  public static final String FILE_IMAGE_DIMENSIONS = "imageDimensions.xml";

  public static final String FILE_I18N_KEYS = "i18nKeys.properties";

  /** For setting the caller action page as parameter for the callee. */
  public static final String PARAMETER_CALLER = "caller";

  public static final String PARAMETER_USER_ID = "uid";

  public static final String PARAMETER_DATE = "date";

  private static final String DIR = "/images/";

  public static final String IMAGE_ACCEPT = DIR + "accept.png";

  public static final String IMAGE_ADD = DIR + "add.png";

  public static final String IMAGE_ARROW_DOWN = DIR + "arrow-down.png";

  public static final String IMAGE_BIRTHDAY = DIR + "cake.png";

  public static final String IMAGE_BIRTHDAY_DELETE = DIR + "cake_delete.png";

  public static final String IMAGE_BULLET_STAR = DIR + "bullet_star.png";

  public static final String IMAGE_BUTTON_ASSIGN_TO_LEFT = DIR + "button_assignToLeft.png";

  public static final String IMAGE_BUTTON_ASSIGN_TO_RIGHT = DIR + "button_assignToRight.png";

  public static final String IMAGE_BUTTON_CANCEL = DIR + "button_cancel.png";

  public static final String IMAGE_CALENDAR_NEXT_MONTH = DIR + "css_img/rightInactive.png";

  public static final String IMAGE_CALENDAR_PREVIOUS_MONTH = DIR + "css_img/leftInactive.png";

  public static final String IMAGE_CALENDAR_SELECT_WEEK = DIR + "button_calendar_week.png";

  public static final String IMAGE_CALENDAR = DIR + "calendar.png";

  public static final String IMAGE_CLOCK = DIR + "clock.png";

  public static final String IMAGE_CLOCK_DELETE = DIR + "clock_delete.png";

  public static final String IMAGE_COG = DIR + "cog.png";

  public static final String IMAGE_DATABASE_DELETE = DIR + "database_delete.png";

  public static final String IMAGE_DATABASE_INSERT = DIR + "database_insert.png";

  public static final String IMAGE_DATABASE_SELECT = DIR + "database_select.png";

  public static final String IMAGE_DATABASE_UPDATE = DIR + "database_update.png";

  public static final String IMAGE_DATE_SELECT = DIR + "button_selectDate.png";

  public static final String IMAGE_DATE_UNSELECT = DIR + "button_unselectDate.png";

  public static final String IMAGE_DELETE = DIR + "trash.png";

  public static final String IMAGE_DENY = DIR + "deny.png";

  public static final String IMAGE_EDIT = DIR + "pencil.png";

  public static final String IMAGE_EXCLAMATION = DIR + "exclamation.png";

  public static final String IMAGE_EYE = DIR + "eye.png";

  public static final String IMAGE_EXPORT_EXCEL = DIR + "page_white_excel.png";

  public static final String IMAGE_EXPORT_PDF = DIR + "page_white_acrobat.png";

  public static final String IMAGE_FEEDBACK = DIR + "comment.png";

  public static final String IMAGE_FIND = DIR + "find.png";

  public static final String IMAGE_GANTT = DIR + "gantt.png";

  public static final String IMAGE_GROUP_SELECT = DIR + "button_selectGroup.png";

  public static final String IMAGE_GROUP_UNSELECT = DIR + "button_unselectGroup.png";

  public static final String IMAGE_HELP = DIR + "help.png";

  public static final String IMAGE_HELP_KEYBOARD = DIR + "keyboard.png";

  public static final String IMAGE_INFO = DIR + "information.png";

  public static final String IMAGE_KOST2_SELECT = DIR + "coins_add.png";

  public static final String IMAGE_KOST2_UNSELECT = DIR + "coins_delete.png";

  public static final String IMAGE_KUNDE_SELECT = DIR + "button_selectCustomer.png";

  public static final String IMAGE_KUNDE_UNSELECT = DIR + "button_unselectCustomer.png";

  @Deprecated
  public static final String IMAGE_MICROMATA_MENU_ICON = DIR + "micromata_icon.png";

  public static final String IMAGE_NEW = DIR + "new.png";

  public static final String IMAGE_PAGE_COPY = DIR + "page_copy.png";

  public static final String IMAGE_PAGE_PASTE = DIR + "page_paste.png";

  public static final String IMAGE_PHONE = DIR + "telephone.png";

  public static final String IMAGE_PHONE_MOBILE = DIR + "phone.png";

  public static final String IMAGE_PHONE_HOME = DIR + "house.png";

  public static final String IMAGE_PROJEKT_SELECT = DIR + "button_selectProjekt.png";

  public static final String IMAGE_PROJEKT_UNSELECT = DIR + "button_unselectProjekt.png";

  public static final String IMAGE_PRINTER = DIR + "printer.png";

  public static final String IMAGE_QUICKSELECT_CURRENT_MONTH = DIR + "button_calendar_month.png";

  public static final String IMAGE_QUICKSELECT_CURRENT_WEEK = DIR + "button_calendar_week.png";

  public static final String IMAGE_QUICKSELECT_FOLLOWING_MONTH = DIR + "button_next_month.png";

  public static final String IMAGE_QUICKSELECT_FOLLOWING_WEEK = DIR + "button_next_week.png";

  public static final String IMAGE_QUICKSELECT_PREVIOUS_MONTH = DIR + "button_previous_month.png";

  public static final String IMAGE_QUICKSELECT_PREVIOUS_WEEK = DIR + "button_previous_week.png";

  public static final String IMAGE_STAR_PLUS = DIR + "star_plus.png";

  public static final String IMAGE_TABLE_ROW_ADD = DIR + "table_row_insert.png";

  public static final String IMAGE_TASK_SELECT = DIR + "button_selectTask.png";

  public static final String IMAGE_TASK_UNSELECT = DIR + "button_unselectTask.png";

  public static final String IMAGE_UNDELETE = DIR + "arrow_undo.png";

  public static final String IMAGE_USER_SELECT = DIR + "button_selectUser.png";

  public static final String IMAGE_USER_SELECT_ME = DIR + "button_selectMe.png";

  public static final String IMAGE_USER_UNSELECT = DIR + "button_unselectUser.png";

  public static final String IMAGE_SPACER = DIR + "spacer.gif";

  public static final String IMAGE_TREE_ICON_LEAF = DIR + "leaf.gif";

  public static final String IMAGE_TREE_ICON_FOLDER = DIR + "folder.gif";

  public static final String IMAGE_TREE_ICON_FOLDER_OPEN = DIR + "folder_open.gif";

  public static final String IMAGE_TREE_ICON_EXPLOSION = DIR + "explosion.gif";

  public static final int IMAGE_TREE_ICON_HEIGHT = 15;

  public static final int IMAGE_TREE_ICON_WIDTH = 19;

  public static final String CSS_BACKGROUND_COLOR_RED = "background-color: #eeaaaa;";

  /**
   * Used as class attribute for input fields.
   */
  public static final String CSS_INPUT_STDTEXT = "stdtext";

  public static final SimpleAttributeModifier BUTTON_CLASS = new SimpleAttributeModifier("class", "button");

  public static final SimpleAttributeModifier BUTTON_CLASS_CANCEL = new SimpleAttributeModifier("class", "button");

  public static final SimpleAttributeModifier BUTTON_CLASS_NOBUTTON = new SimpleAttributeModifier("class", "nobutton");

  public static final SimpleAttributeModifier BUTTON_CLASS_RESET = new SimpleAttributeModifier("class", "reset");

  public static final SimpleAttributeModifier BUTTON_CLASS_DEFAULT = new SimpleAttributeModifier("class", "submit");

  public static final SimpleAttributeModifier HELP_CLASS = new SimpleAttributeModifier("class", "help");
}
