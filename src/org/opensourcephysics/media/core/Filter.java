/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2004  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;

import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * This is the abstract base class for all image filters. Note: subclasses
 * should always provide a no-argument constructor.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public abstract class Filter {

  // instance fields
  /** the VideoPanel this is used for */
  public VideoPanel vidPanel;
  /** true if the filter inspector is visible */
  public boolean inspectorVisible;
  /** the x-component of inspector position */
  public int inspectorX = Integer.MIN_VALUE;
  /** the y-component of inspector position */
  public int inspectorY;
  private boolean enabled = true;
  private String name;
  protected PropertyChangeSupport support;
  protected Action enabledAction;
  protected JCheckBoxMenuItem enabledItem;
  protected JMenuItem deleteItem;
  protected JMenuItem propertiesItem;
  protected boolean hasInspector;
  protected Frame frame;
	protected JButton closeButton;
	protected JButton ableButton;
	protected JButton clearButton;
	protected FilterStack stack; // set by stack when filter added

  /**
   * Constructs a Filter object.
   */
  protected Filter() {
    support = new SwingPropertyChangeSupport(this);
    // get the name of this filter from the class name
    name = getClass().getName();
    int i = name.lastIndexOf('.');
    if (i > 0 &&  i < name.length() - 1) {
      name = name.substring(i+1);
    }
    i = name.indexOf("Filter"); //$NON-NLS-1$
    if (i > 0 &&  i < name.length() - 1) {
      name = name.substring(0, i);
    }
    // set up menu items
    enabledAction = new AbstractAction(MediaRes.getString("Filter.MenuItem.Enabled")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        Filter.this.setEnabled(enabledItem.isSelected());
				refresh();
      }
    };
    enabledItem = new JCheckBoxMenuItem(enabledAction);
    enabledItem.setSelected(isEnabled());
    propertiesItem = new JMenuItem(MediaRes.getString("Filter.MenuItem.Properties")); //$NON-NLS-1$
    propertiesItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JDialog inspector = getInspector();
        if (inspector != null) {
          inspector.setVisible(true);
        }
      }
    });
		closeButton = new JButton();
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
        JDialog inspector = getInspector();
        if (inspector != null) {
          inspector.setVisible(false);
        }
			}
		});
		ableButton = new JButton();
		ableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enabledItem.setSelected(!enabledItem.isSelected());
				enabledAction.actionPerformed(null);
			}
		});
		clearButton = new JButton();
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
  }

  /**
   * Applies the filter to a source image and returns the result.
   * If the filter is not enabled, the source image should be returned.
   *
   * @param sourceImage the source image
   * @return the filtered image
   */
  public abstract BufferedImage getFilteredImage(BufferedImage sourceImage);

  /**
   * Returns a JDialog inspector for controlling filter properties.
   *
   * @return the inspector
   */
  public abstract JDialog getInspector();

  /**
   * Clears the filter. This default method does nothing.
   */
  public void clear() {/** empty block */}

	/**
	 * Refreshes this filter's GUI
	 */
	public void refresh() {
    enabledItem.setText(MediaRes.getString("Filter.MenuItem.Enabled")); //$NON-NLS-1$	
    propertiesItem.setText(MediaRes.getString("Filter.MenuItem.Properties")); //$NON-NLS-1$		
		closeButton.setText(MediaRes.getString("Filter.Button.Close")); //$NON-NLS-1$
		ableButton.setText(isEnabled()? 
						MediaRes.getString("Filter.Button.Disable"): //$NON-NLS-1$
						MediaRes.getString("Filter.Button.Enable")); //$NON-NLS-1$
		clearButton.setText(MediaRes.getString("Filter.Button.Clear")); //$NON-NLS-1$
		clearButton.setEnabled((isEnabled()));
	}

  /**
   * Sets whether this filter is enabled.
   *
   * @param enabled <code>true</code> if this is enabled.
   */
  public void setEnabled(boolean enabled) {
    if (this.enabled == enabled) return;
    this.enabled = enabled;
    support.firePropertyChange("enabled", null, new Boolean(enabled)); //$NON-NLS-1$
  }

  /**
   * Gets whether this filter is enabled.
   *
   * @return <code>true</code> if this is enabled.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Adds a PropertyChangeListener to this filter.
   *
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Adds a PropertyChangeListener to this filter.
   *
   * @param property the name of the property of interest to the listener
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(String property,
                                        PropertyChangeListener listener) {
    support.addPropertyChangeListener(property, listener);
  }

  /**
   * Removes a PropertyChangeListener from this filter.
   *
   * @param listener the listener requesting removal
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener for a specified property.
   *
   * @param property the name of the property
   * @param listener the listener to remove
   */
  public void removePropertyChangeListener(String property,
                                           PropertyChangeListener listener) {
    support.removePropertyChangeListener(property, listener);
  }

  /**
   * Returns a menu with items that control this filter. Subclasses should
   * override this method and add filter-specific menu items.
   *
   * @param video the video using the filter (may be null)
   * @return a menu
   */
  public JMenu getMenu(Video video) {
    JMenu menu = new JMenu(name);
    if (hasInspector) {
      menu.add(propertiesItem);
      menu.addSeparator();
    }
    menu.add(enabledItem);
    if (video != null) {
      menu.addSeparator();
      deleteItem = new JMenuItem(MediaRes.getString("Filter.MenuItem.Delete")); //$NON-NLS-1$
      final FilterStack filterStack = video.getFilterStack();
      deleteItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          filterStack.removeFilter(Filter.this);
        }
      });
      menu.add(deleteItem);
    }
    return menu;
  }

}
