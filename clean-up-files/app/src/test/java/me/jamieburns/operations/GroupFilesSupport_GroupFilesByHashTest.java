package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import me.jamieburns.data.FileData;
import me.jamieburns.operations.GroupFilesSupport.ThrowingBiFunction;

public class GroupFilesSupport_GroupFilesByHashTest {

    MockedStatic<MessageDigest> mockDigest;
    MockedStatic<Files> mockFiles;
    MockedStatic<GroupFilesSupport> mockGroupFilesSupport;

    FileStore mockFileStore;

    @BeforeEach
    void before() {

        mockDigest = mockStatic( MessageDigest.class, CALLS_REAL_METHODS );
        mockFiles = mockStatic( Files.class, CALLS_REAL_METHODS );
        mockGroupFilesSupport = mockStatic( GroupFilesSupport.class, CALLS_REAL_METHODS );

        mockFileStore = mock( FileStore.class );
    }

    @AfterEach
    void after() {
        mockDigest.close();
        mockFiles.close();
        mockGroupFilesSupport.close();
    }


    @Test
    void generateHashStringByChunkHashFnWillReturnSameHashForIdenticalFiles() throws Exception {

        var chunkSize = 8L;

        var identicalContent = "identical content.......".getBytes();
        var identicalHash = "4011102d3c2caff34e9396ea2aed43b597187266f6dcd5286c62200eaaa37113";
        var identicalSizeInBytes = 24L; // 3 * chunkSize

        var uniqueContent = "unique content..".getBytes();
        var uniqueHash = "679c3eeb7cde8b204ed3cf2f794eb12e8ea9b96ad83018b8004cd66f2f5229ae";
        var uniqueSizeInBytes = 16L; // 2 * chunkSize

        var fd1Filename = "file1.txt";

        var fd1 = new FileData.Builder()
                .filename(fd1Filename)
                .path("/path/to/file1.txt")
                .sizeInBytes(identicalSizeInBytes)
                .build();

        var fd2Filename = "file2.txt";

        var fd2 = new FileData.Builder()
                .filename(fd2Filename)
                .path("/path/to/file2.txt")
                .sizeInBytes(uniqueSizeInBytes)
                .build();

        var fd3Filename = "file3.txt";

        var fd3 = new FileData.Builder()
                .filename(fd3Filename)
                .path("/path/to/file3.txt")
                .sizeInBytes(identicalSizeInBytes)
                .build();

        List<FileData> fileDataList = List.of(fd1, fd2, fd3);

        mockFiles.when( () -> Files.newInputStream(Paths.get(fd1.path()))).thenReturn(new ByteArrayInputStream(identicalContent));
        mockFiles.when( () -> Files.newInputStream(Paths.get(fd2.path()))).thenReturn(new ByteArrayInputStream(uniqueContent));
        mockFiles.when( () -> Files.newInputStream(Paths.get(fd3.path()))).thenReturn(new ByteArrayInputStream(identicalContent));

        var hashList = new ArrayList<String>();

        for( var fd : fileDataList ) {
            hashList.add( GroupFilesSupport.generateHashStringByChunkHashFn().apply( fd, Long.valueOf( chunkSize ) ) );
        }

        assertThat(hashList)
                .isNotEmpty()
                .containsExactlyInAnyOrder( identicalHash, identicalHash, uniqueHash );
    }


    @Test
    void whenFileOperationInGenerateHashStringByChunkHashFnThrowsAnIOException() {

        mockFiles.when( () -> Files.newInputStream( any() ) ).thenThrow( new IOException() );

        var fd = new FileData.Builder()
                .filename("file1")
                .path("/path/to/file1.txt")
                .sizeInBytes(1L)
                .build();

        assertThatThrownBy( () -> GroupFilesSupport.generateHashStringByFullHashFn().apply( fd, Long.valueOf( 1L )))
                .isInstanceOf( IOException.class );
    }


