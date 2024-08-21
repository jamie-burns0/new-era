package me.jamieburns.data;

import java.util.Comparator;

public class FileDataComparator implements Comparator<FileData> {

    public int compare(FileData fd1, FileData fd2) {
        return fd1.path().compareTo( fd2.path() );
    }
}
