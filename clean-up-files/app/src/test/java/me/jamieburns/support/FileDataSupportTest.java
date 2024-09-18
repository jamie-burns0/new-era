package me.jamieburns.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import me.jamieburns.data.FileData;
import me.jamieburns.data.FileDataBuilder;
import me.jamieburns.support.FileDataSupport.FunctionArgs;

public class FileDataSupportTest {

    MockedStatic<MessageDigest> mockDigest;
    MockedStatic<Files> mockFiles;
    MockedStatic<Paths> mockPaths;
    MockedStatic<GroupFilesSupport> mockGroupFilesSupport;

    FileStore mockFileStore;

    @BeforeEach
    void before() {

        mockDigest = mockStatic( MessageDigest.class, CALLS_REAL_METHODS );
        mockFiles = mockStatic( Files.class, CALLS_REAL_METHODS );
        mockPaths = mockStatic( Paths.class, CALLS_REAL_METHODS );
        mockGroupFilesSupport = mockStatic( GroupFilesSupport.class, CALLS_REAL_METHODS );

        mockFileStore = mock( FileStore.class );
    }

    @AfterEach
    void after() {
        mockDigest.close();
        mockFiles.close();
        mockPaths.close();
        mockGroupFilesSupport.close();
    }


    @Test
    void generateHashStringByChunkHashFnWillReturnSameHashForIdenticalFiles() throws Exception {

        var blockSize = 4096L;

        var identicalContent = "identical content.......".getBytes();
        var identicalHash = "b06526e3f33bca3e28f58ed4064a5797f37c2549d7399a030c45a9c52ea74c7c";

        var uniqueContent = "unique content..".getBytes();
        var uniqueHash = "a1c186b318088da37f9031b6e23b71a6efe85b63eca8123338347119d5193211";

        var fd1 = new FileDataBuilder()
                .filename( "name1" )
                .path( "path1" )
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileDataBuilder()
                .filename( "name2" )
                .path( "path2" )
                .sizeInBytes( 2L )
                .build();

        var fd3 = new FileDataBuilder()
                .filename( "name3" )
                .path( "path3" )
                .sizeInBytes( 3L )
                .build();

        var mockPath1 = mock(Path.class, CALLS_REAL_METHODS );
        var mockPath2 = mock(Path.class, CALLS_REAL_METHODS );
        var mockPath3 = mock(Path.class, CALLS_REAL_METHODS );

        mockPaths.when( () -> Paths.get( fd1.path() ) ).thenReturn( mockPath1 );
        mockPaths.when( () -> Paths.get( fd2.path() ) ).thenReturn( mockPath2 );
        mockPaths.when( () -> Paths.get( fd3.path() ) ).thenReturn( mockPath3 );

        mockFiles.when( () -> Files.newInputStream( eq(mockPath1), any() ) ).thenReturn( new ByteArrayInputStream(identicalContent) );
        mockFiles.when( () -> Files.newInputStream( eq(mockPath2), any() ) ).thenReturn( new ByteArrayInputStream(uniqueContent) );
        mockFiles.when( () -> Files.newInputStream( eq(mockPath3), any() ) ).thenReturn( new ByteArrayInputStream(identicalContent) );

        var digest = FileDataSupport.newDigest();

        List<FunctionArgs> fnArgsList = List.of(
                new FunctionArgs( fd1, blockSize, digest ),
                new FunctionArgs( fd2, blockSize, digest ),
                new FunctionArgs( fd3, blockSize, digest )
        );
        var hashList = new ArrayList<String>();

        for( var args : fnArgsList ) {
            hashList.add( FileDataSupport.generateHashStringByChunkHashFn().apply( args ) );
        }

        assertThat(hashList)
                .isNotEmpty()
                .containsExactlyInAnyOrder( identicalHash, identicalHash, uniqueHash );
    }


    @Test
    void whenFileOperationInGenerateHashStringByChunkHashFnThrowsAnException() throws Exception {

        var blockSize = 4096L;

        var fd = new FileDataBuilder()
                .filename( "name1" )
                .path( "path1" )
                .sizeInBytes( 1L )
                .build();

        mockFiles.when( () -> Files.newInputStream( any() ) ).thenThrow( new IOException() );

        var mockDigest = mock(MessageDigest.class, CALLS_REAL_METHODS);

        var args = new FunctionArgs( fd, blockSize, mockDigest );

        assertThatThrownBy( () -> FileDataSupport.generateHashStringByChunkHashFn().apply( args ) )
                .isInstanceOf( IOException.class );
    }


