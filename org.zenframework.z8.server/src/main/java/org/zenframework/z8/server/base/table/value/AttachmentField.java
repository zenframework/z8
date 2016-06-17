package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.base.file.AttachmentProcessor;
import org.zenframework.z8.server.runtime.IObject;

public class AttachmentField extends TextField {
    public static class CLASS<T extends AttachmentField> extends TextField.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(AttachmentField.class);
            setAttribute("native", AttachmentField.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new AttachmentField(container);
        }
    }

    public AttachmentField(IObject container) {
        super(container);
        system.set(true);
    }

    public AttachmentProcessor getAttachmentProcessor() {
        return new AttachmentProcessor(this);
    }

    public AttachmentProcessor.CLASS<? extends AttachmentProcessor> z8_getAttachmentProcessor() {
        AttachmentProcessor.CLASS<AttachmentProcessor> processor = new AttachmentProcessor.CLASS<AttachmentProcessor>();
        processor.get().set(this);
        return processor;
    }
    
}
