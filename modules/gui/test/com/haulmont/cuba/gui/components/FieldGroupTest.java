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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.client.testsupport.CubaClientTestCase;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.gui.executors.BackgroundWorker;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.User;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.apache.commons.lang.reflect.MethodUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

@Ignore
public class FieldGroupTest extends CubaClientTestCase {

    @Mocked
    protected BackgroundWorker backgroundWorker;

    protected ComponentsFactory componentsFactory;

    protected TestFieldGroupFieldFactoryImpl fieldFactory;

    @SuppressWarnings("ReassignmentInjectVariable")
    @Before
    public void setUp() throws Exception {
        addEntityPackage("com.haulmont.cuba");
        setupInfrastructure();

        fieldFactory = new TestFieldGroupFieldFactoryImpl() {
            {
                this.componentsFactory = FieldGroupTest.this.componentsFactory;
            }
        };

        new NonStrictExpectations() {
            {
                AppBeans.get(BackgroundWorker.NAME); result = backgroundWorker;
                AppBeans.get(BackgroundWorker.class); result = backgroundWorker;
                AppBeans.get(BackgroundWorker.NAME, BackgroundWorker.class); result = backgroundWorker;

                AppBeans.get(FieldGroupFieldFactory.NAME); result = fieldFactory;
                AppBeans.get(FieldGroupFieldFactory.class); result = fieldFactory;
                AppBeans.get(FieldGroupFieldFactory.NAME, FieldGroupFieldFactory.class); result = fieldFactory;
            }
        };

        initExpectations();

        messages.init();

        componentsFactory = createComponentsFactory();
        fieldFactory.setComponentsFactory(componentsFactory);
    }

    protected void initExpectations() {
    }

    protected ComponentsFactory createComponentsFactory() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void newFieldGroup() {
        Component component = componentsFactory.createComponent(FieldGroup.NAME);
        assertTrue(component instanceof FieldGroup);
    }

