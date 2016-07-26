package org.rcsb.uniprot.auto.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 *  A file that allows to download a file from an external Http location.
 *
<<<<<<< HEAD
 * Created by ap3 on 19/08/2014.
=======
 * Created by Andreas Prlic on 19/08/2014.
>>>>>>> 8d6fa36f8bfc11025375e27f1368d9d1feed67dc
 */
public class HttpResource {

    private static final Logger logger = LoggerFactory.getLogger(HttpResource.class);

    private URL u ;

    protected File cachedFile;

    public HttpResource(URL remoteLocation, File localFile) {
        u = remoteLocation;
        cachedFile = localFile;
    }

    public boolean isLocal(){
        return cachedFile.exists();
    }

    public void download() throws IOException {


        logger.debug("Downloading " + u + " to " + cachedFile.getAbsolutePath());

        long timeS = System.currentTimeMillis();

        try (ReadableByteChannel rbc = Channels.newChannel(u.openStream())) {

            FileOutputStream fos = new FileOutputStream(cachedFile);

            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            rbc.close();
            fos.close();
        }

        long timeE = System.currentTimeMillis();


        logger.debug("successful download in " + (timeE - timeS) + " ms.");


    }


    public void delete(){
        if ( cachedFile.exists() )
            cachedFile.delete();
    }
}
