/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.core.sys.servlet;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.Servlet;

@Component(ServletDynamicRegistration.NAME)
public class ServletDynamicRegistrationBean implements ServletDynamicRegistration {

    @Override
    public Servlet createServlet(ApplicationContext context, String servletClass) {
        Class<? extends Servlet> clazz;

        try {
            //noinspection unchecked
            clazz = (Class<? extends Servlet>) context.getClassLoader()
                    .loadClass(servletClass);
        } catch (ClassNotFoundException e) {
            String msg = String.format("Failed to load servlet class: %s", servletClass);
            throw new RuntimeException(msg, e);
        }

        Servlet servlet;
        try {
            servlet = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            String msg = String.format("Failed to get an instance of a class: %s", servletClass);
            throw new RuntimeException(msg, e);
        }

        return servlet;
    }

    @Override
    public Filter createFilter(ApplicationContext context, String filterClass) {
        Class<? extends Filter> clazz;

        try {
            //noinspection unchecked
            clazz = (Class<? extends Filter>) context.getClassLoader()
                    .loadClass(filterClass);
        } catch (ClassNotFoundException e) {
            String msg = String.format("Failed to load filter class: %s", filterClass);
            throw new RuntimeException(msg, e);
        }

        Filter servlet;
        try {
            servlet = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            String msg = String.format("Failed to get an instance of a class: %s", filterClass);
            throw new RuntimeException(msg, e);
        }

        return servlet;
    }
}