    @Test
    void whenMessageDigestOperationInGenerateHashStringByChunkHashFnThrowsANoSuchAlgoritmException() {

        mockDigest.when( () -> MessageDigest.getInstance( any() ) ).thenThrow( new NoSuchAlgorithmException() );

        var fd = new FileData.Builder()
                .filename("file1")
                .path("/path/to/file1.txt")
                .sizeInBytes(1L)
                .build();

        assertThatThrownBy( () -> GroupFilesSupport.generateHashStringByFullHashFn().apply( fd, Long.valueOf( 1L )))
                .isInstanceOf( NoSuchAlgorithmException.class );
    }


    @Test
    void generateHashStringByFullHashFnWillReturnSameHashForIdenticalFiles() throws Exception {

        var chunkSize = 1024L;

        var identicalContent = "identical content.......".getBytes();
        var identicalHash = "b06526e3f33bca3e28f58ed4064a5797f37c2549d7399a030c45a9c52ea74c7c";
        var identicalSizeInBytes = 24L;

        var uniqueContent = "unique content..".getBytes();
        var uniqueHash = "a1c186b318088da37f9031b6e23b71a6efe85b63eca8123338347119d5193211";

        var uniqueSizeInBytes = 16L;

        var fd1Filename = "file1.txt";

        var fd1 = new FileData.Builder()
                .filename(fd1Filename)
                .path("/path/to/file1.txt")
                .sizeInBytes(identicalSizeInBytes)
                .build();

        var fd2Filename = "file2.txt";

        var fd2 = new FileData.Builder()
                .filename(fd2Filename)
                .path("/path/to/file2.txt")
                .sizeInBytes(uniqueSizeInBytes)
                .build();

        var fd3Filename = "file3.txt";

        var fd3 = new FileData.Builder()
                .filename(fd3Filename)
                .path("/path/to/file3.txt")
                .sizeInBytes(identicalSizeInBytes)
                .build();

        List<FileData> fileDataList = List.of(fd1, fd2, fd3);

        mockFiles.when( () -> Files.newInputStream(Paths.get(fd1.path()))).thenReturn(new ByteArrayInputStream(identicalContent));
        mockFiles.when( () -> Files.newInputStream(Paths.get(fd2.path()))).thenReturn(new ByteArrayInputStream(uniqueContent));
        mockFiles.when( () -> Files.newInputStream(Paths.get(fd3.path()))).thenReturn(new ByteArrayInputStream(identicalContent));

        var hashList = new ArrayList<String>();

        for( var fd : fileDataList ) {
            hashList.add( GroupFilesSupport.generateHashStringByFullHashFn().apply( fd, Long.valueOf( chunkSize ) ) );
        }

        assertThat(hashList)
                .isNotEmpty()
                .containsExactlyInAnyOrder( identicalHash, identicalHash, uniqueHash );
    }


    @Test
    void whenFileOperationInGenerateHashStringByFullHashFnThrowsAnIOException() {

        mockFiles.when( () -> Files.newInputStream( any() ) ).thenThrow( new IOException() );

        var fd = new FileData.Builder()
                .filename("file1")
                .path("/path/to/file1.txt")
                .sizeInBytes(1L)
                .build();

        assertThatThrownBy( () -> GroupFilesSupport.generateHashStringByFullHashFn().apply( fd, Long.valueOf( 1L )))
                .isInstanceOf( IOException.class );
    }


    @Test
    void whenMessageDigestOperationInGenerateHashStringByFullHashFnThrowsANoSuchAlgoritmException() {

        mockDigest.when( () -> MessageDigest.getInstance( any() ) ).thenThrow( new NoSuchAlgorithmException() );

        var fd = new FileData.Builder()
                .filename("file1")
                .path("/path/to/file1.txt")
                .sizeInBytes(1L)
                .build();

        assertThatThrownBy( () -> GroupFilesSupport.generateHashStringByFullHashFn().apply( fd, Long.valueOf( 1L )))
                .isInstanceOf( NoSuchAlgorithmException.class );
    }