    @Test
    void generateHashStringByFullHashFnWillReturnSameHashForIdenticalFiles() throws Exception {

        var blockSize = 8L;

        var identicalContent = "identical content.......".getBytes();
        var identicalHash = "b06526e3f33bca3e28f58ed4064a5797f37c2549d7399a030c45a9c52ea74c7c";

        var uniqueContent = "unique content..".getBytes();
        var uniqueHash = "a1c186b318088da37f9031b6e23b71a6efe85b63eca8123338347119d5193211";

        var fd1 = new FileDataBuilder()
                .filename( "name1" )
                .path( "path1" )
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileDataBuilder()
                .filename( "name2" )
                .path( "path2" )
                .sizeInBytes( 2L )
                .build();

        var fd3 = new FileDataBuilder()
                .filename( "name3" )
                .path( "path3" )
                .sizeInBytes( 3L )
                .build();

        var mockPath1 = mock(Path.class, CALLS_REAL_METHODS );
        var mockPath2 = mock(Path.class, CALLS_REAL_METHODS );
        var mockPath3 = mock(Path.class, CALLS_REAL_METHODS );

        mockPaths.when( () -> Paths.get( fd1.path() ) ).thenReturn( mockPath1 );
        mockPaths.when( () -> Paths.get( fd2.path() ) ).thenReturn( mockPath2 );
        mockPaths.when( () -> Paths.get( fd3.path() ) ).thenReturn( mockPath3 );

        mockFiles.when( () -> Files.readAllBytes( mockPath1 ) ).thenReturn( identicalContent );
        mockFiles.when( () -> Files.readAllBytes( mockPath2 ) ).thenReturn( uniqueContent);
        mockFiles.when( () -> Files.readAllBytes( mockPath3 ) ).thenReturn( identicalContent);

        var digest = FileDataSupport.newDigest();

        List<FunctionArgs> fnArgsList = List.of(
                new FunctionArgs( fd1, blockSize, digest ),
                new FunctionArgs( fd2, blockSize, digest ),
                new FunctionArgs( fd3, blockSize, digest )
        );

        var hashList = new ArrayList<String>();

        for( var args : fnArgsList ) {
            hashList.add( FileDataSupport.generateHashStringByFullHashFn().apply( args ) );
        }

        assertThat(hashList)
                .isNotEmpty()
                .containsExactlyInAnyOrder( identicalHash, identicalHash, uniqueHash );
    }


    @Test
    void whenFileOperationInGenerateHashStringByFullHashFnThrowsAnIOException() throws Exception {

        var blockSize = 4096L;

        var fd = new FileDataBuilder()
                .filename( "name1" )
                .path( "path1" )
                .sizeInBytes( 1L )
                .build();

        mockFiles.when( () -> Files.newInputStream( any() ) ).thenThrow( new IOException() );

        var mockDigest = mock(MessageDigest.class, CALLS_REAL_METHODS);

        var args = new FunctionArgs( fd, blockSize, mockDigest );

        assertThatThrownBy( () -> FileDataSupport.generateHashStringByFullHashFn().apply( args ) )
                .isInstanceOf( IOException.class );
    }


