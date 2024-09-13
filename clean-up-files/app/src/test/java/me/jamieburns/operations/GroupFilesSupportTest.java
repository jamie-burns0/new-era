package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;

import java.util.List;

public class GroupFilesSupportTest {

    @Test
    void whenArgumentIsNullOrEmpty_groupFilesByFilename_ReturnsAnEmptyMap() {
        assertThat( GroupFilesSupport.groupFilesByFilename(null)).isEmpty();
        assertThat( GroupFilesSupport.groupFilesByFilename(List.of())).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlyUniqueFilenames_groupFilesByFilename_ReturnsAllFilesInAMapOfSingleItemLists() {

        var filename1 = "file1";
        var filename2 = "file2";
        var filename3 = "file3";

        var fd1 = new FileData.Builder()
                .filename(filename1)
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename(filename2)
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var fd3 = new FileData.Builder()
                .filename(filename3)
                .path("path3")
                .sizeInBytes( 3L )
                .build();

        var fileList = List.of(fd1, fd2, fd3);

        var groupedFiles = GroupFilesSupport.groupFilesByFilename(fileList);

        assertThat( groupedFiles )
            .isNotNull()
            .isNotEmpty()
            .hasSize( 3 )
            .containsOnlyKeys( filename1, filename2, filename3 )
            .extractingByKeys( filename1, filename2, filename3 )
            .flatExtracting( list -> (List<FileData>) list )
            .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }


    @Test
    void whenArgumentContainsOnlyDuplicateFilenames_groupFilesByFilename_ReturnsAllFilesInAMapOfMultipleItemLists() {

        var duplicateFilename1 = "file1";
        var duplicateFilename2 = "file2";

        var fd1 = new FileData.Builder()
                .filename(duplicateFilename1)
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename(duplicateFilename2)
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var fd3 = new FileData.Builder()
                .filename(duplicateFilename1)
                .path("path3")
                .sizeInBytes( 3L )
                .build();

        var fd4 = new FileData.Builder()
                .filename(duplicateFilename2)
                .path("path4")
                .sizeInBytes( 4L )
                .build();

        var fileList = List.of(fd1, fd2, fd3, fd4);

        var groupedFiles = GroupFilesSupport.groupFilesByFilename(fileList);

        assertThat( groupedFiles )
            .isNotNull()
            .isNotEmpty()
            .hasSize( 2 )
            .containsOnlyKeys( duplicateFilename1, duplicateFilename2 )
            .extractingByKeys( duplicateFilename1, duplicateFilename2 )
            .flatExtracting( list -> (List<FileData>) list )
            .containsExactlyInAnyOrder( fd1, fd3, fd2, fd4 );
    }


    @Test
    void whenArgumentContainsBothUniqueAndDuplicateFilenames_groupFilesByFilename_ReturnsAllFilesInAMapOfEitherSingleItemsListsOrMultipleItemLists() {

        var uniqueFilename1 = "file1";
        var uniqueFilename2 = "file2";
        var duplicateFilename1 = "file3";
        var duplicateFilename2 = "file4";

        var fd1 = new FileData.Builder()
                .filename(uniqueFilename1)
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename(uniqueFilename2)
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var fd3 = new FileData.Builder()
                .filename(duplicateFilename1)
                .path("path3")
                .sizeInBytes( 3L )
                .build();

        var fd4 = new FileData.Builder()
                .filename(duplicateFilename1)
                .path("path4")
                .sizeInBytes( 4L )
                .build();

        var fd5 = new FileData.Builder()
                .filename(duplicateFilename2)
                .path("path5")
                .sizeInBytes( 5L )
                .build();

        var fd6 = new FileData.Builder()
                .filename(duplicateFilename2)
                .path("path6")
                .sizeInBytes( 6L )
                .build();

        var fileList = List.of(fd1, fd2, fd3, fd4, fd5, fd6);

        var groupedFiles = GroupFilesSupport.groupFilesByFilename(fileList);

        assertThat( groupedFiles )
            .isNotNull()
            .isNotEmpty()
            .hasSize( 4 )
            .containsOnlyKeys( uniqueFilename1, uniqueFilename2, duplicateFilename1, duplicateFilename2 )
            .extractingByKeys( uniqueFilename1, uniqueFilename2, duplicateFilename1, duplicateFilename2 )
            .flatExtracting( list -> (List<FileData>) list )
            .containsExactlyInAnyOrder( fd1, fd2, fd3, fd4, fd5, fd6 );
    }    
}
