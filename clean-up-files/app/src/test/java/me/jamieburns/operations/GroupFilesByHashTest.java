package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.security.MessageDigest;
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

public class GroupFilesByHashTest {

    MockedStatic<Files> mockFiles;

    MessageDigest mockDigest;
    FileStore mockFileStore;

    GroupFilesByHash mockGroupFilesByHash;

    @BeforeEach
    void before() {

        mockDigest = mock( MessageDigest.class );
        mockFileStore = mock( FileStore.class );

        mockFiles = mockStatic( Files.class, CALLS_REAL_METHODS );

        mockGroupFilesByHash = mock( 
                GroupFilesByHash.class,
                withSettings()
                    .useConstructor()
                    .defaultAnswer(CALLS_REAL_METHODS)
        );
    }

    @AfterEach
    void after() {
        mockFiles.close();
    }


    @Test
    void groupingNoFilesShouldReturnAnEmptyMap() {

        assertThat( mockGroupFilesByHash.groupFiles( null ) ).isEmpty();
        assertThat( mockGroupFilesByHash.groupFiles( List.<FileData>of() ) ).isEmpty();
    }


    @Test
    void whenFileOperationThrowsAnIOExceptionWhileGroupingFilesShouldReturnAnEmptyMap() {

        mockFiles.when( () -> Files.getFileStore( any() )).thenThrow( new IOException() );

        List<FileData> fdList = List.of(
            new FileData.Builder()
                .filename("file1.txt")
                .path("/path/to/file1.txt")
                .sizeInBytes(1024)
                .build()
        );

        assertThat( mockGroupFilesByHash.groupFiles( fdList ) ).isEmpty();
    }


    @Test
    void fileDataGroupedByAnythingWillBeRegroupedByHash() throws Exception {

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

        when( mockGroupFilesByHash.calculateChunkSize( any() ) ).thenReturn(4096L);
        when( mockGroupFilesByHash.generateHashString( any(), anyLong(), any())).thenReturn( hashString );
        when( mockGroupFilesByHash.newFileData( any(), any())).thenAnswer( i -> 
                new FileData.Builder()
                        .fromFileData( i.getArgument(0))
                        .chunkHash( hashString )
                        .build()
        );

        var regroupedMap = mockGroupFilesByHash.regroupFiles( fileDataGroupedByAnythingMap );        

        assertThat( regroupedMap ).hasSize( 1 );
        assertThat( regroupedMap ).containsOnlyKeys( hashString );
        assertThat( regroupedMap.get( hashString ) )
            .map(FileData::filename, FileData::chunkHash, FileData::fullHash)
            .containsOnly(
                tuple( filename1, Optional.of(hashString), Optional.empty() ),
                tuple( filename2, Optional.of(hashString), Optional.empty() )
            );
    }


    @Test
    void regroupingNoMapMapWillReturnAnEmptyMap() {
        assertThat( mockGroupFilesByHash.regroupFiles(null)).isEmpty();
        assertThat( mockGroupFilesByHash.regroupFiles( Map.of() )).isEmpty();
    }


    @Test
    void whenLargestFileIsLessThanBlockSizeChunkSizeWillBeBlockSize() throws Exception {

        var blockSize = 4096L;
        var largestFilesSize = 2048;

        FileData fd1 = new FileData.Builder()
                .filename("file1.txt")
                .path("/path/to/file1.txt")
                .sizeInBytes(largestFilesSize)
                .build();

        FileData fd2 = new FileData.Builder()
                .filename("file2.txt")
                .path("/path/to/file2.txt")
                .sizeInBytes(1024)
                .build();

        List<FileData> fdList = List.of(fd1, fd2);

        when(mockFileStore.getBlockSize()).thenReturn(blockSize);

        mockFiles.when( () -> Files.getFileStore( any() )).thenReturn( mockFileStore );

        assertThat( new GroupFilesByChunkHash().calculateChunkSize( fdList ) ).isEqualTo( blockSize );
    }


