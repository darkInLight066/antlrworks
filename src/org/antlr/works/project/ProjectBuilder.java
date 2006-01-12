package org.antlr.works.project;

import edu.usfca.xj.appkit.utils.XJDialogProgress;
import edu.usfca.xj.appkit.utils.XJDialogProgressDelegate;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.components.project.CContainerProject;
import org.antlr.works.engine.EngineCompiler;
import org.antlr.works.utils.StreamWatcherDelegate;

import javax.swing.*;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

public class ProjectBuilder implements StreamWatcherDelegate, XJDialogProgressDelegate {

    protected CContainerProject project;
    protected XJDialogProgress progress;

    protected boolean cancel;
    protected int buildingProgress;

    public ProjectBuilder(CContainerProject project) {
        this.project = project;
        this.progress = new XJDialogProgress(project.getXJFrame(), true);
        this.progress.setDelegate(this);
    }

    public List getListOfDirtyBuildFiles(String type) {
        return project.getBuildList().getDirtyBuildFilesOfType(type);
    }

    public List buildListOfGrammarBuildFiles() {
        return getListOfDirtyBuildFiles(CContainerProject.FILE_TYPE_GRAMMAR);
    }

    public List buildListOfJavaBuildFiles() {
        ProjectBuildList buildList = project.getBuildList();

        // Update the build list with list of files on the disk

        File[] files = new File(project.getProjectFolder()).listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String filePath = file.getAbsolutePath();
            if(!CContainerProject.getFileType(filePath).equals(CContainerProject.FILE_TYPE_JAVA))
                continue;

            if(buildList.isFileExisting(filePath, CContainerProject.FILE_TYPE_JAVA))
                buildList.handleExternalModification(filePath, CContainerProject.FILE_TYPE_JAVA);
            else
                buildList.addFile(filePath, CContainerProject.FILE_TYPE_JAVA);
        }

        // Remove all non-existent file on disk that are still in the the build list.
        // Note: only files that are in the project folder are removed because some
        // java file, located outside the project folder, may still be referenced
        // by the project.

        String projectFolder = project.getProjectFolder();
        for (Iterator iterator = buildList.getBuildFilesOfType(CContainerProject.FILE_TYPE_JAVA).iterator(); iterator.hasNext();)
        {
            ProjectBuildList.BuildFile buildFile = (ProjectBuildList.BuildFile) iterator.next();
            if(buildFile.getFileFolder().equals(projectFolder)) {
                // The build file is located inside the project folder.
                // Verify that this file still exists.
                if(!new File(buildFile.getFilePath()).exists()) {
                    // The file doesn't exist anymore. Remove it from the build list.
                    buildList.removeFile(buildFile.getFilePath(), CContainerProject.FILE_TYPE_JAVA);
                }
            }
        }

        return getListOfDirtyBuildFiles(CContainerProject.FILE_TYPE_JAVA);
    }

    public boolean generateGrammarBuildFiles(List buildFiles) {
        for (Iterator iterator = buildFiles.iterator(); iterator.hasNext() && !cancel;) {
            ProjectBuildList.BuildFile buildFile = (ProjectBuildList.BuildFile) iterator.next();

            String file = buildFile.getFilePath();
            String libPath = buildFile.getFileFolder();
            String outputPath = buildFile.getFileFolder();

            setProgressStepInfo("Generating \""+ XJUtils.getLastPathComponent(file)+"\"...");

            String error = EngineCompiler.runANTLR(file, libPath, outputPath, this);
            if(error != null) {
                project.buildReportError(error);
                return false;
            } else {
                buildFile.setDirty(false);
                project.changeDone();
            }
        }
        return true;
    }

    public boolean compileFile(String file) {
        String outputPath = project.getProjectFolder();
        String error = EngineCompiler.compileFiles(new String[] { file }, outputPath, this);
        if(error != null) {
            project.buildReportError(error);
            return false;
        } else {
            return true;
        }
    }

    public boolean compileJavaBuildFiles(List buildFiles) {
        for (Iterator iterator = buildFiles.iterator(); iterator.hasNext() && !cancel;) {
            ProjectBuildList.BuildFile buildFile = (ProjectBuildList.BuildFile) iterator.next();
            String file = buildFile.getFilePath();
            setProgressStepInfo("Compiling \""+ XJUtils.getLastPathComponent(file)+"\"...");
            if(!compileFile(file))
                return false;
            else {
                buildFile.setDirty(false);
                project.changeDone();
            }
        }
        return true;
    }

    public void setProgressStepInfo(String info) {
        progress.setInfo(info);
        progress.setProgress(++buildingProgress);
    }

    public void performBuild() {
        List grammars = buildListOfGrammarBuildFiles();
        List javas = buildListOfJavaBuildFiles();

        int total = grammars.size()+javas.size();
        if(total > 0) {
            progress.setIndeterminate(false);
            progress.setProgress(0);
            progress.setProgressMax(total);

            if(generateGrammarBuildFiles(grammars) && !cancel) {
                if(grammars.size() > 0) {
                    // Rebuild the list of Java files because ANTLR may have
                    // generated some ;-)
                                        
                    javas = buildListOfJavaBuildFiles();
                    total = grammars.size()+javas.size();
                    progress.setProgressMax(total);
                }
                compileJavaBuildFiles(javas);
            }
        }
    }

    public void build() {
        cancel = false;
        buildingProgress = 0;

        progress.setCancellable(true);
        progress.setTitle("Build");
        progress.setInfo("Preparing...");
        progress.setIndeterminate(true);

        new ThreadExecution(new Runnable() {
            public void run() {
                performBuild();
                progress.close();
            }
        }).launch();

        progress.runModal();
    }

    public void performRun() {
        project.clearConsole();
        String error = EngineCompiler.runJava(project.getProjectFolder(), project.getRunParameters(), ProjectBuilder.this);
        if(error != null) {
            project.buildReportError(error);
        }
    }

    public void run() {
        progress.setCancellable(true);
        progress.setTitle("Run");
        progress.setInfo("Preparing...");
        progress.setIndeterminate(true);

        new ThreadExecution(new Runnable() {
            public void run() {
                performBuild();
                progress.setInfo("Running...");
                progress.setIndeterminate(true);
                performRun();
                progress.close();
            }
        }).launch();


        progress.runModal();
    }

    /** This method cleans the project directory by removing the following files:
     * - *.class
     */

    public void clean() {
        File[] files = new File(project.getProjectFolder()).listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String filePath = file.getAbsolutePath();
            if(filePath.endsWith(".class")) {
                file.delete();
            }
        }

        // Mark all files as dirty
        project.getBuildList().setAllFilesToDirty(true);

        // Mark the project as dirty
        project.changeDone();
    }

    public void dialogDidCancel() {
        cancel = true;
    }

    public void streamWatcherDidStarted() {

    }

    public void streamWatcherDidReceiveString(String string) {
        project.printToConsole(string);
    }

    public void streamWatcherException(Exception e) {
        project.printToConsole(e);
    }

    protected class ThreadExecution {

        protected Runnable r;

        public ThreadExecution(Runnable r) {
            this.r = r;
        }

        public void launch() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new Thread(r).start();
                }
            });
        }
    }
}