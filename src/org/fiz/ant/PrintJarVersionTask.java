package org.fiz.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class PrintJarVersionTask extends Task {

    private String jarpath = null;

    public void setJarpath(String path) {
        this.jarpath = path;
    }

    public static void main(String[] args) {
        PrintJarVersionTask pjvt = new PrintJarVersionTask();
        pjvt.jarpath = "/home/user/Test/Demo/./lib/fiz.jar";
        pjvt.execute();
    }

    public void execute() throws BuildException {
        if (jarpath == null) {
            throw new BuildException("\"jarpath\" is a required parameter.");
        }

        try {
            JarFile jar = new JarFile(jarpath);
            String version = jar.getManifest().getMainAttributes().getValue(
                    Attributes.Name.IMPLEMENTATION_VERSION);
            version = (version == null)? "<not-found>": version;
            // Print the version.
            Project prj = getProject();
            prj.log("Fiz version:  " + version);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