    @Test
    void whenMessageDigestOperationThrowsANoSuchAlgorithmException_rebuildWithChunkHash_ReturnsUnmodifiedFileData() {

        mockDigest.when( () -> MessageDigest.getInstance( any() ) ).thenThrow( new NoSuchAlgorithmException() );

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(2L)
                .build();


        assertThat( FileDataSupport.rebuildWithChunkHash( List.of( fd1, fd2 ) ) )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2 );
    }


    @Test
    void whenFileOperationThrowsAnIOException_rebuildWithChunkHash_ReturnsUnmodifiedFileData() throws Exception {

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(2L)
                .build();

        var fileDataList = List.of( fd1, fd2 );

        var mockFileStore = mock(FileStore.class, CALLS_REAL_METHODS );
        doReturn( 4096L ).when( mockFileStore ).getBlockSize();

        mockFiles.when( () -> Files.getFileStore( any() ) ).thenReturn( mockFileStore );
        mockFiles.when( () -> Files.newInputStream( any() ) ).thenThrow( new IOException() );

        assertThat( FileDataSupport.rebuildWithChunkHash( fileDataList ) )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2 );
    }


    @Test
    void whenAFileOperationForAFileDataThrowsAnIOException_rebuildWithChunkHash_ReturnsUnmodifiedFileDataForEachFileDataWhichCausedTheExceptionAndModifiedFileDataForAllOtherFileDataItems() throws Exception {

        var exceptionThrowingPath = "exceptionPath";

        var safePath = "safePath";
        var safePathContent = "safe path content.......".getBytes();
        var safePathHash = "80e8049d8bb6c30616fd261066a8751a64726f978791954bf85168c5a035aa10";

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path(exceptionThrowingPath)
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path(safePath)
                .sizeInBytes(2L)
                .build();

        var fileDataList = List.of( fd1, fd2 );

        var mockFileStore = mock(FileStore.class, CALLS_REAL_METHODS );
        doReturn( 4096L ).when( mockFileStore ).getBlockSize();

        var mockExceptionThrowingPathPath = mock(Path.class, CALLS_REAL_METHODS );
        var mockSafePathPath = mock(Path.class, CALLS_REAL_METHODS );

        mockPaths.when( () -> Paths.get( exceptionThrowingPath ) ).thenReturn( mockExceptionThrowingPathPath );
        mockPaths.when( () -> Paths.get( safePath ) ).thenReturn( mockSafePathPath );

        var safePathInputStream = new ByteArrayInputStream( safePathContent );

        mockFiles.when( () -> Files.getFileStore( any() ) ).thenReturn( mockFileStore );

        mockFiles.when( () -> Files.newInputStream( eq(mockExceptionThrowingPathPath), any() ) ).thenThrow( new IOException() );
        mockFiles.when( () -> Files.newInputStream( eq(mockSafePathPath), any() ) ).thenReturn( safePathInputStream );

        assertThat( FileDataSupport.rebuildWithChunkHash( fileDataList ) )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .contains( fd1 )
                .doesNotContain( fd2 )
                .filteredOn( fd -> fd != fd1 )
                .extracting( FileData::hash )
                .containsExactlyInAnyOrder( Optional.of( safePathHash ) );
    }


    @Test
    void whenNoExceptionsAreThrown_rebuildWithChunkHash_ReturnsOnlyModifiedFileDataItems() throws Exception {

        var fd1Path = "fd1Path";
        var fd1Content = "fd1 content.......".getBytes();
        var fd1Hash = "b6313ff37031a2dfd91fe504e131c9114014f3bee6e3a7f733edc67b1593b09a";

        var fd2Path = "fd2Path";
        var fd2Content = "fd2 content.......".getBytes();
        var fd2Hash = "2200c6406acc7f6c33f31a62c6518a7e68be533567f42b136e20f6e579bb3d74";

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path(fd1Path)
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path(fd2Path)
                .sizeInBytes(2L)
                .build();

        var fileDataList = List.of( fd1, fd2 );

        var mockFileStore = mock(FileStore.class, CALLS_REAL_METHODS );
        doReturn( 4096L ).when( mockFileStore ).getBlockSize();
        mockFiles.when( () -> Files.getFileStore( any() ) ).thenReturn( mockFileStore );

        var mockPath1 = mock(Path.class, CALLS_REAL_METHODS );
        var mockPath2 = mock(Path.class, CALLS_REAL_METHODS );

        mockPaths.when( () -> Paths.get( fd1Path ) ).thenReturn( mockPath1 );
        mockPaths.when( () -> Paths.get( fd2Path ) ).thenReturn( mockPath2 );

        mockFiles.when( () -> Files.newInputStream( eq(mockPath1), any() ) ).thenReturn( new ByteArrayInputStream( fd1Content ) );
        mockFiles.when( () -> Files.newInputStream( eq(mockPath2), any() ) ).thenReturn( new ByteArrayInputStream( fd2Content ) );

        assertThat( FileDataSupport.rebuildWithChunkHash( fileDataList ) )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .doesNotContain( fd1, fd2 )
                .extracting( FileData::hash )
                .containsExactlyInAnyOrder( Optional.of( fd1Hash ), Optional.of( fd2Hash ) );
    }


    @Test
    void whenMessageDigestOperationThrowsANoSuchAlgorithmException_rebuildWithFullHash_ReturnsUnmodifiedFileData() {

        mockDigest.when( () -> MessageDigest.getInstance( any() ) ).thenThrow( new NoSuchAlgorithmException() );

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(2L)
                .build();


        assertThat( FileDataSupport.rebuildWithFullHash( List.of( fd1, fd2 ) ) )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2 );
    }


    @Test
    void whenFileOperationThrowsAnIOException_rebuildWithFullHash_ReturnsUnmodifiedFileData() throws Exception {

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(2L)
                .build();

        var fileDataList = List.of( fd1, fd2 );

        var mockFileStore = mock(FileStore.class, CALLS_REAL_METHODS );
        doReturn( 4096L ).when( mockFileStore ).getBlockSize();

        mockFiles.when( () -> Files.getFileStore( any() ) ).thenReturn( mockFileStore );
        mockFiles.when( () -> Files.newInputStream( any() ) ).thenThrow( new IOException() );

        assertThat( FileDataSupport.rebuildWithFullHash( fileDataList ) )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2 );
    }


    @Test
    void whenAFileOperationForAFileDataThrowsAnIOException_rebuildWithFullHash_ReturnsUnmodifiedFileDataForEachFileDataWhichCausedTheExceptionAndModifiedFileDataForAllOtherFileDataItems() throws Exception {

        var exceptionThrowingPath = "exceptionPath";

        var safePath = "safePath";
        var safePathContent = "safe path content.......".getBytes();
        var safePathHash = "80e8049d8bb6c30616fd261066a8751a64726f978791954bf85168c5a035aa10";

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path(exceptionThrowingPath)
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path(safePath)
                .sizeInBytes(2L)
                .build();

        var fileDataList = List.of( fd1, fd2 );

        var mockFileStore = mock(FileStore.class, CALLS_REAL_METHODS );
        doReturn( 4096L ).when( mockFileStore ).getBlockSize();

        var mockExceptionThrowingPathPath = mock(Path.class, CALLS_REAL_METHODS );
        var mockSafePathPath = mock(Path.class, CALLS_REAL_METHODS );

        mockPaths.when( () -> Paths.get( exceptionThrowingPath ) ).thenReturn( mockExceptionThrowingPathPath );
        mockPaths.when( () -> Paths.get( safePath ) ).thenReturn( mockSafePathPath );

        mockFiles.when( () -> Files.getFileStore( any() ) ).thenReturn( mockFileStore );

        mockFiles.when( () -> Files.readAllBytes( mockExceptionThrowingPathPath ) ).thenThrow( new IOException() );
        mockFiles.when( () -> Files.readAllBytes( mockSafePathPath ) ).thenReturn( safePathContent );

        assertThat( FileDataSupport.rebuildWithFullHash( fileDataList ) )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .contains( fd1 )
                .doesNotContain( fd2 )
                .filteredOn( fd -> fd != fd1 )
                .extracting( FileData::hash )
                .containsExactlyInAnyOrder( Optional.of( safePathHash ) );
    }


    @Test
    void whenNoExceptionsAreThrown_rebuildWithFullHash_ReturnsOnlyModifiedFileDataItems() throws Exception {

        var fd1Path = "fd1Path";
        var fd1Content = "fd1 content.......".getBytes();
        var fd1Hash = "b6313ff37031a2dfd91fe504e131c9114014f3bee6e3a7f733edc67b1593b09a";

        var fd2Path = "fd2Path";
        var fd2Content = "fd2 content.......".getBytes();
        var fd2Hash = "2200c6406acc7f6c33f31a62c6518a7e68be533567f42b136e20f6e579bb3d74";

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path(fd1Path)
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path(fd2Path)
                .sizeInBytes(2L)
                .build();

        var fileDataList = List.of( fd1, fd2 );

        var mockFileStore = mock(FileStore.class, CALLS_REAL_METHODS );
        doReturn( 4096L ).when( mockFileStore ).getBlockSize();

        mockFiles.when( () -> Files.getFileStore( any() ) ).thenReturn( mockFileStore );

        var mockPath1 = mock(Path.class, CALLS_REAL_METHODS );
        var mockPath2 = mock(Path.class, CALLS_REAL_METHODS );

        mockPaths.when( () -> Paths.get( fd1Path ) ).thenReturn( mockPath1 );
        mockPaths.when( () -> Paths.get( fd2Path ) ).thenReturn( mockPath2 );

        mockFiles.when( () -> Files.readAllBytes( mockPath1 ) ).thenReturn( fd1Content );
        mockFiles.when( () -> Files.readAllBytes( mockPath2 ) ).thenReturn( fd2Content );

        assertThat( FileDataSupport.rebuildWithFullHash( fileDataList ) )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .doesNotContain( fd1, fd2 )
                .extracting( FileData::hash )
                .containsExactlyInAnyOrder( Optional.of( fd1Hash ), Optional.of( fd2Hash ) );
    }
}
