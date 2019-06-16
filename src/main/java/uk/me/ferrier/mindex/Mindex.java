package uk.me.ferrier.mindex;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class Mindex {

    void discoverJar(String path, Writer jarOut, Writer classOut) throws IOException {
        JarFile jar = new JarFile(path);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryClassFile = entry.getName();
            if (entryClassFile.endsWith(".class")) {
                String entryName = FilenameUtils.removeExtension(entryClassFile);
                String entryClass = entryName.replace("/", ".");
                jarOut.write(path + " " + entryClass + System.getProperty("line.separator"));

                int classStart = entryClass.lastIndexOf(".");
                classStart = (classStart < 0) ? 0 : classStart;
                String className = entryClass.substring(classStart + 1);
                String packageName = entryClass.substring(0, classStart);
                classOut.write(className + " " + packageName + " " + path + System.getProperty("line.separator"));
            }

            /*
              FileOutputStream fout = new FileOutputStream(f);
              InputStream in = jar.getInputStream(entry);
              byte[] buf = new byte[5000];
              int red = in.read(buf);
              while (red > -1) {
                fout.write(buf, 0, red);
                red = in.read(buf, 0, 5000);
              }
              fout.close();
            */
        }
    }

    final File mavenRoot = new File(System.getProperty("user.home")
                                    + File.separator
                                    + ".m2"
                                    + File.separator
                                    + "repository");

    final File jarIndex = new File(System.getProperty("user.home")
                                    + File.separator
                                    + ".m2"
                                    + File.separator
                                   + ".mindex.jars");

    final File classIndex = new File(System.getProperty("user.home")
                                   + File.separator
                                   + ".m2"
                                   + File.separator
                                   + ".mindex.classes");


    public void seekMaven() throws IOException {
        try (Writer jarOut = new FileWriter(jarIndex);
             Writer classOut = new FileWriter(classIndex)) {
            Iterator<File> list
                = FileUtils.iterateFiles(mavenRoot,
                                         new SuffixFileFilter(".jar"),
                                         TrueFileFilter.INSTANCE);
            while (list.hasNext()) {
                File f = list.next();
                discoverJar(f.getAbsolutePath(), jarOut, classOut);
            }
        }
    }

    public static void main(String[] argv) throws IOException {
        Mindex mindex = new Mindex();
        mindex.seekMaven();
    }
}
