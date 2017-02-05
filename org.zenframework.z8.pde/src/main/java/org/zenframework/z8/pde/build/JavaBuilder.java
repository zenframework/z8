package org.zenframework.z8.pde.build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.StartupCodeGenerator;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.BuildPathManager;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.preferences.PreferencePageConsts;

public class JavaBuilder {
	final static public String Version = "5.0";

	final static public String Usercode = "usercode.jar";

	final static public String ResourcesPath = "../resources";
	final static public String ResourceExt = ".nls";

	final static public String BirtReports = "reports";
	final static public String BirtReportExt = ".rptdesign";
	final static public String BirtReportsList = "reportbinding.xml";

	final static public String ImportFiles = "import";
	final static public String ImportFilePrefix = "import";
	final static public String ImportFileExt = ".xml";

	final static public String HelpFilesFolder = "Help";
	final static public String HelpFiles = "../" + HelpFilesFolder;

	private IProject m_project;
	static private List<Project> m_projects_ref_from_main;
	static private FileFilter m_fileFilter;

	public JavaBuilder(IProject project) {
		m_project = project;
		m_projects_ref_from_main = new ArrayList<Project>();
		m_projects_ref_from_main.clear();
		// getAllReferencedProjectsFromMain(project);
	}

	// Сбор массива всех проектов связанных с запускаемым, включая его самого
	static private IProject[] getAllReferencedProjectsFromMain(IProject iProject) {
		if(iProject == null) {
			return new IProject[0];
		}
		Project project = Workspace.getInstance().getProject(iProject);

		Project[] projs = project.getReferencedProjects();
		IProject[] extret = new IProject[project.getReferencedProjects().length + 1];
		extret[0] = iProject;
		for(int i = 0; i < project.getReferencedProjects().length; i++)
			extret[i + 1] = (IProject)projs[i].getResource();
		return extret;
	}

