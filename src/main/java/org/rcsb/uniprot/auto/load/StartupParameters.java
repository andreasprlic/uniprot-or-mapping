package org.rcsb.uniprot.auto.load;

/**
 * Created by ap3 on 25/08/2014.
 */
public class StartupParameters {

    static int availableProcs = Runtime.getRuntime().availableProcessors();

    public int commitSize = 100;
    public int startPosition = 0;
    public int endPosition   = -1;

    public String configFilePath;

    static int threadPoolSize = availableProcs - 1;
    static {
        if ( threadPoolSize < 1)
            threadPoolSize = 1;
        if ( threadPoolSize > 4)
            threadPoolSize =4;
    }

    public int threadSize = threadPoolSize;

    public int getThreadSize() {
        return threadSize;
    }

    public void setThreadSize(int threadSize) {
        this.threadSize = threadSize;
    }

    public int getCommitSize() {
        return commitSize;
    }

    public void setCommitSize(int commitSize) {
        this.commitSize = commitSize;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    @Override
    public String toString() {
        return "StartupParameters{" +
                "commitSize=" + commitSize +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", configFilePath=" + configFilePath +
                ", threadSize=" + threadSize +
                '}';
    }
}
