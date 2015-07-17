package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class TextChangeCompatibility {
    public static void addTextEdit(TextChange change, String name, TextEdit edit) {
        TextEdit root = change.getEdit();

        if(root == null) {
            root = new MultiTextEdit();
            change.setEdit(root);
        }

        insert(root, edit);
        change.addTextEditGroup(new TextEditGroup(name, edit));
    }

    public static void addTextEdit(TextChange change, String name, TextEdit[] edits) {
        TextEdit root = change.getEdit();

        if(root == null) {
            root = new MultiTextEdit();
            change.setEdit(root);
        }

        for(int i = 0; i < edits.length; i++) {
            insert(root, edits[i]);
        }
        change.addTextEditGroup(new TextEditGroup(name, edits));
    }

    public static void insert(TextEdit parent, TextEdit edit) {
        if(!parent.hasChildren()) {
            parent.addChild(edit);
            return;
        }

        TextEdit[] children = parent.getChildren();

        for(int i = 0; i < children.length; i++) {
            TextEdit child = children[i];
            if(covers(child, edit)) {
                insert(child, edit);
                return;
            }
        }

        for(int i = children.length - 1; i >= 0; i--) {
            TextEdit child = children[i];
            if(covers(edit, child)) {
                parent.removeChild(i);
                edit.addChild(child);
            }
        }

        parent.addChild(edit);
    }

    private static boolean covers(TextEdit thisEdit, TextEdit otherEdit) {
        if(thisEdit.getLength() == 0)
            return false;

        int thisOffset = thisEdit.getOffset();
        int thisEnd = thisEdit.getExclusiveEnd();

        if(otherEdit.getLength() == 0) {
            int otherOffset = otherEdit.getOffset();
            return thisOffset < otherOffset && otherOffset < thisEnd;
        }
        else {
            int otherOffset = otherEdit.getOffset();
            int otherEnd = otherEdit.getExclusiveEnd();
            return thisOffset <= otherOffset && otherEnd <= thisEnd;
        }
    }
}