    @Test
    public void initFields() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);

        FieldGroup.FieldConfig fc = fieldGroup.createField("name");
        fc.setProperty("name");
        fc.setDatasource(createTestDs());

        fieldGroup.addField(fc);
        fieldGroup.bind();
    }

    @Test
    public void initFieldsWithProperties() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);

        Datasource<User> testDs = createTestDs();

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setDatasource(testDs);
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setDatasource(testDs);
        fieldGroup.addField(fcLogin);

        fieldGroup.bind();

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertEquals(fcName, fieldGroup.getField("name"));
        assertEquals(fcLogin, fieldGroup.getField("login"));
    }

    @Test
    public void initWithCustomFields() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setComponent(componentsFactory.createComponent(TextField.NAME));
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setComponent(componentsFactory.createComponent(TextField.NAME));
        fieldGroup.addField(fcLogin);

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));
    }

    @Test
    public void add() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setComponent(componentsFactory.createComponent(TextField.NAME));
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setComponent(componentsFactory.createComponent(TextField.NAME));
        fieldGroup.addField(fcLogin);

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertNotNull(getComposition(fcName));
        assertNotNull(getComposition(fcLogin));

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));

        assertEquals(2, getGridRows(fieldGroup));
        assertEquals(1, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup,0, 0));
        assertEquals(getComposition(fcLogin), getGridCellComposition(fieldGroup,0, 1));
    }

    @Test
    public void addWithColumn() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);
        fieldGroup.setColumns(2);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setComponent(componentsFactory.createComponent(TextField.NAME));
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setComponent(componentsFactory.createComponent(TextField.NAME));
        fieldGroup.addField(fcLogin, 1);

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertNotNull(getComposition(fcName));
        assertNotNull(getComposition(fcLogin));

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));

        assertEquals(1, getGridRows(fieldGroup));
        assertEquals(2, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup,0, 0));
        assertEquals(getComposition(fcLogin), getGridCellComposition(fieldGroup,1, 0));
    }

    @Test
    public void addWithColumnAndRow() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setComponent(componentsFactory.createComponent(TextField.NAME));
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setComponent(componentsFactory.createComponent(TextField.NAME));
        fieldGroup.addField(fcLogin, 0, 0);

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertNotNull(getComposition(fcName));
        assertNotNull(getComposition(fcLogin));

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));

        assertEquals(2, getGridRows(fieldGroup));
        assertEquals(1, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup,0, 1));
        assertEquals(getComposition(fcLogin), getGridCellComposition(fieldGroup,0, 0));
    }

    @Test
    public void removeField() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);

        Datasource<User> testDs = createTestDs();

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setDatasource(testDs);
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setDatasource(testDs);
        fieldGroup.addField(fcLogin);

        fieldGroup.removeField("login");

        assertEquals(fcName, fieldGroup.getField("name"));
        assertEquals(null, fieldGroup.getField("login"));

        fieldGroup.bind();

        assertEquals(1, getGridRows(fieldGroup));
        assertEquals(1, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup,0, 0));
    }

    @Test
    public void removeBoundField() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);

        Datasource<User> testDs = createTestDs();

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setDatasource(testDs);
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setDatasource(testDs);
        fieldGroup.addField(fcLogin);

        fieldGroup.bind();

        fieldGroup.removeField("login");

        assertEquals(fcName, fieldGroup.getField("name"));
        assertEquals(null, fieldGroup.getField("login"));

        assertEquals(1, getGridRows(fieldGroup));
        assertEquals(1, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup,0, 0));
    }

    @Test
    public void addWithSet() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);
        fieldGroup.setColumns(2);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fieldGroup.addField(fcLogin, 1);

        fcName.setComponent(componentsFactory.createComponent(TextField.NAME));
        fcLogin.setComponent(componentsFactory.createComponent(TextField.NAME));

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertNotNull(getComposition(fcName));
        assertNotNull(getComposition(fcLogin));

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));

        assertEquals(1, getGridRows(fieldGroup));
        assertEquals(2, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup, 0, 0));
        assertEquals(getComposition(fcLogin), getGridCellComposition(fieldGroup, 1, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addExistingField() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);
        fieldGroup.setColumns(2);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fieldGroup.addField(fcName);

        fieldGroup.addField(fcName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIncorrectColumn() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);
        fieldGroup.setColumns(2);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fieldGroup.addField(fcName);

        fieldGroup.addField(fcName, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIncorrectRow() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);
        fieldGroup.setColumns(2);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fieldGroup.addField(fcName);

        fieldGroup.addField(fcName, 0, 3);
    }

    @Test(expected = IllegalStateException.class)
    public void changeBoundComponent() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);
        fieldGroup.setColumns(2);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fieldGroup.addField(fcName);

        fieldGroup.bind();

        fcName.setComponent(componentsFactory.createComponent(TextArea.NAME));
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNonDefinedField() {
        FieldGroup fieldGroup = componentsFactory.createComponent(FieldGroup.class);
        fieldGroup.setColumns(2);

        fieldGroup.removeField("none");
    }

    protected Object getComposition(FieldGroup.FieldConfig fc) {
        Method getCompositionMethod = MethodUtils.getAccessibleMethod(fc.getClass(), "getComposition", new Class[]{});
        Object composition;
        try {
            composition = getCompositionMethod.invoke(fc);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Invoke error", e);
        }
        return composition;
    }

    protected int getGridRows(FieldGroup fieldGroup) {
        throw new UnsupportedOperationException();
    }

    protected int getGridColumns(FieldGroup fieldGroup) {
        throw new UnsupportedOperationException();
    }

    protected Object getGridCellComposition(FieldGroup fieldGroup, int col, int row) {
        throw new UnsupportedOperationException();
    }

    protected Datasource<User> createTestDs() {
        //noinspection unchecked
        Datasource<User> testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getView(User.class, View.LOCAL))
                .buildDatasource();

        testDs.setItem(metadata.create(User.class));
        ((DatasourceImpl) testDs).valid();

        return testDs;
    }

    protected static class TestFieldGroupFieldFactoryImpl extends FieldGroupFieldFactoryImpl {
        public void setComponentsFactory(ComponentsFactory componentsFactory) {
            this.componentsFactory = componentsFactory;
        }
    }
}