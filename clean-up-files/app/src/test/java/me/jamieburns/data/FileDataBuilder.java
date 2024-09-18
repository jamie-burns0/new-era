package me.jamieburns.data;

import java.util.Optional;

/**
 * Convenience FileData builder for unit tests
 */
public class FileDataBuilder {

    private String filename;
    private String path;
    private long sizeInBytes;
    private Optional<String> hash = Optional.empty();

    public FileDataBuilder filename(String filename) {
        this.filename = filename;
        return this;
    }

    public FileDataBuilder path(String path) {
        this.path = path;
        return this;
    }

    public FileDataBuilder sizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
        return this;
    }

    public FileDataBuilder hash(String hash) {
        this.hash = Optional.ofNullable( hash );
        return this;
    }

    public FileDataBuilder fromFileData( FileData fileData ) {

        if( fileData == null ) {
            return this;
        }

        this.filename = fileData.filename();
        this.path = fileData.path();
        this.sizeInBytes = fileData.sizeInBytes();
        this.hash = fileData.hash();

        return this;
    }

    public FileData build() {
        return new FileData(filename, path, sizeInBytes, hash);
    }
}
