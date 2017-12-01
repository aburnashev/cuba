/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.cuba.desktop.sys;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.desktop.DesktopConfig;
import com.haulmont.cuba.desktop.gui.components.DesktopComponentsHelper;
import com.haulmont.cuba.desktop.sys.vcl.ToolTipButton;
import com.haulmont.cuba.gui.components.KeyCombination;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;

/**
 * Class that encapsulates displaying of tooltips for all components.
 */
public class DesktopToolTipManager extends MouseAdapter {

    protected static int CLOSE_TIME = 500;
    protected static int SHOW_TIME = 1000;

    protected boolean tooltipShowing = false;

    protected JToolTip toolTipWindow;
    protected Popup window;
    protected JComponent component;

    protected Timer showTimer = new Timer(SHOW_TIME, null);
    protected Timer closeTimer;

    protected Configuration configuration = AppBeans.get(Configuration.NAME);

    protected MouseListener componentMouseListener = new ComponentMouseListener();
    protected KeyListener fieldKeyListener = new FieldKeyListener();
    protected ActionListener btnActionListener = new ButtonClickListener();

    private static DesktopToolTipManager instance;

    /**
     * Return singleton instance of DesktopToolTipManager for application.
     *
     * @return instance of DesktopToolTipManager
     */
    public static DesktopToolTipManager getInstance() {
        if (instance == null) {
            instance = new DesktopToolTipManager();
        }
        return instance;
    }

