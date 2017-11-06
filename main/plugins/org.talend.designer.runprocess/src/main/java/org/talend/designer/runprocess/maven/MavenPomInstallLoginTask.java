// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.maven;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.login.AbstractLoginTask;
import org.talend.repository.ProjectManager;

/**
 * created by ggu on 26 Mar 2015 Detailled comment
 *
 * install aggregator poms after all synchronize(codes file, java version settings) work done.
 */
public class MavenPomInstallLoginTask extends AbstractLoginTask implements IRunnableWithProgress {

    @Override
    public boolean isCommandlineTask() {
        return true;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            AggregatorPomsHelper helper = new AggregatorPomsHelper(ProjectManager.getInstance().getCurrentProject());
            helper.installRootPom(true);

            installAggregatorFolderPoms(helper);

            List<Project> references = ProjectManager.getInstance().getReferencedProjects();
            for (Project ref : references) {
                AggregatorPomsHelper refHelper = new AggregatorPomsHelper(ref);
                refHelper.installRootPom(true);
                // FIXME for reference project, not sure what case will need it.
                // installAggregatorFolderPoms(refHelper);
            }
            
            updateCodeProject(monitor, ERepositoryObjectType.ROUTINES);
            if (ProcessUtils.isRequiredPigUDFs(null)) {
                updateCodeProject(monitor, ERepositoryObjectType.PIG_UDF);
            }
            if (ProcessUtils.isRequiredBeans(null)) {
                updateCodeProject(monitor, ERepositoryObjectType.valueOf("BEANS")); //$NON-NLS-1$
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void installAggregatorFolderPoms(AggregatorPomsHelper helper) throws Exception {
        // codes
        helper.installPom(helper.getCodesFolder().getFile(TalendMavenConstants.POM_FILE_NAME), true);
        // jobs
        helper.installPom(helper.getProcessesFolder().getFile(TalendMavenConstants.POM_FILE_NAME), true);
        // process, process_mr, process_storm, route, service
        for (IResource res : helper.getProcessesFolder().members()) {
            if (res instanceof IFolder) {
                helper.installPom(((IFolder) res).getFile(TalendMavenConstants.POM_FILE_NAME), true);
            }
        }
    }

    private void updateCodeProject(IProgressMonitor monitor, ERepositoryObjectType codeProjectType) {
        try {
            ITalendProcessJavaProject codeProject = TalendJavaProjectManager.getTalendCodeJavaProject(codeProjectType);
            AggregatorPomsHelper.updateCodeProjectPom(monitor, codeProjectType, codeProject.getProjectPom());
            MavenPomSynchronizer.buildAndInstallCodesProject(codeProject, true);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

}