    @Test
    void whenNullMapPassedToRegroupFilesByHashShouldReturnAnEmptyMap() {
        assertThat( GroupFilesSupport.regroupFilesByHash( null, null )).isEmpty();
    }


    @Test
    void whenEmptyMapPassedToRegroupFilesByHashShouldReturnAnEmptyMap() {
        assertThat( GroupFilesSupport.regroupFilesByHash( Map.of(), null )).isEmpty();
    }


    @Test
    void whenRegroupByHashIsGivenFileDataGroupedByAnythingShouldReturnFileDataGroupedByHash() throws Exception {

        var hashString = "abc123";

        var filename1 = "file1.txt";
        var filename2 = "file2.txt";

        var fileDataGroupedByAnythingMap =
                Stream.of(
                        new FileData.Builder()
                                .filename(filename1)
                                .path("/path/to/file1.txt")
                                .sizeInBytes(1024)
                                .build(),
                        new FileData.Builder()
                                .filename(filename2)
                                .path("/path/to/file2.txt")
                                .sizeInBytes(1024)
                                .build())
                        .collect(
                                Collectors.groupingBy(FileData::sizeInBytes));

        mockGroupFilesSupport.when( () -> GroupFilesSupport.calculateChunkSize( any() ) ).thenReturn(4096L);

        var regroupedMap = GroupFilesSupport.regroupFilesByHash( fileDataGroupedByAnythingMap, ( unused1, unused2 ) -> hashString );

        assertThat( regroupedMap ).hasSize( 1 );
        assertThat( regroupedMap ).containsOnlyKeys( hashString );
        assertThat( regroupedMap.get( hashString ) )
            .map(FileData::filename, FileData::hash)
            .containsOnly(
                tuple( filename1, Optional.of(hashString) ),
                tuple( filename2, Optional.of(hashString) )
            );
    }


    @Test
    void whenRegroupByHashIsGivenFileDataGroupedByHashShouldReturnFileDataGroupedByHash() throws Exception {

        var groupedByHashString = "789xyz";
        var regroupedByHashString = "abc123";

        var filename1 = "file1.txt";
        var filename2 = "file2.txt";

        var fileDataGroupedByAnythingMap =
                Stream.of(
                        new FileData.Builder()
                                .filename(filename1)
                                .path("/path/to/file1.txt")
                                .sizeInBytes(1024)
                                .hash( groupedByHashString )
                                .build(),
                        new FileData.Builder()
                                .filename(filename2)
                                .path("/path/to/file2.txt")
                                .sizeInBytes(1024)
                                .hash( groupedByHashString )
                                .build())
                        .collect(
                                Collectors.groupingBy(FileData::hash));

        mockGroupFilesSupport.when( () -> GroupFilesSupport.calculateChunkSize( any() ) ).thenReturn(4096L);

        var regroupedMap = GroupFilesSupport.regroupFilesByHash( fileDataGroupedByAnythingMap, ( unused1, unused2 ) -> regroupedByHashString );

        assertThat( regroupedMap ).hasSize( 1 );
        assertThat( regroupedMap ).containsOnlyKeys( regroupedByHashString );
        assertThat( regroupedMap.get( regroupedByHashString ) )
            .map(FileData::filename, FileData::hash)
            .containsOnly(
                tuple( filename1, Optional.of(regroupedByHashString) ),
                tuple( filename2, Optional.of(regroupedByHashString) )
            );
    }


    @Test
    void whenRegroupFilesByChunkHashIsGivenANullMapShouldReturnAnEmptyMap() {
        assertThat( GroupFilesSupport.regroupFilesByChunkHash( null )).isEmpty();
    }


    @Test
    void whenRegroupFilesByChunkHashIsGivenAnEmptyMapShouldReturnAnEmptyMap() {
        assertThat( GroupFilesSupport.regroupFilesByChunkHash( Map.of() )).isEmpty();
    }


