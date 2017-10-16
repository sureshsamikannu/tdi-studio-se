// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.java;

import static org.talend.designer.maven.model.TalendJavaProjectConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.CommonUIPlugin;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.maven.launch.MavenPomCommandLauncher;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsManager;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.maven.tools.creator.CreateMavenCodeProject;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.TalendCodeProjectUtil;
import org.talend.repository.ProjectManager;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class TalendJavaProjectManager {

    private static Map<ERepositoryObjectType, ITalendProcessJavaProject> talendCodeJavaProjects = new HashMap<>();

    private static Map<String, ITalendProcessJavaProject> talendJobJavaProjects = new HashMap<>();
    
    private static ITalendProcessJavaProject tempJavaProject;

    private static boolean initialized;

    public static void initJavaProjects(Project project) {
        // TODO should surround by workunit
        try {
            IFolder poms = getProjectPomsFolder(project);
            if (!poms.exists()) {
                initialized = false;
                IProgressMonitor monitor = new NullProgressMonitor();
                poms.create(true, true, monitor);
                // create project pom.
                IFile projectPom = ResourceUtils.getFile(poms, TalendMavenConstants.POM_FILE_NAME, false);

                // create codes folder
                IFolder code = poms.getFolder(DIR_CODES);
                code.create(true, true, null);
                IFile codePom = code.getFile(TalendMavenConstants.POM_FILE_NAME);
                // create routines folder
                IFolder routines = code.getFolder(DIR_ROUTINES);
                routines.create(true, true, null);
                // create pigudfs folder
                IFolder pigudfs = code.getFolder(DIR_PIGUDFS);
                pigudfs.create(true, true, null);
                // create beans folder
                IFolder beans = code.getFolder(DIR_BEANS);
                beans.create(true, true, null);
                // create jobs folder
                IFolder jobs = poms.getFolder(DIR_JOBS);
                jobs.create(true, true, null);
                IFile jobsPom = jobs.getFile(TalendMavenConstants.POM_FILE_NAME);
                // create process folder
                IFolder process = jobs.getFolder(DIR_PROCESS);
                process.create(true, true, null);
                IFile processPom = process.getFile(TalendMavenConstants.POM_FILE_NAME);
                // create process_mr folder
                IFolder process_mr = jobs.getFolder(DIR_PROCESS_MR);
                process_mr.create(true, true, monitor);
                IFile processMRPom = process_mr.getFile(TalendMavenConstants.POM_FILE_NAME);
                // create process_storm folder
                IFolder process_storm = jobs.getFolder(DIR_PROCESS_STORM);
                process_storm.create(true, true, monitor);
                IFile processStormPom = process_storm.getFile(TalendMavenConstants.POM_FILE_NAME);

                // create routes and services
                IFolder process_routes = jobs.getFolder(DIR_PROCESS_ROUTES);
                process_routes.create(true, true, monitor);
                IFile processRoutesPom = process_routes.getFile(TalendMavenConstants.POM_FILE_NAME);
                IFolder process_services = jobs.getFolder(DIR_PROCESS_SERVICES);
                process_services.create(true, true, monitor);
                IFile processServicesPom = process_services.getFile(TalendMavenConstants.POM_FILE_NAME);
                
                // create aggregator poms
                AggregatorPomsManager manager = new AggregatorPomsManager(project);
                manager.createAggregatorFolderPom(processPom, DIR_PROCESS,
                        PomIdsHelper.getJobGroupId(project.getTechnicalLabel()), monitor);
                manager.createAggregatorFolderPom(processMRPom, DIR_PROCESS_MR,
                        PomIdsHelper.getJobGroupId(project.getTechnicalLabel()), monitor);
                manager.createAggregatorFolderPom(processStormPom, DIR_PROCESS_STORM,
                        PomIdsHelper.getJobGroupId(project.getTechnicalLabel()), monitor);
                manager.createAggregatorFolderPom(processRoutesPom, DIR_PROCESS_ROUTES,
                        PomIdsHelper.getJobGroupId(project.getTechnicalLabel()), monitor);
                manager.createAggregatorFolderPom(processServicesPom, DIR_PROCESS_SERVICES,
                        PomIdsHelper.getJobGroupId(project.getTechnicalLabel()), monitor);
                manager.createAggregatorFolderPom(jobsPom, DIR_JOBS, PomIdsHelper.getJobGroupId(project.getTechnicalLabel()),
                        monitor);
                manager.createAggregatorFolderPom(codePom, DIR_CODES, "org.talend.codes." + project.getTechnicalLabel(), monitor); //$NON-NLS-1$

                // create codes poms
                manager.createRoutinesPom(routines.getFile(TalendMavenConstants.POM_FILE_NAME), monitor);
                if (ProcessUtils.isRequiredPigUDFs(null)) {
                    manager.createPigUDFsPom(pigudfs.getFile(TalendMavenConstants.POM_FILE_NAME), monitor);
                }
                if (ProcessUtils.isRequiredBeans(null)) {
                    manager.createBeansPom(beans.getFile(TalendMavenConstants.POM_FILE_NAME), monitor);
                }

                manager.createRootPom(projectPom, monitor);

                if (CommonUIPlugin.isFullyHeadless()) {
                    installRootPom(projectPom);
                }
            } else {
                initialized = true;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    public static void installRootPom(IFile pomFile) {
        if (initialized) {
            return;
        }
        if (pomFile == null) {
            pomFile = getProjectPomsFolder(ProjectManager.getInstance().getCurrentProject())
                    .getFile(TalendMavenConstants.POM_FILE_NAME);
        }
        try {
            new MavenPomCommandLauncher(pomFile, TalendMavenConstants.GOAL_INSTALL).execute(new NullProgressMonitor());
            initialized = true;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    public static boolean isRootPomInstalled() {
        return initialized;
    }

    public static ITalendProcessJavaProject getTalendCodeJavaProject(ERepositoryObjectType type) {
        Project project = ProjectManager.getInstance().getCurrentProject();
        ITalendProcessJavaProject talendCodeJavaProject = talendCodeJavaProjects.get(type);
        if (talendCodeJavaProject == null || talendCodeJavaProject.getProject() == null
                || !talendCodeJavaProject.getProject().exists()) {
            try {
                IProgressMonitor monitor = new NullProgressMonitor();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IFolder codeProjectFolder = getProjectPomsFolder(project).getFolder(type.getFolder());
                IProject codeProject = root.getProject(project.getTechnicalLabel() + "_" + type.name());
                if (!codeProject.exists() || TalendCodeProjectUtil.needRecreate(monitor, codeProject)) {
                    createMavenJavaProject(monitor, codeProject, codeProjectFolder);
                }
                IJavaProject javaProject = JavaCore.create(codeProject);
                if (!javaProject.isOpen()) {
                    javaProject.open(monitor);
                }
                talendCodeJavaProject = new TalendProcessJavaProject(javaProject);
                talendCodeJavaProject.cleanMavenFiles(monitor);
                talendCodeJavaProjects.put(type, talendCodeJavaProject);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return talendCodeJavaProject;
    }

    public static ITalendProcessJavaProject getTalendJobJavaProject(Property property) {
        if (property == null) {
            return getTempJavaProject();
        }
        if (property.getItem() instanceof JobletProcessItem) {
            return getTempJavaProject();
        }
        if (!(property.getItem() instanceof ProcessItem)) {
            return null;
        }
        ITalendProcessJavaProject talendJobJavaProject = null;
        try {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                        .getDefault().getService(ITestContainerProviderService.class);
                if (testContainerService.isTestContainerItem(property.getItem())) {
                    property = testContainerService.getParentJobItem(property.getItem()).getProperty();
                }
            }
            String projectTechName = ProjectManager.getInstance().getProject(property).getTechnicalLabel();
            Project project = ProjectManager.getInstance().getProjectFromProjectTechLabel(projectTechName);
            talendJobJavaProject = talendJobJavaProjects.get(property.getId());
            if (talendJobJavaProject == null || talendJobJavaProject.getProject() == null
                    || !talendJobJavaProject.getProject().exists()) {
                IProgressMonitor monitor = new NullProgressMonitor();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IProject jobProject = root.getProject(project.getTechnicalLabel() + "_" + property.getLabel()); //$NON-NLS-1$
                IPath itemRelativePath = ItemResourceUtil.getItemRelativePath(property);
                String jobFolderName = "item_" + property.getLabel(); //$NON-NLS-1$
                ERepositoryObjectType type = ERepositoryObjectType.getItemType(property.getItem());
                IFolder jobFolder = getProcessFolder(project, type).getFolder(itemRelativePath).getFolder(jobFolderName);
                if (!jobProject.exists() || TalendCodeProjectUtil.needRecreate(monitor, jobProject)) {
                    createMavenJavaProject(monitor, jobProject, jobFolder);
                }
                IJavaProject javaProject = JavaCore.create(jobProject);
                if (!javaProject.isOpen()) {
                    javaProject.open(monitor);
                }
                talendJobJavaProject = new TalendProcessJavaProject(javaProject, property);
                if (talendJobJavaProject != null) {
                    MavenPomSynchronizer pomSynchronizer = new MavenPomSynchronizer(talendJobJavaProject);
                    pomSynchronizer.syncTemplates(false);
                    pomSynchronizer.cleanMavenFiles(monitor);
                }
                talendJobJavaProjects.put(property.getId(), talendJobJavaProject);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return talendJobJavaProject;
    }

    public static ITalendProcessJavaProject getTempJavaProject() {
        NullProgressMonitor monitor = new NullProgressMonitor();
        if (tempJavaProject == null) {
            try {
                IProject project = TalendCodeProjectUtil.initCodeProject(monitor);
                if (project != null) {
                    IJavaProject javaProject = JavaCore.create(project);
                    if (!javaProject.isOpen()) {
                        javaProject.open(monitor);
                    }
                    tempJavaProject = new TalendProcessJavaProject(javaProject);
                    tempJavaProject.cleanMavenFiles(monitor);
                    tempJavaProject.createSubFolder(monitor, tempJavaProject.getSrcFolder(), JavaUtils.JAVA_INTERNAL_DIRECTORY);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return tempJavaProject;
    }

    public static ITalendProcessJavaProject getExistedTalendProject(IProject project) {
        List<ITalendProcessJavaProject> talendProjects = new ArrayList<>();
        talendProjects.addAll(talendCodeJavaProjects.values());
        talendProjects.addAll(talendJobJavaProjects.values());
        talendProjects.add(tempJavaProject);
        for (ITalendProcessJavaProject talendProject : talendProjects) {
            if (project == talendProject.getProject()) {
                return talendProject;
            }
        }
        return null;
    }

    private static void createMavenJavaProject(IProgressMonitor monitor, IProject jobProject, IFolder projectFolder)
            throws CoreException, Exception {
        if (jobProject.exists()) {
            if (jobProject.isOpen()) {
                jobProject.close(monitor);
            }
            jobProject.delete(true, true, monitor);
        }
        CreateMavenCodeProject createProject = new CreateMavenCodeProject(jobProject);
        createProject.setProjectLocation(projectFolder.getLocation());
        createProject.setPomFile(projectFolder.getFile(TalendMavenConstants.POM_FILE_NAME));
        createProject.create(monitor);
        jobProject = createProject.getProject();
        if (!jobProject.isOpen()) {
            jobProject.open(IProject.BACKGROUND_REFRESH, monitor);
        } else {
            if (!jobProject.isSynchronized(IProject.DEPTH_INFINITE)) {
                jobProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            }
        }

    }
    
    public static IFile getProjectRootPom(Project project) {
        if (project == null) {
            project = ProjectManager.getInstance().getCurrentProject();
        }
        return getProjectPomsFolder(project).getFile(TalendMavenConstants.POM_FILE_NAME);
    }

    private static IFolder getProjectPomsFolder(Project project) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return workspace.getRoot().getFolder(new Path(project.getTechnicalLabel() + "/" + DIR_POMS)); //$NON-NLS-1$
    }

    private static IFolder getProcessesFolder(Project project) {
        return getProjectPomsFolder(project).getFolder(DIR_JOBS);
    }

    private static IFolder getCodesFolder(Project project) {
        return getProjectPomsFolder(project).getFolder(DIR_CODES);
    }

    private static IFolder getProcessFolder(Project project, ERepositoryObjectType type) {
        return getProcessesFolder(project).getFolder(type.getFolder());
    }

    public static void removeDeletedJavaProject() {
        // TODO when do delete in repo, remove javaProject, remove it from maps.
    }

    public static void createUserDefineFolder() {
        // TODO call it when create folders in repo, add aggregator pom, add it to parent modules.
    }

    public static void removeUserDefineFolder() {
        // TODO
    }

    public static void moveUserDefineFolder() {
        // TODO
    }

    public static void renameUserDefineFolder() {
        // TODO
    }

    public static void deleteEclipseProjectByNatureId(String natureId) throws CoreException {
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for (IProject project : projects) {
                    if (project.hasNature(natureId)) {
                        IFile eclipseClasspath = project.getFile(CLASSPATH_FILE_NAME);
                        if (eclipseClasspath.exists()) {
                            eclipseClasspath.delete(true, monitor);
                        }
                        IFile projectFile = project.getFile(PROJECT_FILE_NAME);
                        if (projectFile.exists()) {
                            projectFile.delete(true, monitor);
                        }
                        project.delete(false, true, monitor);
                    }
                }
            };

        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            ISchedulingRule schedulingRule = workspace.getRoot();
            // the update the project files need to be done in the workspace runnable to avoid all
            // notification
            // of changes before the end of the modifications.
            workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
        } catch (CoreException e) {
            if (e.getCause() != null) {
                ExceptionHandler.process(e.getCause());
            } else {
                ExceptionHandler.process(e);
            }
        }

    }

}