    protected DesktopToolTipManager() {
        closeTimer = new Timer(CLOSE_TIME, null);
        closeTimer.setRepeats(false);
        closeTimer.addActionListener(e -> {
            window.hide();
            window = null;
            tooltipShowing = false;
            toolTipWindow.removeMouseListener(DesktopToolTipManager.this);
            component.removeMouseListener(DesktopToolTipManager.this);

        });

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            protected MouseEvent event;

            @Override
            public void eventDispatched(AWTEvent e) {
                if (!tooltipShowing) {
                    return;
                }
                event = (MouseEvent) e;
                if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                    if (event.getComponent() != null && event.getComponent().isShowing()) {
                        if (!isPointInComponent(event.getLocationOnScreen(), toolTipWindow))
                            hideTooltip();
                    } else
                        hideTooltip();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    protected boolean isPointInComponent(Point point, JComponent component) {
        if (!component.isShowing())
            return false;

        Point componentLocation = component.getLocationOnScreen();
        Rectangle bounds = component.getBounds();
        return (((point.x >= componentLocation.x) && (point.x <= componentLocation.x + bounds.width)) &&
                (point.y >= componentLocation.y) && (point.y <= componentLocation.y + bounds.height));
    }

    /**
     * Register tooltip for component.
     * The tooltip is displayed when a user either presses F1 on a focused component or hovers over it.
     * ToolTip text is taken from {@link javax.swing.JComponent#getToolTipText()}.
     *
     * @param component component to register
     */
    public void registerTooltip(final JComponent component) {
        component.removeKeyListener(fieldKeyListener);
        component.addKeyListener(fieldKeyListener);

        component.removeMouseListener(componentMouseListener);
        component.addMouseListener(componentMouseListener);
    }

    /**
     * Register tooltip for {@link javax.swing.AbstractButton}.
     * Tooltip is displayed when the user hovers over a button
     * ToolTip text is taken from {@link javax.swing.JComponent#getToolTipText()}.
     *
     * @param btn Button to register
     */
    public void registerTooltip(final AbstractButton btn) {
        btn.removeMouseListener(componentMouseListener);
        btn.addMouseListener(componentMouseListener);
    }

    /**
     * Register tooltip for {@link javax.swing.JLabel}.
     * Tooltip is displayed when the user hovers over a label
     * ToolTip text is taken from {@link javax.swing.JComponent#getToolTipText()}.
     *
     * @param lbl Label to register
     */
    public void registerTooltip(final JLabel lbl) {
        lbl.removeMouseListener(componentMouseListener);
        lbl.addMouseListener(componentMouseListener);
    }

    /**
     * Register tooltip for ToolTipButton.
     * Tooltip is displayed when the user presses the button
     * ToolTip text is taken from {@link javax.swing.JComponent#getToolTipText()} .
     *
     * @param btn Button to register
     */
    public void registerTooltip(final ToolTipButton btn) {
        btn.removeActionListener(btnActionListener);
        btn.addActionListener(btnActionListener);
    }

    protected void hideTooltip() {
        closeTimer.stop();
        if (window != null) {
            window.hide();
            window = null;
            tooltipShowing = false;
            toolTipWindow.removeMouseListener(DesktopToolTipManager.this);
            component.removeMouseListener(DesktopToolTipManager.this);
        }
    }

    protected void showTooltip(JComponent field, String text) {
        if (!field.isShowing())
            return;

        if (StringUtils.isEmpty(text)) {
            return;
        }

        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return;
        }

        Point mouseLocation = pointerInfo.getLocation();
        Point location = new Point();

        GraphicsConfiguration gc;
        gc = field.getGraphicsConfiguration();
        Rectangle sBounds = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit()
                .getScreenInsets(gc);

        sBounds.x += screenInsets.left;
        sBounds.y += screenInsets.top;
        sBounds.width -= (screenInsets.left + screenInsets.right);
        sBounds.height -= (screenInsets.top + screenInsets.bottom);

        location.x = mouseLocation.x + 15;
        location.y = mouseLocation.y + 15;
        int x = location.x;
        int y = location.y;

        if (toolTipWindow != null) {
            hideTooltip();
        }

        component = field;

        final JToolTip toolTip = new CubaToolTip();
        toolTip.setTipText(text);
        final Popup tooltipContainer = PopupFactory.getSharedInstance().getPopup(field, toolTip, x, y);
        tooltipContainer.show();
        window = tooltipContainer;
        toolTipWindow = toolTip;

        tooltipShowing = true;
        if (!(field instanceof ToolTipButton)) {
            toolTip.addMouseListener(this);
            field.addMouseListener(this);
        }
    }

    protected class CubaToolTip extends JToolTip {
        @Override
        public void setTipText(String tipText) {
            UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();
            int maxTooltipWidth = lafDefaults.getInt("Tooltip.maxWidth");
            if (maxTooltipWidth == 0) {
                maxTooltipWidth = DesktopComponentsHelper.TOOLTIP_WIDTH;
            }

            FontMetrics fontMetrics = StyleContext.getDefaultStyleContext().getFontMetrics(getFont());
            int actualWidth = SwingUtilities.computeStringWidth(fontMetrics, tipText);

            if (actualWidth < maxTooltipWidth) {
                tipText = "<html>" + tipText + "</html>";
            } else {
                tipText = "<html><body width=\"" + maxTooltipWidth + "px\">" + tipText + "</body></html>";
            }

            super.setTipText(tipText);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        closeTimer.start();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (closeTimer.isRunning()) {
            closeTimer.stop();
        }
    }

    protected class ComponentMouseListener extends MouseAdapter {

        protected JComponent cmp;

        {
            showTimer.setRepeats(false);
            showTimer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!tooltipShowing)
                        showTooltip(cmp, cmp.getToolTipText());
                }
            });
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (window != null) {
                if (e.getSource() != component && e.getSource() != toolTipWindow) {
                    hideTooltip();
                    cmp = (JComponent) e.getSource();
                    showTimer.start();
                    return;
                }
            }
            if (!tooltipShowing) {
                cmp = (JComponent) e.getSource();
                showTimer.start();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!tooltipShowing) {
                if (showTimer.isRunning()) {
                    showTimer.stop();
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            showTimer.stop();
            hideTooltip();
        }
    }

    protected class FieldKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            String showTooltipShortcut = configuration.getConfig(DesktopConfig.class).getShowTooltipShortcut();
            KeyStroke keyStroke = DesktopComponentsHelper
                    .convertKeyCombination(KeyCombination.create(showTooltipShortcut));

            if (KeyStroke.getKeyStrokeForEvent(e).equals(keyStroke)) {
                hideTooltip();
                JComponent field = (JComponent) e.getSource();
                showTooltip(field, field.getToolTipText());
            } else {
                if (tooltipShowing) {
                    hideTooltip();
                }
            }
        }
    }

    protected class ButtonClickListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (component == e.getSource() && tooltipShowing) {
                return;
            }
            if (tooltipShowing) {
                hideTooltip();
            }
            showTooltip((JComponent) e.getSource(), ((JButton) e.getSource()).getToolTipText());
        }
    }
}