    @Test
    void whenAFileOperationThrowsAnIOExceptionInRegroupFilesByChunkHashShouldReturnAnEmptyMap() {

        mockFiles.when( () -> Files.newInputStream( any() ) ).thenThrow( new IOException() );

        var fd = new FileData.Builder()
                .filename("file1")
                .path("/path/to/file1.txt")
                .sizeInBytes(1L)
                .build();

        assertThat( GroupFilesSupport.regroupFilesByChunkHash( Map.of( Long.valueOf( 1L ), List.of( fd ) ) ) ).isEmpty();
    }


    @Test
    void whenMessageDigestThrowsANoSuchAlgorithmExceptionInRegroupFilesByChunkHashShouldReturnAnEmptyMap() {

        mockDigest.when( () -> MessageDigest.getInstance( any() ) ).thenThrow( new NoSuchAlgorithmException() );

        var fd = new FileData.Builder()
                .filename("file1")
                .path("/path/to/file1.txt")
                .sizeInBytes(1L)
                .build();

        assertThat( GroupFilesSupport.regroupFilesByChunkHash( Map.of( Long.valueOf( 1L ), List.of( fd ) ) ) ).isEmpty();
    }


    @Test
    void whenRegroupByChunkHashIsGivenFileDataGroupedByAnythingShouldReturnFileDataGroupedByHash() {

        Map<Long, List<FileData>> mapByAnything =
                Map.of( 1L,
                        List.of( new FileData.Builder()
                                .filename("file1.txt")
                                .path("path")
                                .sizeInBytes(1L)
                                .build()));

        Map<String, List<FileData>> expectedMapByHash =
                Map.of( "hash",
                        List.of( new FileData.Builder()
                                .filename("file1.txt")
                                .path("path")
                                .sizeInBytes(1L)
                                .build()));

        mockGroupFilesSupport.when( () -> GroupFilesSupport.regroupFilesByHash( any(), any() ) ).thenReturn( expectedMapByHash );
        mockGroupFilesSupport.when( GroupFilesSupport::generateHashStringByChunkHashFn )
                             .thenReturn((ThrowingBiFunction<FileData, Long, String, Exception>) ( FileData unused1, Long unused2 ) -> "unused" );

        assertThat( GroupFilesSupport.regroupFilesByChunkHash( mapByAnything ) ).containsExactlyInAnyOrderEntriesOf( expectedMapByHash );

        mockGroupFilesSupport.verify( () -> GroupFilesSupport.generateHashStringByChunkHashFn(), times(1) );
    }


    @Test
    void whenRegroupByChunkHashIsGivenFileDataGroupedByHashShouldReturnFileDataGroupedByHash() throws Exception {

        Map<String, List<FileData>> mapByAnything =
                Map.of("hash1",
                        List.of( new FileData.Builder()
                                .filename("file1.txt")
                                .path("path")
                                .sizeInBytes(1L)
                                .build()));

        Map<String, List<FileData>> expectedMapByHash =
                Map.of("hash2",
                        List.of( new FileData.Builder()
                                .filename("file1.txt")
                                .path("path")
                                .sizeInBytes(1L)
                                .build()));

        mockGroupFilesSupport.when( () -> GroupFilesSupport.regroupFilesByHash( any(), any() ) ).thenReturn( expectedMapByHash );
        mockGroupFilesSupport.when( GroupFilesSupport::generateHashStringByChunkHashFn )
                             .thenReturn((ThrowingBiFunction<FileData, Long, String, Exception>) ( FileData unused1, Long unused2 ) -> "unused" );

        assertThat( GroupFilesSupport.regroupFilesByChunkHash( mapByAnything ) ).containsExactlyInAnyOrderEntriesOf( expectedMapByHash );

        mockGroupFilesSupport.verify( () -> GroupFilesSupport.generateHashStringByChunkHashFn(), times(1) );
    }


    @Test
    void whenRegroupFilesByFullHashIsGivenANullMapShouldReturnAnEmptyMap() {
        assertThat( GroupFilesSupport.regroupFilesByFullHash( null )).isEmpty();
    }


