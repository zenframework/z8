package org.zenframework.z8.pde;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import org.zenframework.z8.pde.build.Z8ProjectBuilder;

public class Z8ProjectNature implements IProjectNature {
    public static final String Name = "Z8 Project Nature";
    public static final String Id = "org.zenframework.z8.pde.ProjectNature";

    protected IProject m_project;

    @Override
    public void configure() throws CoreException {
        if(m_project == null) {
            return;
        }
        IProjectDescription d = m_project.getDescription();
        ICommand command = d.newCommand();
        Map<String, String> args = new HashMap<String, String>(3);
        args.put("JavaSource", "./.java");
        command.setArguments(args);
        command.setBuilderName(Z8ProjectBuilder.Id);
        d.setBuildSpec(new ICommand[] { command });
        m_project.setDescription(d, 0, null);
    }

    @Override
    public void deconfigure() throws CoreException {
        // TODO Auto-generated method stub
    }

    @Override
    public IProject getProject() {
        return this.m_project;
    }

    @Override
    public void setProject(IProject project) {
        m_project = project;
    }

}
