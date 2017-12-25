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

package com.haulmont.cuba.desktop.gui.components;

import com.haulmont.cuba.gui.components.Component;

/**
 *
 * Desktop components require to know their container.
 *
 * When its size or alignment changes, component asks container to update him.
 *
 */
public interface DesktopComponent extends Component {
    void setContainer(DesktopContainer container);

    void setExpanded(boolean expanded);

    /**
     * A sub-interface implemented by components that provide additional
     * capabilities for components that have context help icon click handler.
     */
    interface HasContextHelpClickHandler extends HasContextHelp {
        /**
         * Fires a {@link Component.ContextHelpIconClickEvent} for all listeners.
         *
         * @param event event to be fired
         */
        void fireContextHelpIconClickEvent(Component.ContextHelpIconClickEvent event);
    }
}
