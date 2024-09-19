package me.jamieburns.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;
import java.util.Optional;


public class FileDataSupport {

    private static final long DEFAULT_BLOCK_SIZE = 4096L;
    private static final String DEFAULT_MESSAGE_DIGEST_ALGORITHM = "SHA-256";

    static final List<FileData> collectFileData( String path, String matchFilenameToRegex ) {

        try {
            return FileDataSupport.throwingCollectFileData(path, matchFilenameToRegex);
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    static final List<FileData> throwingCollectFileData( String path, String matchFilenameToRegex ) throws IOException {

        var predicate = Pattern.compile( matchFilenameToRegex ).asPredicate();

        return Files.walk( Path.of( path ), Integer.MAX_VALUE )
                .filter( p -> predicate.test( p.toString() ) )
                .map( FileDataSupport::newFileData )
                .collect( Collectors.toList() );
    }

    private static FileData newFileData( Path path ) {

        long size;

        try {
            size = (long) Files.readAttributes( path, "size").get("size");
        } catch (IOException e) {
            e.printStackTrace();
            size = Long.MAX_VALUE;
        }

        var pathString = path.toString();
        var separator = path.getFileSystem().getSeparator();
        var filename = pathString.substring( pathString.lastIndexOf(separator) + 1 );

        return new FileData(
            filename,
            pathString,
            size,
            Optional.empty() );
    }

    public static final List<FileData> rebuildWithChunkHash( List<FileData> fileDataList )
    {
        return rebuildWithHash( fileDataList, generateHashStringByChunkHashFn() );
    }


    static final ThrowingFunction<FunctionArgs, String, Exception> generateHashStringByChunkHashFn() {

        return ( args ) -> {

            var fileData = args.fileData();
            var blockSize = args.blockSize();
            var digest = args.digest();

            digest.reset();

            var bytes = new byte[ (int) blockSize ];

            try( var inputStream = Files.newInputStream( Paths.get( fileData.path() ), StandardOpenOption.READ ) ) {

                int bytesRead = inputStream.read(bytes);

                if( bytesRead > 0 ) {
                    digest.update(bytes, 0, bytesRead);
                }
            }

            return convertBytesToHex(digest.digest());
        };
    }


    public static final List<FileData> rebuildWithFullHash( List<FileData> fileDataList )
    {
        var timer = new Timer<>( "FileSupport.rebuildWithFullHash" ).start();
        var result = rebuildWithHash( fileDataList, generateHashStringByFullHashFn() );
        timer.stop();
        return result;
    }


    static final ThrowingFunction<FunctionArgs, String, Exception> generateHashStringByFullHashFn() {

        return ( args ) -> {

            var fileData = args.fileData();
            var digest = args.digest();

            digest.reset();

            var bytes = Files.readAllBytes( Paths.get( fileData.path() ) );
            digest.update(bytes, 0, bytes.length);

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
            return throwingNewFileDataWithHash( fileData, hashFn, blockSize );
        }
        catch( Exception e ) {
            e.printStackTrace();
            return fileData;
        }
    }


    private static final FileData throwingNewFileDataWithHash(
        FileData fileData,
        ThrowingFunction<FunctionArgs, String, Exception> hashFn,
        long blockSize) throws Exception
    {
        var digest = newDigest();

        return FileData.newFileDataWithHash(
                fileData,
                hashFn.apply( new FunctionArgs( fileData, blockSize, digest ) ) );
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


    record FunctionArgs( FileData fileData, long blockSize, MessageDigest digest ) {}
}
