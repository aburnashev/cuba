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

package com.haulmont.cuba.gui.components;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.chile.core.model.Range;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesMetaProperty;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesUtils;
import com.haulmont.cuba.core.app.dynamicattributes.PropertyType;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.RuntimePropsDatasource;
import com.haulmont.cuba.gui.dynamicattributes.DynamicAttributesGuiTools;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.haulmont.cuba.gui.WindowManager.OpenType;
import static com.haulmont.cuba.gui.components.EntityLinkField.EntityLinkClickHandler;

public abstract class AbstractFieldFactory implements FieldFactory {

    protected ComponentsFactory componentsFactory = AppConfig.getFactory();

    @Override
    public Component createField(Datasource datasource, String property, Element xmlDescriptor) {
        // Step 0. Making preparation
        MetaClass metaClass = datasource.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, property);

        MetaContext context = new MetaContext(metaClass, property, datasource,
                ParamsMap.of("xmlDescriptor", xmlDescriptor));

        // Step 1. Trying to find a custom factory
        Map<String, MetaComponentFactory> factoryMap = AppBeans.getAll(MetaComponentFactory.class);
        factoryMap.remove(MetaComponentFactoryImpl.NAME);

        if (MapUtils.isNotEmpty(factoryMap)) {
            List<MetaComponentFactory> availableFactories = new ArrayList<>(factoryMap.values());

            AnnotationAwareOrderComparator.sort(availableFactories);

            for (MetaComponentFactory factory : availableFactories) {
                Component component = factory.createComponent(context);
                if (component != null) {
                    return component;
                }
            }
        }

        // Step 2. Check if we need to create a specific field
        if (mpp != null) {
            Range mppRange = mpp.getRange();
            if (mppRange.isDatatype()) {
                Class type = mppRange.asDatatype().getJavaClass();

                if (xmlDescriptor != null
                        && "true".equalsIgnoreCase(xmlDescriptor.attributeValue("link"))) {
                    return createDatatypeLinkField(datasource, property, xmlDescriptor);
                } else if (type.equals(String.class)) {
                    if (xmlDescriptor != null
                            && xmlDescriptor.attribute("mask") != null) {
                        return createMaskedField(datasource, property, xmlDescriptor);
                    } else {
                        return createStringField(datasource, property, xmlDescriptor);
                    }
                } else if (type.equals(java.sql.Date.class) || type.equals(Date.class)) {
                    return createDateField(datasource, property, mpp, xmlDescriptor);
                } else if (type.equals(Time.class)) {
                    return createTimeField(datasource, property, xmlDescriptor);
                } else if (Number.class.isAssignableFrom(type)) {
                    if (xmlDescriptor != null
                            && xmlDescriptor.attribute("mask") != null) {
                        MaskedField maskedField = (MaskedField) createMaskedField(datasource, property, xmlDescriptor);
                        maskedField.setValueMode(MaskedField.ValueMode.MASKED);
                        maskedField.setSendNullRepresentation(false);
                        return maskedField;
                    }
                }
            } else if (mppRange.isClass()) {
                MetaProperty metaProperty = mpp.getMetaProperty();
                Class<?> javaType = metaProperty.getJavaType();
                if (!FileDescriptor.class.isAssignableFrom(javaType)
                        && !Collection.class.isAssignableFrom(javaType)) {
                    return createEntityField(datasource, property, mpp, xmlDescriptor);
                }
            }
        }

        // Step 3. Create a default field
        MetaComponentFactory factory = AppBeans.get(MetaComponentFactoryImpl.NAME);
        Component component = factory.createComponent(context);
        if (component != null) {
            return component;
        }

