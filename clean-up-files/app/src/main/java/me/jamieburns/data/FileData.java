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

    public static FileData newFileDataWithHash( FileData fileData, String hash ) {
        return new FileData( fileData.filename(), fileData.path(), fileData.sizeInBytes(), Optional.of( hash ) );
    }
}