    @Test
    void whenLargestFileIsEqualToBlockSizeChunkSizeWillBeBlockSize() throws Exception {

        var blockSize = 4096L;
        var largestFilesSize = (int)blockSize;

        FileData fd1 = new FileData.Builder()
                .filename("file1.txt")
                .path("/path/to/file1.txt")
                .sizeInBytes(largestFilesSize)
                .build();

        FileData fd2 = new FileData.Builder()
                .filename("file2.txt")
                .path("/path/to/file2.txt")
                .sizeInBytes(1024)
                .build();

        List<FileData> fdList = List.of(fd1, fd2);

        when(mockFileStore.getBlockSize()).thenReturn(blockSize);

        mockFiles.when( () -> Files.getFileStore( any() )).thenReturn( mockFileStore );

        assertThat( new GroupFilesByChunkHash().calculateChunkSize( fdList ) ).isEqualTo( blockSize );
    }


    @Test
    void whenLargestFileIsGreaterThanBlockSizeChunkSizeWillBeAMultipleOfBlockSize() throws Exception {

        var expectedChunkSize = 24694784L;

        var blockSize = 4096L;
        var largestFilesSize = 123456789;

        FileData fd1 = new FileData.Builder()
                .filename("file1.txt")
                .path("/path/to/file1.txt")
                .sizeInBytes(largestFilesSize)
                .build();

        FileData fd2 = new FileData.Builder()
                .filename("file2.txt")
                .path("/path/to/file2.txt")
                .sizeInBytes(1024)
                .build();

        List<FileData> fdList = List.of(fd1, fd2);

        when(mockFileStore.getBlockSize()).thenReturn(blockSize);

        mockFiles.when( () -> Files.getFileStore( any() )).thenReturn( mockFileStore );

        var calculatedChunkSize = new GroupFilesByChunkHash().calculateChunkSize( fdList );

        assertThat( calculatedChunkSize ).isEqualTo( expectedChunkSize );
        assertThat( calculatedChunkSize % blockSize ).isEqualTo(0);
    }


    @Test
    void whenNoLargestFileChunkSizeWillBeBlockSize() throws Exception {

        var blockSize = 4096L;

        List<FileData> fdList = List.of();

        when(mockFileStore.getBlockSize()).thenReturn(blockSize);

        mockFiles.when( () -> Files.getFileStore( any() )).thenReturn( mockFileStore );

        assertThat( new GroupFilesByChunkHash().calculateChunkSize( fdList ) ).isEqualTo( blockSize );
    }

    @Test
    void when20PercentOfLargestFileIsLessThanOneChunkSizeWillBeBlockSize() throws Exception {

        var blockSize = 4096L;
        var largestFilesSize = 4;

        FileData fd1 = new FileData.Builder()
                .filename("file1.txt")
                .path("/path/to/file1.txt")
                .sizeInBytes(largestFilesSize)
                .build();

        FileData fd2 = new FileData.Builder()
                .filename("file2.txt")
                .path("/path/to/file2.txt")
                .sizeInBytes(1)
                .build();

        List<FileData> fdList = List.of(fd1, fd2);

        when(mockFileStore.getBlockSize()).thenReturn(blockSize);

        mockFiles.when( () -> Files.getFileStore( any() )).thenReturn( mockFileStore );

        assertThat( new GroupFilesByChunkHash().calculateChunkSize( fdList ) ).isEqualTo( blockSize );
    }


    @Test
    void whenLargestFileIsZeroBytesChunkSizeWillBeBlockSize() throws Exception {

        var blockSize = 4096L;
        var largestFilesSize = 0;

        FileData fd1 = new FileData.Builder()
                .filename("file1.txt")
                .path("/path/to/file1.txt")
                .sizeInBytes(largestFilesSize)
                .build();

        FileData fd2 = new FileData.Builder()
                .filename("file2.txt")
                .path("/path/to/file2.txt")
                .sizeInBytes(largestFilesSize)
                .build();

        List<FileData> fdList = List.of(fd1, fd2);

        when(mockFileStore.getBlockSize()).thenReturn(blockSize);

        mockFiles.when( () -> Files.getFileStore( any() )).thenReturn( mockFileStore );

        assertThat( new GroupFilesByChunkHash().calculateChunkSize( fdList ) ).isEqualTo( blockSize );
    }

}
