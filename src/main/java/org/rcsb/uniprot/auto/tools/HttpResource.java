package org.rcsb.uniprot.auto.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 *  A file that allows to download a file from an external Http location.
 *
 * Created by ap3 on 19/08/2014.
 */
public class HttpResource {


    boolean debug = false;
    URL u ;

    File cachedFile;

    public HttpResource(URL remoteLocation, File localFile) {
        u = remoteLocation;
        cachedFile = localFile;
    }

    public boolean isLocal(){
        return cachedFile.exists();
    }

    public boolean download() {

        if( debug)
            System.out.println("Downloading " + u + " to " + cachedFile.getAbsolutePath());
        long timeS = System.currentTimeMillis();
        try {
            ReadableByteChannel rbc = Channels.newChannel(u.openStream());

            FileOutputStream fos = new FileOutputStream(cachedFile);

            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            rbc.close();
            fos.close();
            long timeE = System.currentTimeMillis();

            if (debug)
                System.out.println("successful download in " + (timeE - timeS) + " ms.");
        } catch (Exception e){
            System.err.println(e.getMessage());
           // e.printStackTrace();
            return false;
        }
        return true;
    }


    public void delete(){
        if ( cachedFile.exists() )
            cachedFile.delete();
    }
}
