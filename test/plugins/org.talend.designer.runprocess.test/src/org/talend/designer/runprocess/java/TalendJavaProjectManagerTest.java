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

import static org.junit.Assert.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.general.TalendJobNature;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.maven.model.TalendJavaProjectConstants;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.repository.ProjectManager;

/**
 * DOC zwxue class global comment. Detailled comment
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TalendJavaProjectManagerTest {

    private ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    private Property property;

    @Before
    public void setUp() throws Exception {
        property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId(factory.getNextId());
        property.setLabel("test");
        property.setVersion(VersionUtils.DEFAULT_VERSION);

        ProcessItem item = PropertiesFactory.eINSTANCE.createProcessItem();
        ProcessType process = TalendFileFactory.eINSTANCE.createProcessType();
        item.setProcess(process);
        item.setProperty(property);

        factory.create(item, new Path(""));
    }

    @Test
    public void testInitJavaProjects() {
        Project projectTechName = ProjectManager.getInstance().getCurrentProject();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IFolder pomsFolder = root.getFolder(new Path(projectTechName + "/poms"));
        assertTrue(pomsFolder.exists());
        IFile pomFile = pomsFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
        assertTrue(pomFile.exists());

        IFolder codesFolder = pomsFolder.getFolder(TalendJavaProjectConstants.DIR_CODES);
        assertTrue(codesFolder.exists());
        IFile codesPom = codesFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
        assertTrue(codesPom.exists());
        IFolder routinesFolder = codesFolder.getFolder(TalendJavaProjectConstants.DIR_ROUTINES);
        assertTrue(routinesFolder.exists());
        IFile routinesPom = routinesFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
        assertTrue(routinesPom.exists());

        IFolder jobsFolder = pomsFolder.getFolder(TalendJavaProjectConstants.DIR_JOBS);
        assertTrue(jobsFolder.exists());
        IFile jobsPom = jobsFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
        assertTrue(jobsPom.exists());
        IFolder processFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_PROCESS);
        assertTrue(processFolder.exists());
        IFile processPom = processFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
        assertTrue(processPom.exists());
    }

    @Test
    public void testGetTalendCodeJavaProject() throws Exception {
        ITalendProcessJavaProject talendJavaProject = TalendJavaProjectManager
                .getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);

        validateProject(talendJavaProject);

        String projectTechName = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel() + "_"
                + ERepositoryObjectType.ROUTINES.name();
        assertEquals(projectTechName, talendJavaProject.getProject().getName());
    }

    @Test
    public void testGetTalendJobJavaProject() throws Exception {
        ITalendProcessJavaProject talendJavaProject = TalendJavaProjectManager.getTalendJobJavaProject(property);

        validateProject(talendJavaProject);

        String projectTechName = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel() + "_" + property.getLabel();
        assertEquals(projectTechName, talendJavaProject.getProject().getName());
    }

    @Test
    public void testGetTempJavaProject() throws Exception {
        ITalendProcessJavaProject talendJavaProject = TalendJavaProjectManager.getTempJavaProject();

        validateProject(talendJavaProject);

        assertEquals(TalendMavenConstants.PROJECT_NAME, talendJavaProject.getProject().getName());
    }

    @Test
    public void testZDeleteEclipseProjectByNatureId() throws Exception {
        ITalendProcessJavaProject routinesProject = TalendJavaProjectManager
                .getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);
        ITalendProcessJavaProject jobProject = TalendJavaProjectManager.getTalendJobJavaProject(property);
        ITalendProcessJavaProject tempProject = TalendJavaProjectManager.getTempJavaProject();

        TalendJavaProjectManager.deleteEclipseProjectByNatureId(TalendJobNature.ID);

        assertFalse(TalendJavaProjectManager.getExistedTalendProject(routinesProject.getProject()).getProject().exists());
        assertFalse(TalendJavaProjectManager.getExistedTalendProject(jobProject.getProject()).getProject().exists());
        assertFalse(TalendJavaProjectManager.getExistedTalendProject(tempProject.getProject()).getProject().exists());
    }

    private void validateProject(ITalendProcessJavaProject talendJavaProject) throws Exception {
        assertNotNull(talendJavaProject);
        IProject project = talendJavaProject.getProject();
        assertNotNull(project);
        assertTrue(project.exists());
        assertTrue(project.isOpen());
        assertTrue(project.hasNature(JavaCore.NATURE_ID));
        assertTrue(project.hasNature("org.eclipse.m2e.core.maven2Nature"));
        assertTrue(project.hasNature(TalendJobNature.ID));

        IJavaProject javaProject = talendJavaProject.getJavaProject();
        assertNotNull(javaProject);
        assertTrue(javaProject.exists());
        assertTrue(javaProject.isOpen());
        assertTrue(javaProject.getRawClasspath() != null && javaProject.getRawClasspath().length > 0);

        assertTrue(talendJavaProject.getProjectPom().exists());
    }

    @After
    public void tearDown() throws Exception {
        factory.deleteObjectPhysical(new RepositoryObject(property));
    }
}