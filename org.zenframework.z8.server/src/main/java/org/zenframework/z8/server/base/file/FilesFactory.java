package org.zenframework.z8.server.base.file;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.runtime.ServerRuntime;

public class FilesFactory {

    private static class FilesListener implements Properties.Listener {
        @Override
        public void onPropertyChange(String key, String value) {
            if (ServerRuntime.FileItemSizeThresholdProperty.equalsKey(key))
                FilesFactory.fileItemFactory.setSizeThreshold(Integer.parseInt(value));
        }
        
    }
    
    static {
        Properties.addListener(new FilesListener());
    }

    private static DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
    
    public static FileItemFactory getFileItemFactory() {
        return fileItemFactory;
    }
    
    public static FileItem createFileItem(String fileName) {
        return getFileItemFactory().createItem(null, null, false, fileName);
    }

    private FilesFactory() {/**/}

}