    @Test
    void whenRegroupFilesByFullHashIsGivenAnEmptyMapShouldReturnAnEmptyMap() {
        assertThat( GroupFilesSupport.regroupFilesByFullHash( Map.of() )).isEmpty();
    }


    @Test
    void whenAFileOperationThrowsAnIOExceptionInRegroupFilesByFullHashShouldReturnAnEmptyMap() {

        mockFiles.when( () -> Files.newInputStream( any() ) ).thenThrow( new IOException() );

        var fd = new FileData.Builder()
                .filename("file1")
                .path("/path/to/file1.txt")
                .sizeInBytes(1L)
                .build();

        assertThat( GroupFilesSupport.regroupFilesByFullHash( Map.of( Long.valueOf( 1L ), List.of( fd ) ) ) ).isEmpty();
    }


    @Test
    void whenMessageDigestThrowsANoSuchAlgorithmExceptionInRegroupFilesByFullHashShouldReturnAnEmptyMap() {

        mockDigest.when( () -> MessageDigest.getInstance( any() ) ).thenThrow( new NoSuchAlgorithmException() );

        var fd = new FileData.Builder()
                .filename("file1")
                .path("/path/to/file1.txt")
                .sizeInBytes(1L)
                .build();

        assertThat( GroupFilesSupport.regroupFilesByFullHash( Map.of( Long.valueOf( 1L ), List.of( fd ) ) ) ).isEmpty();
    }


    @Test
    void whenRegroupByFullHashIsGivenFileDataGroupedByAnythingShouldReturnFileDataGroupedByHash() {

        Map<Long, List<FileData>> mapByAnything =
                Map.of( 1L,
                        List.of( new FileData.Builder()
                                .filename("file1.txt")
                                .path("path")
                                .sizeInBytes(1L)
                                .build()));

        Map<String, List<FileData>> expectedMapByHash =
                Map.of( "hash",
                        List.of( new FileData.Builder()
                                .filename("file1.txt")
                                .path("path")
                                .sizeInBytes(1L)
                                .build()));

        mockGroupFilesSupport.when( () -> GroupFilesSupport.regroupFilesByHash( any(), any() ) ).thenReturn( expectedMapByHash );
        mockGroupFilesSupport.when( GroupFilesSupport::generateHashStringByFullHashFn )
                             .thenReturn((ThrowingBiFunction<FileData, Long, String, Exception>) ( FileData unused1, Long unused2 ) -> "unused" );

        assertThat( GroupFilesSupport.regroupFilesByFullHash( mapByAnything ) ).containsExactlyInAnyOrderEntriesOf( expectedMapByHash );

        mockGroupFilesSupport.verify( () -> GroupFilesSupport.generateHashStringByFullHashFn(), times(1) );
    }


    @Test
    void whenRegroupByFullHashIsGivenFileDataGroupedByHashShouldReturnFileDataGroupedByHash() throws Exception {

        Map<String, List<FileData>> mapByAnything =
                Map.of("hash1",
                        List.of( new FileData.Builder()
                                .filename("file1.txt")
                                .path("path")
                                .sizeInBytes(1L)
                                .build()));

        Map<String, List<FileData>> expectedMapByHash =
                Map.of("hash2",
                        List.of( new FileData.Builder()
                                .filename("file1.txt")
                                .path("path")
                                .sizeInBytes(1L)
                                .build()));

        mockGroupFilesSupport.when( () -> GroupFilesSupport.regroupFilesByHash( any(), any() ) ).thenReturn( expectedMapByHash );
        mockGroupFilesSupport.when( GroupFilesSupport::generateHashStringByFullHashFn )
                             .thenReturn((ThrowingBiFunction<FileData, Long, String, Exception>) ( FileData unused1, Long unused2 ) -> "unused" );

        assertThat( GroupFilesSupport.regroupFilesByFullHash( mapByAnything ) ).containsExactlyInAnyOrderEntriesOf( expectedMapByHash );

        mockGroupFilesSupport.verify( () -> GroupFilesSupport.generateHashStringByFullHashFn(), times(1) );
    }
}
