package uk.me.ferrier.mindex;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.IOException;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class Mindex {

    void discoverJar(String path) throws IOException {
        JarFile jar = new JarFile(path);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            System.out.println(path + ":" + entryName);

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

    public void seekMaven() throws IOException {
        Iterator<File> list
            = FileUtils.iterateFiles(mavenRoot,
                                     new SuffixFileFilter(".jar"),
                                     TrueFileFilter.INSTANCE);
        while (list.hasNext()) {
            File f = list.next();
            discoverJar(f.getAbsolutePath());
        }
    }

    public static void main(String[] argv) throws IOException {
        Mindex mindex = new Mindex();
        mindex.seekMaven();
    }
}
