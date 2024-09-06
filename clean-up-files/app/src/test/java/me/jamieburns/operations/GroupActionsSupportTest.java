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
    void whenArgumentContainsANullOrEmptyList_PartitionOnKeepActionsByFilename_ReturnsAMapOfEmptyMaps() {
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
    void whenArgumentContainsOnlyKeepActionItemsWithTheSameFilename_PartitionOnKeepActionsByFilename_ReturnsAMapWithOnlyTheKeepActions() {

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
    void whenArgumentContainsOnlyKeepActionItemsWithTheDifferentFilenames_PartitionOnKeepActionsByFilename_ReturnsAMapWithOnlyTheKeepActions() {

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
    void whenArgumentDoesNotContainAnyKeepActionItems_PartitionOnKeepActionsByFilename_ReturnsAMapWithNoKeepActionAndAllOtherActions() {

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
    void whenArgumentDoesKeepActionItemsAndOtherActionItems_PartitionOnKeepActionsByFilename_ReturnsAMapWithKeepActionAndAllOtherActions() {

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
}
