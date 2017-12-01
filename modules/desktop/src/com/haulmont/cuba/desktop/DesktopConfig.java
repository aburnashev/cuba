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

package com.haulmont.cuba.desktop;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultInt;
import com.haulmont.cuba.core.config.defaults.DefaultString;
import com.haulmont.cuba.core.config.type.Factory;
import com.haulmont.cuba.core.config.type.IntegerListTypeFactory;

import java.util.List;

/**
 * Desktop UI configuration parameters.
 *
 */
@Source(type = SourceType.APP)
public interface DesktopConfig extends Config {

    /**
     * List of root directories where resources (icons, images, theme config) are located.
     * Resources are local to each desktop theme.
     * For each root directory must exist subdirectory with name of current theme.
     *
     * @return list of root resource directories
     */
    @Property("cuba.desktop.resourceLocations")
    @Default("com/haulmont/cuba/desktop/res")
    String getResourceLocations();

    @Property("cuba.desktop.mainTabCaptionLength")
    @DefaultInt(25)
    int getMainTabCaptionLength();

    @Property("cuba.desktop.windowIcon")
    String getWindowIcon();

    /**
     * Desktop theme name.
     * Theme configuration file is located as <code>${resourceLocation}/${themeName}/${themeName}.xml</code>.
     *
     * @return desktop theme name
     */
    @Property("cuba.desktop.theme")
    @Default("nimbus")
    String getTheme();

    @Property("cuba.desktop.dialogNotificationsEnabled")
    @DefaultBoolean(false)
    boolean isDialogNotificationsEnabled();

    @Property("cuba.desktop.availableFontSizes")
    @Factory(factory = IntegerListTypeFactory.class)
    @Default("6 8 10 12 14 16 18 20 22 24 28 32 36 48 54 60 72")
    List<Integer> getAvailableFontSizes();

    /**
     * @return true if application should change time zone to that which is used by server
     */
    @Property("cuba.desktop.useServerTimeZone")
    @DefaultBoolean(true)
    boolean isUseServerTimeZone();

    /**
     * @return true if application should synchronize its time source with server time
     */
    @Property("cuba.desktop.useServerTime")
    @DefaultBoolean(true)
    boolean isUseServerTime();

    /**
     * @return Timeout of loading session messages from server in seconds
     */
    @Property("cuba.desktop.sessionMessagesIntervalSec")
    @DefaultInt(60)
    int getSessionMessagesIntervalSec();

    /**
     * @deprecated Use {@link ClientConfig#getSuggestionFieldAsyncSearchDelayMs()} instead
     */
    @Property("cuba.desktop.searchField.asyncTimeoutMs")
    @DefaultInt(200)
    int getSearchFieldAsyncTimeoutMs();

    /**
     * @return Default user login to set in the login dialog.
     */
    @Property("cuba.desktop.loginDialogDefaultUser")
    @Default("admin")
    String getLoginDialogDefaultUser();

    /**
     * @return Default user password to set in the login dialog.
     */
    @Property("cuba.desktop.loginDialogDefaultPassword")
    @Default("admin")
    String getLoginDialogDefaultPassword();

    @Property("cuba.desktop.showTooltipShortcut")
    @Source(type = SourceType.DATABASE)
    @DefaultString("F1")
    String getShowTooltipShortcut();
}