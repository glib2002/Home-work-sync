/*
 * Copyright (C) 2016 CodeFireUA <edu@codefire.com.ua>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javasync.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CodeFireUA <edu@codefire.com.ua>
 */
public class DownloadTask implements Runnable {

    private final LinkStore linkStore;
    private final File store;
    private long total;
    private String source;
    private String target;
    private Descriptor descriptor;
    
    public DownloadTask(LinkStore linkStore, File store) {
        this.linkStore = linkStore;
        this.store = store;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (linkStore) {
                try {
                    linkStore.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            source = linkStore.getLinks().poll();

            if (source != null) {
                try {
                    URLConnection conn = new URL(source).openConnection();
                    conn.getContentType();
                    total = conn.getContentLengthLong();

                    URL url = conn.getURL();

                    // Get file path on server (decoded)
                    String decodedFile = URLDecoder.decode(new String(url.getFile().getBytes("ISO-8859-1"), "UTF-8"), "UTF-8");
                    // Get file name from file path
                    String filename = new File(decodedFile).getName();

                    File targetFile = new File(store, filename);
                    target = targetFile.toString();
                    
                    descriptor = new Descriptor(source, target, filename, total);

                    // PROCESS
                    linkStore.beginDownload(descriptor);
                    
                    download(conn.getInputStream(), targetFile);

                    linkStore.progressChanged(descriptor);
                    linkStore.completeDownload(descriptor);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                break;
            }
        }
    }

    private void download(InputStream inputStream, File targetFile) throws InterruptedException {
        long portion = total / (1024 * 1024);
        long mount = 0;

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {

            byte[] buffer = new byte[8192]; // 8K buffer

            for (int read; (read = inputStream.read(buffer)) >= 0;) {

                if (read > 0) {
                    descriptor.increase(read);
                    mount += read;

                    if (mount - portion >= 0) {
                        mount = 0;
//                        System.out.println(read + "  " + descriptor.getDownload());
                        linkStore.progressChanged(descriptor);
                    }
                }

                fos.write(buffer, 0, read);
                fos.flush();
                
//                Thread.sleep(1);
            }
        } catch (IOException ex) {
            Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public class Descriptor {

        private final String source;
        private final String target;
        private final String filename;
        private final long total;
        private long download;

        public Descriptor(String source, String target, String filename, long total) {
            this.source = source;
            this.target = target;
            this.filename = filename;
            this.total = total;
        }

        public String getFilename() {
            return filename;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public long getTotal() {
            return total;
        }

        public long getDownload() {
            return download;
        }

        public void increase(long progress) {
            this.download += progress;
        }

        @Override
        public String toString() {
            return String.format("[ %3d%% ] %s", download * 100 / total, filename);
        }
    }
}
