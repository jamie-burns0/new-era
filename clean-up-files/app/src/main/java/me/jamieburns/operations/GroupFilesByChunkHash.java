package me.jamieburns.operations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

import me.jamieburns.data.FileData;

public final class GroupFilesByChunkHash extends GroupFilesByHash {

    String generateHashString(FileData fileData, long chunkSize, MessageDigest digest) throws IOException {

        try (var inputStream = Files.newInputStream(Paths.get(fileData.path()))) {

            byte[] chunk = new byte[(int) chunkSize];
            int bytesRead = inputStream.read(chunk);

            if( bytesRead > 0 ) {                    
                digest.update(chunk, 0, bytesRead);                    
            }
        }

        return convertBytesToHex(digest.digest());
    }
    
    FileData newFileData( FileData fileData, String hashString ) {
        return new FileData.Builder()
            .fromFileData(fileData)
            .chunkHash( hashString )
            .build();
    }
}
