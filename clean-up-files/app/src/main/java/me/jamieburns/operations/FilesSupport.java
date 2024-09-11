package me.jamieburns.operations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;


public class FilesSupport {

    private static final long DEFAULT_BLOCK_SIZE = 4096L;
    private static final String DEFAULT_MESSAGE_DIGEST_ALGORITHM = "SHA-256";

    public static final List<FileData> buildFileList( String path, String filenameFilter ) {

        // get our list of files from walking the folder tree
        return FolderSupport.fileStream( new File( path ), filenameFilter )
                    .collect(Collectors.toList());
    }


    public static final List<FileData> rebuildWithChunkHash( List<FileData> fileDataList )
    {
        return rebuildWithHash( fileDataList, generateHashStringByChunkHashFn() );
    }


    static final ThrowingFunction<FunctionArgs, String, Exception> generateHashStringByChunkHashFn() {

        return ( args ) -> {

            var inputStream = args.inputStream();
            var chunkSize = args.chunkSize();
            var digest = args.digest();

            digest.reset();

            byte[] chunk = new byte[ (int) chunkSize ];
            int bytesRead = inputStream.read(chunk);

            if( bytesRead > 0 ) {
                digest.update(chunk, 0, bytesRead);
            }

            return convertBytesToHex(digest.digest());
        };
    }


    public static final List<FileData> rebuildWithFullHash( List<FileData> fileDataList )
    {
        return rebuildWithHash( fileDataList, generateHashStringByFullHashFn() );
    }


    static final ThrowingFunction<FunctionArgs, String, Exception> generateHashStringByFullHashFn() {

        return ( args ) -> {

            var inputStream = args.inputStream();
            var chunkSize = args.chunkSize();
            var digest = args.digest();

            digest.reset();

            byte[] chunk = new byte[(int) chunkSize ];
            int bytesRead;

            while ((bytesRead = inputStream.read(chunk)) != -1) {
                digest.update(chunk, 0, bytesRead);
            }

            return convertBytesToHex(digest.digest());
        };
    }


    private static final List<FileData> rebuildWithHash(
            List<FileData> fileDataList,
            ThrowingFunction<FunctionArgs, String, Exception> hashFn )
    {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return List.of();
        }

        var blockSize = blockSize( fileDataList.get(0).path());

        return fileDataList.stream()
                .map( fd -> newFileDataWithHash( fd, hashFn, blockSize ) )
                .collect( Collectors.toList() );
    }


    private static final FileData newFileDataWithHash(
            FileData fileData,
            ThrowingFunction<FunctionArgs, String, Exception> hashFn,
            long blockSize )
    {
        try {
            return newFileDataWithHashImpl( fileData, hashFn, blockSize );
        }
        catch( Exception e ) {
            e.printStackTrace();
            return fileData;
        }
    }


    private static final FileData newFileDataWithHashImpl(
        FileData fileData,
        ThrowingFunction<FunctionArgs, String, Exception> hashFn,
        long blockSize) throws Exception
    {
        var chunkSize = calculateChunkSize( fileData, blockSize );
        var digest = newDigest();

        try (var inputStream = Files.newInputStream(Paths.get(fileData.path())))
        {
            return newFileData( fileData, hashFn.apply( new FunctionArgs( inputStream, chunkSize, digest ) ) );
        }
    }


    private static final FileData newFileData( FileData fileData, String hashString ) {
        return new FileData.Builder()
            .fromFileData(fileData)
            .hash( hashString )
            .build();
    }

    private static final long blockSize( String path ) {

        try {
            return Files.getFileStore(Paths.get(path)).getBlockSize();
        }
        catch( IOException e ) {
            e.printStackTrace();
            return DEFAULT_BLOCK_SIZE;
        }
    }


    static final MessageDigest newDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(DEFAULT_MESSAGE_DIGEST_ALGORITHM);
    }


    private static final long calculateChunkSize( FileData fileData, long blockSize ) throws IOException{

        if( fileData == null ) {
            return DEFAULT_BLOCK_SIZE;
        }

        var idealChunkSize = Math.max((long) Math.ceil( fileData.sizeInBytes() * 0.2), 1);

        return Math.ceilDiv(idealChunkSize, blockSize) * blockSize;
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

    @FunctionalInterface
    interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    record FunctionArgs( InputStream inputStream, long chunkSize, MessageDigest digest ) {}
}
