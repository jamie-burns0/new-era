package me.jamieburns.data;

import java.util.Objects;
import java.util.Optional;

public record FileData( String filename, String path, long sizeInBytes, Optional<String> hash ) {

    public FileData {
        Objects.requireNonNull( filename );
        Objects.requireNonNull( path );
        if ( sizeInBytes < 0 ) {
            throw new RuntimeException( "Cannot create FileData record with a negative sizeInBytes. Expected positive value, got %s".formatted( sizeInBytes ));
        }
    }

    public static class Builder {

        private String filename;
        private String path;
        private long sizeInBytes;
        private Optional<String> hash = Optional.empty();

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder sizeInBytes(long sizeInBytes) {
            this.sizeInBytes = sizeInBytes;
            return this;
        }

        public Builder hash(String hash) {
            this.hash = Optional.ofNullable( hash );
            return this;
        }

        public Builder fromFileData( FileData fileData ) {

            if( fileData == null ) {
                return this;
            }

            this.filename = fileData.filename;
            this.path = fileData.path;
            this.sizeInBytes = fileData.sizeInBytes;
            this.hash = fileData.hash;

            return this;
        }

        public FileData build() {
            return new FileData(filename, path, sizeInBytes, hash);
        }
    }
}

