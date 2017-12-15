/*
 * Copyright (c) 2008-2017 Haulmont.
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
 */

package com.haulmont.cuba.desktop.test.ui;

import com.google.common.collect.ImmutableMap;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.desktop.gui.DesktopComponentsFactory;
import com.haulmont.cuba.desktop.gui.components.DesktopFieldGroup;
import com.haulmont.cuba.desktop.gui.executors.impl.DesktopBackgroundWorker;
import com.haulmont.cuba.gui.components.ComponentGenerationStrategy;
import com.haulmont.cuba.gui.components.DefaultComponentGenerationStrategy;
import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.components.FieldGroupTest;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DesktopFieldGroupTest extends FieldGroupTest {
    @Mocked({"checkSwingUIAccess"})
    protected DesktopBackgroundWorker desktopBackgroundWorker;

    @Override
    protected void initExpectations() {
        super.initExpectations();

        new NonStrictExpectations() {
            {
                globalConfig.getAvailableLocales(); result = ImmutableMap.of("en", Locale.ENGLISH);
                AppContext.getProperty("cuba.mainMessagePack"); result = "com.haulmont.cuba.desktop";
            }
        };
    }

    @Override
    protected ComponentsFactory createComponentsFactory() {
        return new DesktopComponentsFactory() {
            @Override
            public List<ComponentGenerationStrategy> getComponentGenerationStrategies() {
                return Collections.singletonList(new DefaultComponentGenerationStrategy(messages, this));
            }
        };
    }

    @Override
    protected int getGridRows(FieldGroup fieldGroup) {
        return ((DesktopFieldGroup) fieldGroup).getRows();
    }

    @Override
    protected int getGridColumns(FieldGroup fieldGroup) {
        return fieldGroup.getColumns();
    }

    @Override
    protected Object getGridCellComposition(FieldGroup fieldGroup, int col, int row) {
        return ((DesktopFieldGroup) fieldGroup).getCellComponent(col, row);
    }
}