        // Step 4. No component created, throw the exception
        String exceptionMessage;
        if (mpp != null) {
            String name = mpp.getRange().isDatatype()
                    ? mpp.getRange().asDatatype().toString()
                    : mpp.getRange().asClass().getName();
            exceptionMessage = String.format("Can't create field \"%s\" with data type: %s", property, name);
        } else {
            exceptionMessage = String.format("Can't create field \"%s\" with given data type", property);
        }
        throw new UnsupportedOperationException(exceptionMessage);
    }

    protected Component createDatatypeLinkField(Datasource datasource, String property, Element xmlDescriptor) {
        EntityLinkField linkField = componentsFactory.createComponent(EntityLinkField.class);

        linkField.setDatasource(datasource, property);

        if (xmlDescriptor != null) {
            String linkScreen = xmlDescriptor.attributeValue("linkScreen");
            if (StringUtils.isNotEmpty(linkScreen)) {
                linkField.setScreen(linkScreen);
            }

            final String invokeMethodName = xmlDescriptor.attributeValue("linkInvoke");
            if (StringUtils.isNotEmpty(invokeMethodName)) {
                linkField.setCustomClickHandler(new InvokeEntityLinkClickHandler(invokeMethodName));
            }

            String openTypeAttribute = xmlDescriptor.attributeValue("linkScreenOpenType");
            if (StringUtils.isNotEmpty(openTypeAttribute)) {
                OpenType openType = OpenType.valueOf(openTypeAttribute);
                linkField.setScreenOpenType(openType);
            }
        }

        return linkField;
    }

    protected Component createMaskedField(Datasource datasource, String property, Element xmlDescriptor) {
        MaskedField maskedField = componentsFactory.createComponent(MaskedField.class);
        maskedField.setDatasource(datasource, property);
        if (xmlDescriptor != null) {
            maskedField.setMask(xmlDescriptor.attributeValue("mask"));

            String valueModeStr = xmlDescriptor.attributeValue("valueMode");
            if (StringUtils.isNotEmpty(valueModeStr)) {
                maskedField.setValueMode(MaskedField.ValueMode.valueOf(valueModeStr.toUpperCase()));
            }
        }
        return maskedField;
    }

    protected Component createStringField(Datasource datasource, String property, Element xmlDescriptor) {
        TextInputField textField = null;

        if (xmlDescriptor != null) {
            final String rows = xmlDescriptor.attributeValue("rows");
            if (!StringUtils.isEmpty(rows)) {
                TextArea textArea = componentsFactory.createComponent(TextArea.class);
                textArea.setRows(Integer.parseInt(rows));
                textField = textArea;
            }
        }
        if (DynamicAttributesUtils.isDynamicAttribute(property)) {
            MetaClass metaClass = datasource instanceof RuntimePropsDatasource ?
                    ((RuntimePropsDatasource) datasource).resolveCategorizedEntityClass() : datasource.getMetaClass();
            MetaPropertyPath mpp = DynamicAttributesUtils.getMetaPropertyPath(metaClass, property);
            if (mpp != null) {
                CategoryAttribute categoryAttribute = DynamicAttributesUtils.getCategoryAttribute(mpp.getMetaProperty());
                if (categoryAttribute != null && categoryAttribute.getDataType() == PropertyType.STRING
                        && categoryAttribute.getRowsCount() != null && categoryAttribute.getRowsCount() > 1) {
                    TextArea textArea = componentsFactory.createComponent(TextArea.class);
                    textArea.setRows(categoryAttribute.getRowsCount());
                    textField = textArea;
                }
            }
        }

        if (textField == null) {
            textField = componentsFactory.createComponent(TextField.class);
        }

        textField.setDatasource(datasource, property);

        String maxLength = xmlDescriptor != null ? xmlDescriptor.attributeValue("maxLength") : null;
        if (StringUtils.isNotEmpty(maxLength)) {
            ((TextInputField.MaxLengthLimited) textField).setMaxLength(Integer.parseInt(maxLength));
        }

        return textField;
    }

    protected Component createDateField(Datasource datasource, String property, MetaPropertyPath mpp,
                                        Element xmlDescriptor) {
        DateField dateField = componentsFactory.createComponent(DateField.class);
        dateField.setDatasource(datasource, property);

        final String resolution = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("resolution");
        String dateFormat = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("dateFormat");

        DateField.Resolution dateResolution = DateField.Resolution.MIN;

        if (StringUtils.isNotEmpty(resolution)) {
            dateResolution = DateField.Resolution.valueOf(resolution);
            dateField.setResolution(dateResolution);
        }

        if (dateFormat == null) {
            if (dateResolution == DateField.Resolution.DAY) {
                dateFormat = "msg://dateFormat";
            } else if (dateResolution == DateField.Resolution.MIN) {
                dateFormat = "msg://dateTimeFormat";
            }
        }
        Messages messages = AppBeans.get(Messages.NAME);

        if (StringUtils.isNotEmpty(dateFormat)) {
            if (dateFormat.startsWith("msg://")) {
                dateFormat = messages.getMainMessage(dateFormat.substring(6, dateFormat.length()));
            }
            dateField.setDateFormat(dateFormat);
        }

        return dateField;
    }

    protected Component createTimeField(Datasource datasource, String property, Element xmlDescriptor) {
        TimeField timeField = componentsFactory.createComponent(TimeField.class);
        timeField.setDatasource(datasource, property);

        if (xmlDescriptor != null) {
            String showSeconds = xmlDescriptor.attributeValue("showSeconds");
            if (Boolean.parseBoolean(showSeconds)) {
                timeField.setShowSeconds(true);
            }
        }
        return timeField;
    }

    protected Component createEntityField(Datasource datasource, String property, MetaPropertyPath mpp, Element xmlDescriptor) {
        String linkAttribute = null;
        if (xmlDescriptor != null) {
            linkAttribute = xmlDescriptor.attributeValue("link");
        }

        if (!Boolean.parseBoolean(linkAttribute)) {
            CollectionDatasource optionsDatasource = getOptionsDatasource(datasource, property);

            if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty())) {
                DynamicAttributesMetaProperty metaProperty = (DynamicAttributesMetaProperty) mpp.getMetaProperty();
                CategoryAttribute attribute = metaProperty.getAttribute();
                if (Boolean.TRUE.equals(attribute.getLookup())) {
                    DynamicAttributesGuiTools dynamicAttributesGuiTools = AppBeans.get(DynamicAttributesGuiTools.class);
                    optionsDatasource = dynamicAttributesGuiTools.createOptionsDatasourceForLookup(metaProperty.getRange().asClass(),
                            attribute.getJoinClause(), attribute.getWhereClause());
                }
            }

            PickerField pickerField;
            if (optionsDatasource == null) {
                pickerField = componentsFactory.createComponent(PickerField.class);
                pickerField.setDatasource(datasource, property);
                if (mpp.getMetaProperty().getType() == MetaProperty.Type.ASSOCIATION) {
                    pickerField.addLookupAction();
                    if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty())) {
                        DynamicAttributesGuiTools dynamicAttributesGuiTools = AppBeans.get(DynamicAttributesGuiTools.class);
                        DynamicAttributesMetaProperty dynamicAttributesMetaProperty = (DynamicAttributesMetaProperty) mpp.getMetaProperty();
                        dynamicAttributesGuiTools.initEntityPickerField(pickerField, dynamicAttributesMetaProperty.getAttribute());
                    }
                    boolean actionsByMetaAnnotations = ComponentsHelper.createActionsByMetaAnnotations(pickerField);
                    if (!actionsByMetaAnnotations) {
                        pickerField.addClearAction();
                    }
                } else {
                    pickerField.addOpenAction();
                    pickerField.addClearAction();
                }
            } else {
                LookupPickerField lookupPickerField = componentsFactory.createComponent(LookupPickerField.class);
                lookupPickerField.setDatasource(datasource, property);
                lookupPickerField.setOptionsDatasource(optionsDatasource);

                pickerField = lookupPickerField;

                ComponentsHelper.createActionsByMetaAnnotations(pickerField);
            }

            if (xmlDescriptor != null) {
                String captionProperty = xmlDescriptor.attributeValue("captionProperty");
                if (StringUtils.isNotEmpty(captionProperty)) {
                    pickerField.setCaptionMode(CaptionMode.PROPERTY);
                    pickerField.setCaptionProperty(captionProperty);
                }
            }

            return pickerField;
        } else {
            EntityLinkField linkField = componentsFactory.createComponent(EntityLinkField.class);

            linkField.setDatasource(datasource, property);

            if (xmlDescriptor != null) {
                String linkScreen = xmlDescriptor.attributeValue("linkScreen");
                if (StringUtils.isNotEmpty(linkScreen)) {
                    linkField.setScreen(linkScreen);
                }

                final String invokeMethodName = xmlDescriptor.attributeValue("linkInvoke");
                if (StringUtils.isNotEmpty(invokeMethodName)) {
                    linkField.setCustomClickHandler(new InvokeEntityLinkClickHandler(invokeMethodName));
                }

                String openTypeAttribute = xmlDescriptor.attributeValue("linkScreenOpenType");
                if (StringUtils.isNotEmpty(openTypeAttribute)) {
                    OpenType openType = OpenType.valueOf(openTypeAttribute);
                    linkField.setScreenOpenType(openType);
                }
            }

            return linkField;
        }
    }

    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        MetaPropertyPath mpp = metaClass.getPropertyPath(property);

        if (mpp == null && DynamicAttributesUtils.isDynamicAttribute(property)) {
            mpp = DynamicAttributesUtils.getMetaPropertyPath(metaClass, property);
        }

        return mpp;
    }

    @Nullable
    protected abstract CollectionDatasource getOptionsDatasource(Datasource datasource, String property);

    protected static class InvokeEntityLinkClickHandler implements EntityLinkClickHandler {
        protected final String invokeMethodName;

        public InvokeEntityLinkClickHandler(String invokeMethodName) {
            this.invokeMethodName = invokeMethodName;
        }

        @Override
        public void onClick(EntityLinkField field) {
            Window frame = ComponentsHelper.getWindow(field);
            if (frame == null) {
                throw new IllegalStateException("Please specify Frame for EntityLinkField");
            }

            Object controller = ComponentsHelper.getFrameController(frame);
            Method method;
            try {
                method = controller.getClass().getMethod(invokeMethodName, EntityLinkField.class);
                try {
                    method.invoke(controller, field);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (NoSuchMethodException e) {
                try {
                    method = controller.getClass().getMethod(invokeMethodName);
                    try {
                        method.invoke(controller);
                    } catch (Exception e1) {
                        throw new RuntimeException(e1);
                    }
                } catch (NoSuchMethodException e1) {
                    throw new IllegalStateException(String.format("No suitable methods named %s for invoke", invokeMethodName));
                }
            }
        }
    }
}