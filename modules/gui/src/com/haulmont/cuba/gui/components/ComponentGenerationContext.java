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

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import org.dom4j.Element;

import javax.annotation.Nullable;

/**
 * A class that stores information that can be used to create
 * a component by the {@link com.haulmont.cuba.gui.xml.layout.ComponentsFactory}.
 */
public class ComponentGenerationContext {
    protected MetaClass metaClass;
    protected String property;
    protected Datasource datasource;
    protected CollectionDatasource optionsDatasource;
    protected Element xmlDescriptor;
    protected Class componentClass;

    /**
     * Creates an instance of MetaContext.
     *
     * @param metaClass an instance of {@link MetaClass} for which a component should be created
     * @param property  a property of meta class for which a component should be created
     */
    public ComponentGenerationContext(MetaClass metaClass, String property) {
        this.metaClass = metaClass;
        this.property = property;
    }

    /**
     * @return an instance of {@link MetaClass} for which a component should be created
     */
    public MetaClass getMetaClass() {
        return metaClass;
    }

    /**
     * Sets an instance of {@link MetaClass} for which a component should be created, using fluent API method.
     *
     * @param metaClass an instance of {@link MetaClass} for which a component should be created
     * @return this object
     */
    public ComponentGenerationContext setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
        return this;
    }

    /**
     * @return a property of meta class for which a component should be created
     */
    public String getProperty() {
        return property;
    }

    /**
     * Sets a property of meta class for which a component should be created, using fluent API method.
     *
     * @param property a property of meta class for which a component should be created
     * @return this object
     */
    public ComponentGenerationContext setProperty(String property) {
        this.property = property;
        return this;
    }

    /**
     * @return a datasource that can be used to create the component
     */
    @Nullable
    public Datasource getDatasource() {
        return datasource;
    }

    /**
     * Sets a datasource that can be used to create the component, using fluent API method.
     *
     * @param datasource a datasource that can be used to create the component
     * @return this object
     */
    public ComponentGenerationContext setDatasource(Datasource datasource) {
        this.datasource = datasource;
        return this;
    }

    /**
     * @return a datasource that can be used as optional to create the component
     */
    @Nullable
    public CollectionDatasource getOptionsDatasource() {
        return optionsDatasource;
    }

    /**
     * Sets a datasource that can be used as optional to create the component, using fluent API method.
     *
     * @param optionsDatasource a datasource that can be used as optional to create the component
     * @return this object
     */
    public ComponentGenerationContext setOptionsDatasource(CollectionDatasource optionsDatasource) {
        this.optionsDatasource = optionsDatasource;
        return this;
    }

    /**
     * @return an XML descriptor which contains additional information
     */
    @Nullable
    public Element getXmlDescriptor() {
        return xmlDescriptor;
    }

    /**
     * Sets an XML descriptor which contains additional information, using fluent API method.
     *
     * @param xmlDescriptor an XML descriptor which contains additional information
     * @return this object
     */
    public ComponentGenerationContext setXmlDescriptor(Element xmlDescriptor) {
        this.xmlDescriptor = xmlDescriptor;
        return this;
    }

    /**
     * @return a component class for which a component should be created
     */
    @Nullable
    public Class getComponentClass() {
        return componentClass;
    }

    /**
     * Sets a component class for which a component should be created, using fluent API method.
     *
     * @param componentClass a component class for which a component should be created
     * @return this object
     */
    public ComponentGenerationContext setComponentClass(Class componentClass) {
        this.componentClass = componentClass;
        return this;
    }
}
