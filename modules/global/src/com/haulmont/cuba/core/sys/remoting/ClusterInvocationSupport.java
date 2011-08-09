/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.core.sys.remoting;

import com.haulmont.cuba.core.sys.AppContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>$Id$</p>
 *
 * @author krivopustov
 */
public class ClusterInvocationSupport {

    public interface Listener {
        void urlListChanged(List<String> newUrlList);
    }

    private Log log = LogFactory.getLog(getClass());

    private List<String> urls;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    protected String baseUrl = AppContext.getProperty("cuba.connectionUrl");
    protected String servletPath = "remoting";

    private List<Listener> listeners = new ArrayList<Listener>();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public void init() {
        urls = new ArrayList<String>();
        String[] strings = baseUrl.split("[,;]");
        for (String string : strings) {
            if (!StringUtils.isBlank(string)) {
                urls.add(string + "/" + servletPath);
            }
        }
    }

    public List<String> getUrlList() {
        return urls;
    }

    public List<String> getUrlList(String serviceName) {
        lock.readLock().lock();
        try {
            List<String> list = new ArrayList<String>(urls.size());
            for (String url : urls) {
                list.add(url + "/" + serviceName);
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }

    public synchronized void updateUrlPriority(String successfulUrl) {
        List<String> newList = new ArrayList<String>();
        String url = successfulUrl.substring(0, successfulUrl.lastIndexOf("/"));
        newList.add(url);
        lock.writeLock().lock();
        try {
            for (String u : urls) {
                if (!u.equals(url)) {
                    newList.add(u);
                }
            }
            log.debug("Connection URL priority changed: " + urls + " -> " + newList);
            urls = newList;
            for (Listener listener : listeners) {
                listener.urlListChanged(Collections.unmodifiableList(urls));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addListener(Listener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
}