	static private FileFilter SvnFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() && (!file.getName().equalsIgnoreCase(".svn"));
		}
	};

	static private List<File> getFileListingIgnoreSVNwithFilterNoSort(File aStartingDir) {
		List<File> result = new ArrayList<File>();
		for(File file : aStartingDir.listFiles(m_fileFilter))
			result.add(file);
		for(File dirNoSVN : aStartingDir.listFiles(SvnFilter)) {
			// must be a directory
			List<File> deeperList = getFileListingIgnoreSVNwithFilterNoSort(dirNoSVN);
			result.addAll(deeperList);
		}
		return result;
	}

	protected IProject getProject() {
		return m_project;
	}

	private class PrepareForRun {
		IProject m_iProject;
		Path m_javaClasses;
		Path m_webInf;
		String m_manifestFilePath;
		boolean m_useJar;
		String m_memoryBound;

		String[] javacArgs = null;
		String[] jarArgs = null;

		PrepareForRun(IProject iProject, String memoryBound, boolean useJar) {
			m_useJar = useJar;
			m_memoryBound = memoryBound;
			m_iProject = iProject;
			m_webInf = new Path(Plugin.getPreferenceString(PreferencePageConsts.ATTR_WEB_INF_PATH));
			m_manifestFilePath = m_webInf.toString() + "/manifest.mf";
			// определяем путь для генерации исполняемых class-файлов:
			IPath classes = m_useJar ? new Path(BuildPathManager.getJavaOutputPath(m_iProject).toString() + "/../classes") : m_webInf.append("/classes");
			m_javaClasses = (Path)classes;

			// создаём дир-рию (если нет) для вывода исполняемых class-файлов
			File classesDir = classes.makeAbsolute().toFile();
			if(!classesDir.canRead())
				classesDir.mkdir();
		}

		void stage_stopPreviosLaunched() {
			Plugin.getDefault().shutdownAllLaunched();
		}

		boolean stage_javac() throws IOException, JavaBuilderException {

			Project project = Workspace.getInstance().getProject(m_iProject);
			String[] sources = getJavaFilesList(project, m_javaClasses);
			if(!(sources.length > 0))
				return false;

			List<String> args = new ArrayList<String>();
			args.add("javac.exe");
			args.add("-g");
			args.add("-J-Xmx" + m_memoryBound + "m");
			args.add("-nowarn");
			args.add("-encoding");
			args.add("UTF-8");
			args.add("-classpath");
			args.add("\"" + m_webInf + "/lib/org.zenframework.z8.server.jar;" + m_javaClasses + "\"");
			args.add("-d");
			args.add(m_javaClasses.toString());

			IPath javaFilesLstPath = project.getOutputPath().append("javafiles.lst");

			args.add("-sourcepath");
			args.add(project.getOutputPath().toString());

			BufferedWriter fileWriter = null;

			try {
				fileWriter = new BufferedWriter(new FileWriter(javaFilesLstPath.toString(), false));

				for(String path : sources) {
					fileWriter.write(path);
					fileWriter.newLine();
				}
			} catch(FileNotFoundException e) {
				Plugin.log(e);
			} catch(IOException e) {
				Plugin.log(e);
			} finally {
				try {
					fileWriter.close();
				} catch(IOException e) {
					Plugin.log(e);
				}
			}

			args.add("@\"" + javaFilesLstPath + '"');

			javacArgs = args.toArray(new String[args.size()]);

			StringBuffer javaErrors = new StringBuffer();

			Process process = Runtime.getRuntime().exec(javacArgs);

			assert (process != null);

			new Pipe(process.getErrorStream(), javaErrors);

			if(process.exitValue() != 0) {
				throw new JavaBuilderException(javaErrors.toString());
			}

			return true;
		}

		void stage_cleanOldClassesOrJar() {
			if(m_useJar) {
				IPath targetPath = m_webInf.append("/classes");

				try {
					if(targetPath.makeAbsolute().toFile().exists())
						FileUtils.removeDir(targetPath);
				} catch(IOException e) {
					Plugin.log(e);
				}
			} else {
				IPath targetPath = m_webInf.append("/lib/" + Usercode);

				File target = targetPath.makeAbsolute().toFile();

				if(target.canRead())
					target.delete();
			}
		}

		void stage_manifest() throws RepositoryLocalChangesException {
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("dd.MM.yyyy HH:ss");
			BufferedWriter manifestWriter = null;
			try {
				manifestWriter = new BufferedWriter(new FileWriter(m_manifestFilePath, false));
				String line = "Manifest-Version: 1.0" + "\r\n";
				line = line + "Created-By:" + "\r\n";
				line = line + "\r\n";
				line = line + "Name: org/zenframework/z8/" + "\r\n";
				line = line + "Implementation-Title: usercode.jar" + "\r\n";
				line = line + "Implementation-Version: " + sdf.format(new GregorianCalendar().getTime()) + "\r\n";
				line = line + "Implementation-Vendor: ZENFRAMEWORK\r\n";
				line = line + "Specification-Title: " + "\r\n";
				line = line + "Specification-Version: ";
				line = line + "\r\n";
				manifestWriter.write(line);
			} catch(FileNotFoundException e) {
				Plugin.log(e);
			} catch(IOException e) {
				Plugin.log(e);
			} finally {
				try {
					if(manifestWriter != null) {
						manifestWriter.close();
					}
				} catch(IOException e) {
					Plugin.log(e);
				}
			}
		}

		void stage_jar() throws IOException, JavaBuilderException {
			List<String> args = new ArrayList<String>();
			args.add("jar.exe");

			String jarFilePath = m_webInf.toString() + "/lib/" + Usercode;
			new File(jarFilePath);
			args.add("cmf");

			args.add(m_manifestFilePath);
			args.add(jarFilePath);
			args.add("-C");
			args.add(m_javaClasses.toString());
			args.add(".");
			jarArgs = args.toArray(new String[args.size()]);

			StringBuffer jarErrors = new StringBuffer();

			Process process = Runtime.getRuntime().exec(jarArgs);
			new Pipe(process.getErrorStream(), jarErrors);

			if(process != null && process.exitValue() != 0) {
				throw new JavaBuilderException(jarErrors.toString());
			}
		}

		void stage_deployResources() {
			IPath resourcesPath = m_webInf.append(ResourcesPath);
			File resourcesDir = resourcesPath.makeAbsolute().toFile();

			FileFilter resourcesFilter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && (file.getName().endsWith(ResourceExt));
				}
			};

			IPath staticReportsExploded = m_webInf.append(BirtReports);
			File staticReportsExplodedDir = staticReportsExploded.makeAbsolute().toFile();

			FileFilter fileFilterReports = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && (file.getName().endsWith(BirtReportExt));
				}
			};

			FileFilter fileFilterReportBinding = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && (file.getName().equalsIgnoreCase(BirtReportsList));
				}
			};

			for(IProject proj : getAllReferencedProjectsFromMain(m_project)) {
				IPath Project = proj.getLocation();
				File ProjectDir = Project.makeAbsolute().toFile();

				if(ProjectDir.canRead()) {
					if(!resourcesDir.canRead()) {
						resourcesDir.mkdir();
					}
					m_fileFilter = resourcesFilter;
					for(File xml_file : getFileListingIgnoreSVNwithFilterNoSort(ProjectDir)) {
						try {
							FileUtils.copyFile(new Path(xml_file.getPath()), resourcesPath.append(xml_file.getName()), true);
						} catch(IOException e) {
							e.printStackTrace();
						}
					}

					if(!staticReportsExplodedDir.canRead()) {
						staticReportsExplodedDir.mkdir();
					}
					m_fileFilter = fileFilterReports;
					for(File rpt_file : getFileListingIgnoreSVNwithFilterNoSort(ProjectDir)) {
						try {
							FileUtils.copyFile(new Path(rpt_file.getPath()), staticReportsExploded.append(rpt_file.getName()), true);
						} catch(IOException e) {
							e.printStackTrace();
						}
					}

					m_fileFilter = fileFilterReportBinding;
					for(File reportbinding_file : getFileListingIgnoreSVNwithFilterNoSort(ProjectDir)) {
						try {
							FileUtils.copyFile(new Path(reportbinding_file.getPath()), staticReportsExploded.append(reportbinding_file.getName()), true);
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	boolean compareSourceVsTarget(IPath sourcePath, IPath targetPath) {
		File sourceFile = sourcePath.toFile();
		File targetFile = targetPath.toFile();

		String sourceFile_truename = "";
		String targetFile_truename = "";
		try {
			String canonical_form = sourceFile.getCanonicalPath();
			sourceFile_truename = canonical_form.substring(canonical_form.lastIndexOf(File.separator) + 1, canonical_form.lastIndexOf('.'));
			canonical_form = targetFile.getCanonicalPath();
			targetFile_truename = canonical_form.substring(canonical_form.lastIndexOf(File.separator) + 1, canonical_form.lastIndexOf('.'));
		} catch(IOException e) {
			Plugin.log(e);
		}

		final String _targetFile_truename = targetFile_truename;
		// для проверки случая если различается только регистр некоторых букв в
		// имени java-файла и class-файла (exists() не различает регистр!!)
		if(!sourceFile_truename.equals(targetFile_truename)) {// если есть
																// различие, то
																// class-файл от
																// старой версии
																// java-файла =>
																// удаляем все
																// такие
																// class-файлы
																// !!
			FilenameFilter classFilesFilter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.equals(_targetFile_truename + ".class") || name.startsWith(_targetFile_truename + "$");
				}
			};
			File dir = new File(targetFile.getPath().substring(0, targetFile.getPath().lastIndexOf(File.separator) + 1));
			for(File deleteOldClassFile : dir.listFiles(classFilesFilter)) {
				deleteOldClassFile.delete();
			}
		}

		// на случай если java-файл вообще не существует
		if(!sourceFile.exists())
			return true;

		return targetFile.exists() && sourceFile.lastModified() < targetFile.lastModified();
	}

	private String[] getJavaFilesList(Project project, IPath javaClasses) {
		List<String> list = new ArrayList<String>();

		CompilationUnit[] dependencies = project.getDependencies();

		for(CompilationUnit compilationUnit : dependencies) {
			if(!compilationUnit.containsNativeType()) {
				IPath sourcePath = compilationUnit.getOutputPath();
				IPath targetPath = javaClasses.append(compilationUnit.getPackagePath()).append(compilationUnit.getSimpleName()).addFileExtension("class");

				if(!compareSourceVsTarget(sourcePath, targetPath)) {
					list.add(sourcePath.toString());
				}
			}
		}

		// checking PersistentList.java
		IPath sourcePath = project.getOutputPath().append(StartupCodeGenerator.getRuntimeJavaPath(project));
		IPath targetPath = javaClasses.append(StartupCodeGenerator.getRuntimeClassPath(project));

		if(!compareSourceVsTarget(sourcePath, targetPath)) {
			list.add(sourcePath.toString());
		}

		list.add(project.getOutputPath().append(StartupCodeGenerator.Z8BlRuntimePath).toString());

		return list.toArray(new String[list.size()]);
	}

	static public void run(IProject iProject, boolean isDebugMode, String memoryBound, boolean useJar) throws IOException, JavaBuilderException, RepositoryLocalChangesException {
		new JavaBuilder(iProject).build(isDebugMode, memoryBound, useJar);
	}

	protected void build(boolean isDebugMode, String memoryBound, boolean useJar) throws IOException, JavaBuilderException, RepositoryLocalChangesException {
		IProject iProject = getProject();
		PrepareForRun prepareForRun = new PrepareForRun(iProject, memoryBound, useJar);

		prepareForRun.stage_stopPreviosLaunched();
		if(prepareForRun.stage_javac()) {
			prepareForRun.stage_cleanOldClassesOrJar();

			if(useJar) {
				prepareForRun.stage_manifest();
				prepareForRun.stage_jar();
			}
		}

		// копирование файлов ресурсов из проекта в exploded-директорию запуска
		prepareForRun.stage_deployResources();

		prepareForRun = null;
	}

	public class Pipe {
		public Pipe(InputStream in, OutputStream out) {
			byte[] bytes = new byte[256];

			while(true) {
				try {
					int bytesRead = in.read(bytes);

					if(bytesRead == -1) {
						return;
					}

					if(out != null) {
						out.write(bytes, 0, bytesRead);
						out.flush();
					}
				} catch(IOException e) {
					Plugin.log(e);
				}
			}
		}

		public Pipe(InputStream in, StringBuffer out) {
			byte[] bytes = new byte[256];

			while(true) {
				try {
					int bytesRead = in.read(bytes);

					if(bytesRead == -1) {
						return;
					}

					if(out != null) {
						out.append(new String(bytes, 0, bytesRead));
					}
				} catch(IOException e) {
					Plugin.log(e);
				}
			}
		}
	}
}
