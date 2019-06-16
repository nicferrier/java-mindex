package uk.me.ferrier.mindex;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;

/** Indexer for Maven stuff.
 *
 * @author Nic Ferrier, <nic@ferrier.me.uk>
 */
public class Mindex implements Runnable {

    class Indexer extends ClassLoader {
        Indexer(ClassLoader loader) {
            super(loader);
        }

        Class loadIt(String name, byte[] buffer) {
            return defineClass(name, buffer, 0, buffer.length);
        }
    }

    final String eol = System.getProperty("line.separator");

    void discoverJar(ClassLoader loader,
                     String path,
                     Writer jarOut, Writer classOut, Writer methodOut) throws IOException {
        JarFile jar = new JarFile(path);
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryClassFile = entry.getName();

            if (entryClassFile.endsWith(".class")) {
                String entryName = FilenameUtils.removeExtension(entryClassFile);
                String entryClass = entryName.replace("/", ".");
                jarOut.write(path + " " + entryClass + eol);

                int classStart = entryClass.lastIndexOf(".");
                classStart = (classStart < 0) ? 0 : classStart;
                String className = entryClass.substring(classStart + 1);
                String packageName = entryClass.substring(0, classStart);
                classOut.write(className + " " + packageName + " " + path + eol);

                // Read in the class file
                InputStream in = jar.getInputStream(entry);
                byte[] buf = new byte[5000];
                byte[] classBuf = new byte[0];
                int red = in.read(buf);
                while (red > -1) {
                    int classBufLen = classBuf.length;
                    byte[] newBuf = new byte[classBufLen + red];
                    System.arraycopy(classBuf, 0, newBuf, 0, classBufLen);
                    System.arraycopy(buf, 0, newBuf, classBufLen, red);
                    classBuf = newBuf;
                    red = in.read(buf, 0, 5000);
                }

                try {
                    Indexer indexLoader = new Indexer(loader);
                    Class theClass = indexLoader.loadIt(entryClass, classBuf);
                    Method[] methods = theClass.getDeclaredMethods();
                    for (Method method : methods) {
                        int mods = method.getModifiers();
                        if (Modifier.isPublic(mods)) {
                            methodOut.write(method.getName() + " " + method.toGenericString() + eol);
                        }
                    }
                }
                catch (NoClassDefFoundError e) {
                    // System.out.println(entryClass + " cannot be loaded");
                }
                catch (IllegalAccessError e) {
                    // System.out.println(entryClass + " cannot be loaded");
                }
                catch (VerifyError e) {
                    // System.out.println(entryClass + " cannot be loaded");
                }
                catch (ClassFormatError e) {
                    // System.out.println(entryClass + " cannot be loaded");
                }
            }
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

    final File methodIndex = new File(System.getProperty("user.home")
                                      + File.separator
                                      + ".m2"
                                      + File.separator
                                      + ".mindex.methods");

    
    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  

    void seekMaven() throws IOException {
        ClassLoader loader = this.getClass().getClassLoader();

        LocalDateTime now = LocalDateTime.now();  
        String time = dtf.format(now);
        System.out.println(time + " indexing maven started");

        try (Writer jarOut = new FileWriter(jarIndex);
             Writer classOut = new FileWriter(classIndex);
             Writer methodOut = new FileWriter(methodIndex);) {
            Iterator<File> list
                = FileUtils.iterateFiles(mavenRoot,
                                         new SuffixFileFilter(".jar"),
                                         TrueFileFilter.INSTANCE);
            while (list.hasNext()) {
                File f = list.next();
                discoverJar(loader,
                            f.getAbsolutePath(),
                            jarOut, classOut, methodOut);
            }
        }

        now = LocalDateTime.now();  
        String finishedTime = dtf.format(now);
        System.out.println(finishedTime + " indexing maven finished");
    }

    public void run() {
        try {
            seekMaven();
        }
        catch (Exception e) {
        }
    }

    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    final ScheduledFuture<?> indexHandler
        = scheduler.scheduleAtFixedRate(this, 30, 30, TimeUnit.MINUTES);

    public static void main(String[] argv) throws IOException {
        Mindex mindex = new Mindex();
        mindex.seekMaven();
    }
}
