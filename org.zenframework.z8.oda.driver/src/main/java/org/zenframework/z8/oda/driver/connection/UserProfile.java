package org.zenframework.z8.oda.driver.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.OdaException;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.security.Component;
import org.zenframework.z8.server.security.IUser;

public class UserProfile {
    private IUser user;

    @SuppressWarnings("rawtypes")
    private List<Desktop.CLASS> desktops = null;

    protected UserProfile(IUser user) throws OdaException {
        this.user = user;

        init();
    }

    @SuppressWarnings({ "rawtypes" })
    protected void init() throws OdaException {
        desktops = new ArrayList<Desktop.CLASS>();

        if(user != null) {
            Component[] list = user.components();

            for(Component comp : list) {
                CLASS cls = Runtime.instance().getEntry(comp.className());

                if(cls != null && cls.instanceOf(Desktop.class)) {
                    desktops.add((Desktop.CLASS)cls);
                }
            }
        }
    }

    public String getLogin() {
        return user.name();
    }

    public String getPassword() {
        return user.password();
    }

    @SuppressWarnings({ "rawtypes" })
    public Desktop.CLASS[] getDesktops() {
        Desktop.CLASS[] desktops = this.desktops.toArray(new Desktop.CLASS[0]);

        Comparator<Desktop.CLASS> comparator = new Comparator<Desktop.CLASS>() {

            @Override
            public int compare(Desktop.CLASS o1, Desktop.CLASS o2) {
                return o1.displayName().compareTo(o2.displayName());
            }

        };

        Arrays.sort(desktops, comparator);
        return desktops;
    }

}
