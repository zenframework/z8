package org.zenframework.z8.server.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.string.Lower;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

public class User implements IUser {
    private static final long serialVersionUID = -4955893424674255525L;

    private guid id;

    private String name;
    private String password;
    SecurityGroup securityGroup = SecurityGroup.Users;

    private String description;
    private String phone;
    private String email;
    private boolean blocked;

    private String settings;

    private List<Component> components = new ArrayList<Component>();
    private List<guid> companies = new ArrayList<guid>();
    private Map<String, IForm> forms = new HashMap<String, IForm>();

    private RLinkedHashMap<string, primary> parameters = new RLinkedHashMap<string, primary>();

    static public IUser system() {
        guid id = BuiltinUsers.System.guid();
        String login = Resources.get("BuiltinUsers.System.name");
        String description = Resources.get("BuiltinUsers.System.description");
        String password = "";
        boolean blocked = false;

        User user = new User();

        user.id = id;
        user.name = login;
        user.password = password;
        user.description = description;
        user.blocked = blocked;

        user.settings = "";
        user.phone = "";
        user.email = "";
        user.securityGroup = SecurityGroup.Administrators;

        user.setComponents(new Component[] { new Component(null, SystemTools.class.getCanonicalName(), Resources
                .get(SystemTools.strings.Title)) });

        return user;
    }

    public User() {}

    @Override
    public guid id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public SecurityGroup securityGroup() {
        return securityGroup;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String phone() {
        return phone;
    }

    @Override
    public String email() {
        return email;
    }

    @Override
    public boolean blocked() {
        return blocked;
    }

    @Override
    public Component[] components() {
        if (forms.isEmpty()) {
            return components.toArray(new Component[0]);
        } else {
            Collection<Component> components = new ArrayList<Component>();

            for (IForm form : forms.values()) {
                if (form.getOwnerId().isEmpty() && form.getAccess().getRead()) {
                    components.add(new Component(null, form.getId(), form.getName()));
                }
            }

            return components.toArray(new Component[0]);
        }
    }

    public guid[] companies() {
        return companies.toArray(new guid[0]);
    }

    @Override
    public Map<String, IForm> forms() {
        return forms;
    }

    @Override
    public Map<string, primary> parameters() {
        return parameters;
    }

    @Override
    public void setComponents(Component[] components) {
        this.components.clear();
        this.components.addAll(Arrays.asList(components));
    }

    public void setCompanies(guid[] companies) {
        this.companies.clear();
        this.companies.addAll(Arrays.asList(companies));
    }

    static public IUser load(String login, String password, boolean trusted, Database database) {
        User user = new User();
        user.readInfo(login, password, database);
        if (!trusted && (password == null || !password.equals(user.password()) || user.blocked())) {
            throw new AccessDeniedException();
        }
        user.readComponents(database);

        if (user.securityGroup == SecurityGroup.Administrators) {
            user.addSystemTools();
        }
        return user;
    }

    private void addSystemTools() {
        for (Component component : components) {
            if (component.className().equals(SystemTools.class.getCanonicalName())) {
                return;
            }
        }

        components.add(new Component(null, SystemTools.class.getCanonicalName(), Resources.get(SystemTools.strings.Title)));
    }

    @SuppressWarnings("unchecked")
    private Users getUsers() {
        CLASS<? extends Users> result = null;
        Class<?> cls = Users.class;

        for (CLASS<? extends Table> table : Runtime.instance().tables()) {
            if (table.instanceOf(cls)) {
                result = (CLASS<? extends Users>) table;
                cls = result.getJavaClass();
            }
        }

        return result != null ? (Users) result.get() : null;
    }

    private void readInfo(String login, String password, Database database) {
        Users users = getUsers();
        users.setDatabase(database);

        Collection<Field> fields = new ArrayList<Field>();
        fields.add(users.recordId.get());
        fields.add(users.password.get());
        users.password.get().mask = false;
        fields.add(users.settings.get());
        fields.add(users.phone.get());
        fields.add(users.email.get());
        fields.add(users.securityGroup.get());

        SqlToken where = new Rel(new Lower(users.name.get()), Operation.Eq, new sql_string(login.toLowerCase()));

        users.read(fields, where);

        if (users.next()) {
            this.id = users.recordId.get().guid();
            this.name = login;
            this.password = users.password.get().string().get();
            this.settings = users.settings.get().string().get();
            this.phone = users.phone.get().string().get();
            this.email = users.email.get().string().get();
            this.securityGroup = SecurityGroup.fromGuid(users.securityGroup.get().guid());
        } else {
            throw new AccessDeniedException();
        }

        try {
            if (!users.getExtraParameters(this, parameters))
                throw new AccessDeniedException();
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Throwable e) {
            Trace.logError(e);
        }
    }

    private void readComponents(Database database) {
        UserEntries userEntries = new UserEntries.CLASS<UserEntries>().get();
        userEntries.setDatabase(database);

        Collection<Field> fields = new ArrayList<Field>();
        fields.add(userEntries.entries.get().id.get());
        fields.add(userEntries.entries.get().name.get());

        SqlToken first = new Rel(userEntries.user.get(), Operation.Eq, userEntries.users.get().recordId.get());

        SqlToken second = new Rel(userEntries.entry.get(), Operation.Eq, userEntries.entries.get().recordId.get());

        SqlToken third = new Rel(new Lower(userEntries.users.get().name.get()), Operation.Eq, new sql_string(
                name().toLowerCase()));

        SqlToken where = new And(new And(first, second), third);

        userEntries.sortFields.add(userEntries.position);

        userEntries.read(fields, where);

        List<Component> components = new ArrayList<Component>();

        while (userEntries.next()) {
            String className = userEntries.entries.get().id.get().string().get();
            String title = userEntries.entries.get().name.get().string().get();
            components.add(new Component(null, className, title));
        }

        setComponents(components.toArray(new Component[0]));
    }

    @Override
    public String settings() {
        return settings;
    }

    @Override
    public void setSettings(String settings) {
        this.settings = settings;
    }

    @Override
    public void save(Database database) {
        Users users = new Users.CLASS<Users>().get();
        users.setDatabase(database);

        users.settings.get().set(new string(settings));
        users.update(id);
    }
}
