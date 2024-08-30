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

public class GroupFilesSupport {

    private static final long DEFAULT_BLOCK_SIZE = 4096L;

    private GroupFilesSupport() {}

    public static final Map<Long, List<FileData>> groupFilesBySize(List<FileData> fileDataList) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return Map.<Long, List<FileData>>of();
        }

        return fileDataList.stream()
            .collect(Collectors.groupingBy(FileData::sizeInBytes));
    }


    /**
     * For reference only. Implementation of groupFiles where the returned map is mutable.
     * The map returned in the groupFiles implementation above is not guaranteed to be mutable.
     */
    // private static final Map<Long, List<FileData>> groupFilesBySizeInMutableMapImpl( List<FileData> fileDataList ) {

    //     if( fileDataList == null || fileDataList.isEmpty() ) {
    //         return Map.<Long, List<FileData>>of();
    //     }

    //     return fileDataList.stream()
    //             .collect(Collectors.toMap(
    //                     FileData::sizeInBytes,
    //                     Collections::singletonList,
    //                     (list1, list2) -> {
    //                             List<FileData> mergedList = new ArrayList<>(list1);
    //                             mergedList.addAll(list2);
    //                             return mergedList;
    //                     }
    //             ));
    // }


    public static final Map<String, List<FileData>> regroupFilesByChunkHash( Map<?, List<FileData>> fileDataGroupedByAnything ) {

        return regroupFilesByHash( fileDataGroupedByAnything, generateHashStringByChunkHashFn() );
    }


    static final ThrowingBiFunction<FileData, Long, String, Exception> generateHashStringByChunkHashFn() {

        return ( fileData, chunkSize ) -> {

            var digest = MessageDigest.getInstance("SHA-256");

            try (var inputStream = Files.newInputStream(Paths.get(fileData.path()))) {

                byte[] chunk = new byte[chunkSize.intValue()];
                int bytesRead = inputStream.read(chunk);

                if( bytesRead > 0 ) {
                    digest.update(chunk, 0, bytesRead);
                }
            }

            return convertBytesToHex(digest.digest());
        };
    }


    public static final Map<String, List<FileData>> regroupFilesByFullHash( Map<?, List<FileData>> fileDataGroupedByAnything ) {

        return regroupFilesByHash( fileDataGroupedByAnything, generateHashStringByFullHashFn() );
    }


    static final ThrowingBiFunction<FileData, Long, String, Exception> generateHashStringByFullHashFn() {

        return ( fileData, chunkSize ) -> {

            var digest = MessageDigest.getInstance("SHA-256");

            try (var inputStream = Files.newInputStream(Paths.get(fileData.path()))) {

                byte[] chunk = new byte[chunkSize.intValue()];
                int bytesRead;

                while ((bytesRead = inputStream.read(chunk)) != -1) {
                    digest.update(chunk, 0, bytesRead);
                }
            }

            return convertBytesToHex(digest.digest());
        };
    }

    static final Map<String, List<FileData>> regroupFilesByHash(
            Map<?, List<FileData>> fileDataGroupedByAnything,
            ThrowingBiFunction<FileData, Long, String, Exception> hashFn ) {

        if( fileDataGroupedByAnything == null || fileDataGroupedByAnything.isEmpty() ) {
            return Map.of();
        }

        Map<String, List<FileData>> fileDataRegroupedByHash = new HashMap<>();

        for( var fileDataList : fileDataGroupedByAnything.values() ) {

            Map<String, List<FileData>> regroupedFileData = groupFilesByHash( fileDataList, hashFn );

            for( var key : regroupedFileData.keySet() ) {
                fileDataRegroupedByHash.computeIfAbsent( key, unused -> new ArrayList<FileData>() );
                fileDataRegroupedByHash.get( key ).addAll( regroupedFileData.get( key ) );
            }
        }

        return fileDataRegroupedByHash;
    }


    // //@Override
    // private static final Map<String, List<FileData>> regroupFilesByHash( Map<?, List<FileData>> fileDataGroupedByAnything ) {

    //     if( fileDataGroupedByAnything == null || fileDataGroupedByAnything.isEmpty() ) {
    //         return Map.of();
    //     }

    //     Map<String, List<FileData>> fileDataRegroupedByHash = new HashMap<>();

    //     for( var fileDataList : fileDataGroupedByAnything.values() ) {

    //         Map<String, List<FileData>> regroupedFileData = groupFiles( fileDataList );

    //         for( var key : regroupedFileData.keySet() ) {
    //             fileDataRegroupedByHash.computeIfAbsent( key, unused -> new ArrayList<FileData>() );
    //             fileDataRegroupedByHash.get( key ).addAll( regroupedFileData.get( key ) );
    //         }
    //     }

    //     return fileDataRegroupedByHash;
    // }

    /**
     * For reference only. Implementation of regroupFiles using Stream and Collectors apis
     */
    // Map<String, List<FileData>> regroupFilesStreamingAndCollectorsImpl( Map<?, List<FileData>> fileDataGroupedByAnything ) {

    //     if( fileDataGroupedByAnything == null || fileDataGroupedByAnything.isEmpty() ) {
    //         return Map.<String, List<FileData>>of();
    //     }

    //     return fileDataGroupedByAnything.values().stream()
    //             .flatMap(fileDataList -> groupFiles(fileDataList).entrySet().stream())
    //             .collect(Collectors.toMap(
    //                     Map.Entry::getKey,
    //                     Map.Entry::getValue,
    //                     (existing, replacement) -> {
    //                         existing.addAll(replacement);
    //                         return existing;
    //                     },
    //                     HashMap::new
    //             ));
    // }


    //@Override
    private static final Map<String, List<FileData>> groupFilesByHash(
                List<FileData> fileDataList,
                ThrowingBiFunction<FileData, Long, String, Exception> hashFn ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return Map.of();
        }

        try {
            return groupFilesByHashImpl(fileDataList, hashFn );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    private static final Map<String, List<FileData>> groupFilesByHashImpl(
            List<FileData> fileDataList,
            ThrowingBiFunction<FileData, Long, String, Exception> hashFn ) throws Exception {

        long chunkSize = calculateChunkSize( fileDataList );

        System.out.println( "[chunkSize=%s]".formatted(chunkSize));

        Map<String, List<FileData>> groupByHashMap = new HashMap<>();

        for (FileData fileData : fileDataList) {

            var hashString = hashFn.apply( fileData, chunkSize );

            groupByHashMap
                    .computeIfAbsent(hashString, k -> new ArrayList<>())
                    .add( newFileData( fileData, hashString ) );
        }

        return groupByHashMap;
    }


    static final FileData newFileData( FileData fileData, String hashString ) {
        return new FileData.Builder()
            .fromFileData(fileData)
            .hash( hashString )
            .build();
    }


    private static final String convertBytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


    static final long calculateChunkSize( List<FileData> fileDataList ) throws IOException{

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


    /**
     * See https://github.com/pivovarit/throwing-function/blob/master/src/main/java/com/pivovarit/function/ThrowingBiFunction.java
     */
    @FunctionalInterface
    interface ThrowingBiFunction<T, U, R, E extends Exception> {
        R apply(T t, U u) throws E;
    }
}
