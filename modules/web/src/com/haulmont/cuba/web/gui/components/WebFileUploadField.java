/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Dmitry Abramov
 * Created: 11.03.2009 17:48:51
 * $Id$
 */
package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.web.toolkit.ui.Upload;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class WebFileUploadField
    extends
        WebAbstractComponent<Upload>
    implements
        FileUploadField
{
    protected String fileName;
    protected byte[] bytes;
    protected ByteArrayOutputStream outputStream;
    private List<Listener> listeners = new ArrayList<Listener>();

    public WebFileUploadField() {
        String caption = MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), "Upload");
        component = new Upload(caption, new Upload.Receiver() {
            public OutputStream receiveUpload(String filename, String MIMEType) {
                fileName = filename;
                outputStream = new ByteArrayOutputStream();
                return outputStream;
            }
        });
        component.setAction("");

        component.addListener(new Upload.StartedListener() {
            public void uploadStarted(Upload.StartedEvent event) {
                bytes = null;
                final Listener.Event e = new Listener.Event(event.getFilename());
                for (Listener listener : listeners) {
                    listener.uploadStarted(e);
                }
            }
        });
        component.addListener(new Upload.FinishedListener() {
            public void uploadFinished(Upload.FinishedEvent event) {
                bytes = outputStream.toByteArray();
                final Listener.Event e = new Listener.Event(event.getFilename());
                for (Listener listener : listeners) {
                    listener.uploadFinished(e);
                }
            }
        });
        component.addListener(new Upload.SucceededListener() {
            public void uploadSucceeded(Upload.SucceededEvent event) {
                final Listener.Event e = new Listener.Event(event.getFilename());
                for (Listener listener : listeners) {
                    listener.uploadSucceeded(e);
                }
            }
        });
        component.addListener(new Upload.FailedListener() {
            public void uploadFailed(Upload.FailedEvent event) {
                final Listener.Event e = new Listener.Event(event.getFilename());
                for (Listener listener : listeners) {
                    listener.uploadFailed(e);
                }
            }
        });
        component.addListener(new Upload.ProgressListener() {
            public void updateProgress(long readBytes, long contentLength) {
                for (Listener listener : listeners) {
                    listener.updateProgress(readBytes, contentLength);
                }
            }
        });
        component.setButtonCaption(MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(),
                "upload.submit"));
    }

/*
    public <T> T getComponent() {
        return (T) component;
    }

*/
    public String getFilePath() {
        return fileName;
    }

    public String getFileName() {
        String[] strings = fileName.split("[/\\\\]");
        return strings[strings.length-1];
    }

    public boolean isUploading() {
        return component.isUploading();
    }

    public long getBytesRead() {
        return component.getBytesRead();
    }

    public void release() {
        outputStream = null;
        bytes = null;
    }

    public void addListener(Listener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getCaption() {
        return component.getCaption();
    }

    public void setCaption(String caption) {
        component.setCaption(caption);
    }

    public String getDescription() {
        return component.getDescription();
    }

    public void setDescription(String description) {
        component.setDescription(description);
    }
}
