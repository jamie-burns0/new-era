package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.assertj.core.groups.Tuple.tuple;

import java.io.ByteArrayInputStream;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import me.jamieburns.data.FileData;

public class GroupFilesByChunkHashTest {

    MockedStatic<Files> mockFiles;

    MessageDigest mockDigest;
    FileStore mockFileStore;

    @BeforeEach
    void before() {

        mockDigest = mock( MessageDigest.class );
        mockFileStore = mock( FileStore.class );

        mockFiles = mockStatic( Files.class, CALLS_REAL_METHODS );
    }

    @AfterEach
    void after() {
        mockFiles.close();
    }
    

    @Test
    void shouldGroupFilesByChunkHash() throws Exception {

        var identicalContent = "identical content".getBytes();
        var identicalHash = "15bbe85aac4518db7da507997bd8b9baa07ddea5d0a08d098f85f1bf08c02521";
        var identicalSizeInBytes = 17L;

        var uniqueContent = "unique content".getBytes();
        var uniqueHash = "dbdb38c762fee1875e3ae6a0e894678a62425cf3897068ad03b47e49ade85b0a";
        var uniqueSizeInBytes = 14L;

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

        when(mockFileStore.getBlockSize()).thenReturn(4096L);

        mockFiles.when( () -> Files.getFileStore( any() )).thenReturn( mockFileStore );
        mockFiles.when( () -> Files.newInputStream(Paths.get(fd1.path()))).thenReturn(new ByteArrayInputStream(identicalContent));
        mockFiles.when( () -> Files.newInputStream(Paths.get(fd2.path()))).thenReturn(new ByteArrayInputStream(uniqueContent));
        mockFiles.when( () -> Files.newInputStream(Paths.get(fd3.path()))).thenReturn(new ByteArrayInputStream(identicalContent));

        var map = new GroupFilesByChunkHash().groupFiles(fileDataList);

        assertThat(map).isNotEmpty();

        assertThat( map ).containsKey( identicalHash );
        assertThat( map ).extractingByKey( identicalHash, as(InstanceOfAssertFactories.LIST)).hasSize( 2 );
        assertThat( map.get( identicalHash ) )
            .map(FileData::filename, FileData::chunkHash, FileData::fullHash)
            .containsOnly(
                tuple( fd1Filename, Optional.of(identicalHash), Optional.empty() ),
                tuple( fd3Filename, Optional.of(identicalHash), Optional.empty() )
            );

        assertThat( map ).containsKey( uniqueHash );
        assertThat( map ).extractingByKey( uniqueHash, as(InstanceOfAssertFactories.LIST)).hasSize( 1 );

        assertThat( map.get( uniqueHash ) )
            .map(FileData::filename, FileData::chunkHash, FileData::fullHash)
            .containsOnly(
                tuple( fd2Filename, Optional.of(uniqueHash), Optional.empty() )
            );
    }
}
