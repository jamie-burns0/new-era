package me.jamieburns.operations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

import me.jamieburns.data.FileData;

public final class GroupFilesByFullHash extends GroupFilesByHash {

    String generateHashString(FileData fileData, long chunkSize, MessageDigest digest) throws IOException {

        try (var inputStream = Files.newInputStream(Paths.get(fileData.path()))) {

            byte[] chunk = new byte[(int) chunkSize];
            int bytesRead;

            while ((bytesRead = inputStream.read(chunk)) != -1) {
                digest.update(chunk, 0, bytesRead);
            }
        }

        return convertBytesToHex(digest.digest());
    }

    FileData newFileData( FileData fileData, String hashString ) {
        return new FileData.Builder()
            .fromFileData(fileData)
            .fullHash( hashString )
            .build();
    }
}
