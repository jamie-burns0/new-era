package me.jamieburns.operations;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;

public abstract class GroupFilesByHash implements GroupFiles<String> {

    private static final long DEFAULT_BLOCK_SIZE = 4096L;

    abstract String generateHashString(FileData fileData, long chunkSize, MessageDigest digest) throws IOException;
    abstract FileData newFileData( FileData fileData, String hashString );

    @Override
    public Map<String, List<FileData>> regroupFiles( Map<?, List<FileData>> fileDataGroupedByAnything ) {

        if( fileDataGroupedByAnything == null || fileDataGroupedByAnything.isEmpty() ) {
            return Map.of();
        }

        Map<String, List<FileData>> fileDataRegroupedByHash = new HashMap<>();

        for( var fileDataList : fileDataGroupedByAnything.values() ) {

            Map<String, List<FileData>> regroupedFileData = groupFiles( fileDataList );

            for( var key : regroupedFileData.keySet() ) {                
                fileDataRegroupedByHash.computeIfAbsent( key, unused -> new ArrayList<FileData>() );
                fileDataRegroupedByHash.get( key ).addAll( regroupedFileData.get( key ) );
            }
        }

        return fileDataRegroupedByHash;
    }

    /**
     * For reference only. Implementation of regroupFiles using Stream and Collectors apis
     */
    Map<String, List<FileData>> regroupFilesStreamingAndCollectorsImpl( Map<?, List<FileData>> fileDataGroupedByAnything ) {

        if( fileDataGroupedByAnything == null || fileDataGroupedByAnything.isEmpty() ) {
            return Map.<String, List<FileData>>of();
        }

        return fileDataGroupedByAnything.values().stream()
                .flatMap(fileDataList -> groupFiles(fileDataList).entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> {
                            existing.addAll(replacement);
                            return existing;
                        },
                        HashMap::new
                ));
    }

    @Override
    public Map<String, List<FileData>> groupFiles(List<FileData> fileDataList) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return Map.of();
        }

        try {
            return groupFilesImpl(fileDataList);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    private Map<String, List<FileData>> groupFilesImpl(List<FileData> fileDataList) throws Exception {

        long chunkSize = calculateChunkSize( fileDataList );

        System.out.println( "[chunkSize=%s]".formatted(chunkSize));

        var shaDigest = MessageDigest.getInstance("SHA-256");

        Map<String, List<FileData>> groupByHashMap = new HashMap<>();

        for (FileData fileData : fileDataList) {

            var hashString = generateHashString( fileData, chunkSize, shaDigest );

            groupByHashMap
                    .computeIfAbsent(hashString, k -> new ArrayList<>())
                    .add( newFileData( fileData, hashString ) );
        }

        return groupByHashMap;
    }


    String convertBytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


    long calculateChunkSize( List<FileData> fileDataList ) throws IOException{

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return DEFAULT_BLOCK_SIZE;
        }

        var maxFileSize = fileDataList.stream()
            .map(FileData::sizeInBytes)
            .collect(
                Collectors.maxBy(Comparator.naturalOrder())
            )
            .get();        
        
        var blockSize = 0L;

        FileStore fileStore = Files.getFileStore(Paths.get(fileDataList.get(0).path()));
        blockSize = fileStore.getBlockSize();

        System.out.println("Block size of the filesystem: " + blockSize);

        var idealChunkSize = Math.max((long) Math.ceil(maxFileSize * 0.2), 1);

        return Math.ceilDiv(idealChunkSize, blockSize) * blockSize;
    }
}