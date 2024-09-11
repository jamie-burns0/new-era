package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;

public class GroupActionsSupportTest {

    @Test
    void whenArgumentContainsANullOrEmptyList_partitionOnKeepActionsByFilename_ReturnsAMapOfEmptyMaps() {
        assertThat( GroupActionsSupport.partitionOnKeepActionsByFilename(null) )
            .containsOnly(
                entry( Boolean.TRUE, Map.of() ),
                entry( Boolean.FALSE, Map.of() )
            );

        assertThat( GroupActionsSupport.partitionOnKeepActionsByFilename(List.of()) )
            .containsOnly(
                entry( Boolean.TRUE, Map.of() ),
                entry( Boolean.FALSE, Map.of() )
            );
    }


    @Test
    void whenArgumentContainsOnlyKeepActionItemsWithTheSameFilename_partitionOnKeepActionsByFilename_ReturnsAMapWithOnlyTheKeepActions() {

        var duplicateFilename = "fileD";

        var fd1 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var actionList = Stream.of(fd1, fd2 )
                        .map( KeepAction::new )
                        .map( a -> (Action<FileData>) a )
                        .collect(Collectors.toList());

        var partitionMap = GroupActionsSupport.partitionOnKeepActionsByFilename(actionList);

        assertThat( partitionMap.get(Boolean.TRUE) )
            .isNotNull()
            .isNotEmpty()
            .hasSize( 1 )
            .containsOnlyKeys( duplicateFilename )
            .extractingByKey( duplicateFilename, InstanceOfAssertFactories.iterable( KeepAction.class ) )
            .map( Action::data )
            .containsExactlyInAnyOrder( fd1, fd2 );

        assertThat( partitionMap.get(Boolean.FALSE) ).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlyKeepActionItemsWithTheDifferentFilenames_partitionOnKeepActionsByFilename_ReturnsAMapWithOnlyTheKeepActions() {

        var filename1 = "file1";
        var filename2 = "file2";

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

        List<Action<FileData>> actionList = Stream.of(fd1, fd2 )
                        .map( KeepAction::new )
                        .map( a -> (Action<FileData>) a )
                        .collect(Collectors.toList());

        var partitionMap = GroupActionsSupport.partitionOnKeepActionsByFilename(actionList);

        assertThat( partitionMap.get(Boolean.TRUE) )
            .isNotNull()
            .isNotEmpty()
            .hasSize( 2 )
            .containsOnlyKeys( filename1, filename2 )
            .extractingByKeys( filename1, filename2 )
            .flatExtracting( list -> (List<Action<FileData>>) list )
            .map( x -> (Action<FileData>) x)
            .map( Action::data )
            .containsExactlyInAnyOrder( fd1, fd2 );

        assertThat( partitionMap.get(Boolean.FALSE) ).isEmpty();
    }


    @Test
    void whenArgumentDoesNotContainAnyKeepActionItems_partitionOnKeepActionsByFilename_ReturnsAMapWithNoKeepActionAndAllOtherActions() {

        var filename1 = "file1";
        var filename2 = "file2";

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

        List<Action<FileData>> actionList = List.of(
                new KeepWithRenameAction<>( fd1 ),
                new RemoveAction<>( fd2 )
            );

        var partitionMap = GroupActionsSupport.partitionOnKeepActionsByFilename(actionList);

        assertThat( partitionMap.get(Boolean.TRUE) ).isEmpty();

        assertThat( partitionMap.get(Boolean.FALSE) )
            .isNotNull()
            .isNotEmpty()
            .hasSize( 2 )
            .containsOnlyKeys( filename1, filename2 )
            .extractingByKeys( filename1, filename2 )
            .flatExtracting( list -> (List<Action<FileData>>) list )
            .map( x -> (Action<FileData>) x)
            .map( Action::data )
            .containsExactlyInAnyOrder( fd1, fd2 );

        assertThat( partitionMap.get(Boolean.FALSE) )
            .values()
            .flatExtracting( list -> (List<Action<FileData>>) list )
            .map( x -> (Action<FileData>) x)
            .extracting( "class" )
            .containsExactlyInAnyOrder( KeepWithRenameAction.class, RemoveAction.class );
    }


    @Test
    void whenArgumentDoesKeepActionItemsAndOtherActionItems_partitionOnKeepActionsByFilename_ReturnsAMapWithKeepActionAndAllOtherActions() {

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

        List<Action<FileData>> actionList = List.of(
                new KeepWithRenameAction<>( fd1 ),
                new RemoveAction<>( fd2 ),
                new KeepAction<>( fd3 )
            );

        var partitionMap = GroupActionsSupport.partitionOnKeepActionsByFilename(actionList);

        assertThat( partitionMap.get(Boolean.TRUE) )
            .isNotNull()
            .isNotEmpty()
            .hasSize( 1 )
            .containsOnlyKeys( filename3 )
            .extractingByKeys( filename3 )
            .flatExtracting( list -> (List<Action<FileData>>) list )
            .map( x -> (Action<FileData>) x)
            .map( Action::data )
            .containsExactlyInAnyOrder( fd3 );

        assertThat( partitionMap.get(Boolean.TRUE) )
            .values()
            .flatExtracting( list -> (List<Action<FileData>>) list )
            .map( x -> (Action<FileData>) x)
            .extracting( "class" )
            .containsExactlyInAnyOrder( KeepAction.class );

        assertThat( partitionMap.get(Boolean.FALSE) )
            .isNotNull()
            .isNotEmpty()
            .hasSize( 2 )
            .containsOnlyKeys( filename1, filename2 )
            .extractingByKeys( filename1, filename2 )
            .flatExtracting( list -> (List<Action<FileData>>) list )
            .map( x -> (Action<FileData>) x)
            .map( Action::data )
            .containsExactlyInAnyOrder( fd1, fd2 );

        assertThat( partitionMap.get(Boolean.FALSE) )
            .values()
            .flatExtracting( list -> (List<Action<FileData>>) list )
            .map( x -> (Action<FileData>) x)
            .extracting( "class" )
            .containsExactlyInAnyOrder( KeepWithRenameAction.class, RemoveAction.class );
    }


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


    @Test
    void whenArgumentIsNullOrEmpty_partitionOnUniqueFileSizesWithDuplicateFilenames_ReturnsAPartitionResultWithEmptyValues() {
        var partitionResult = GroupFilesSupport.partitionOnUniqueFileSizesWithDuplicateFilenames(null);
        assertThat( partitionResult.positivePartitionList() ).isEmpty();
        assertThat( partitionResult.negativePartitionList() ).isEmpty();

        partitionResult = GroupFilesSupport.partitionOnUniqueFileSizesWithDuplicateFilenames(List.of());
        assertThat( partitionResult.positivePartitionList() ).isEmpty();
        assertThat( partitionResult.negativePartitionList() ).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlyItemsWithUniqueFileSizesAndUniqueFilenames_partitionOnUniqueFileSizesWithDuplicateFilenames_ReturnsAPartitionResultWithAnEmptyPositiveListAndAllItemsInTheNegativeList() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var fileList = List.of(fd1, fd2);

        var partitionResult = GroupFilesSupport.partitionOnUniqueFileSizesWithDuplicateFilenames(fileList);

        assertThat( partitionResult.positivePartitionList() ).isEmpty();

        assertThat( partitionResult.negativePartitionList() )
            .isNotNull()
            .isNotEmpty()
            .hasSize( 2 )
            .containsExactlyInAnyOrder( fd1, fd2 );
    }
}
