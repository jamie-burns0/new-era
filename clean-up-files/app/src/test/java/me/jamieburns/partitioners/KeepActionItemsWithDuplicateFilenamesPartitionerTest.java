package me.jamieburns.partitioners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.KeepAction;
import me.jamieburns.data.FileData;

public class KeepActionItemsWithDuplicateFilenamesPartitionerTest {

    @Test
    void whenArgumentIsNullOrEmpty_partitionOnKeepActionItemsWithDuplicateFilenames_ReturnsAnEmptyPartitionResult() {

        assertThat( KeepActionItemsWithDuplicateFilenamesPartitioner.partition( null ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));

        assertThat( KeepActionItemsWithDuplicateFilenamesPartitioner.partition( List.of() ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));
    }


    @Test
    void whenArgumentContainsOnlyKeepActionItemsWithDuplicateFilenames_partitionOnKeepActionItemsWithDuplicateFilenames_ReturnsAPartitionResultWithAnEmptyNegativeListAndAllItemsInThePositiveList() {

        var duplicateFilename = "duplicatefilename";

        var fd1 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path2")
                .sizeInBytes(2L)
                .build();

        var fd3 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path3")
                .sizeInBytes(3L)
                .build();

        List<Action<FileData>> keepActionList = Stream.of( fd1, fd2, fd3 )
                .map( KeepAction::new )
                .collect( Collectors.toList() );

        var result = KeepActionItemsWithDuplicateFilenamesPartitioner.partition( keepActionList );

        assertThat( result.positivePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 )
                .map( Action::data )
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );

        assertThat( result.negativePartitionList() ).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlyKeepActionItemsWithUniqueFilenames_partitionOnKeepActionItemsWithDuplicateFilenames_ReturnsAPartitionResultWithAnEmptyPositiveListAndAllItemsInTheNegativeList() {

        var uniqueFilename1 = "filename1";
        var uniqueFilename2 = "filename2";
        var uniqueFilename3 = "filename3";

        var fd1 = new FileData.Builder()
                .filename(uniqueFilename1)
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileData.Builder()
                .filename(uniqueFilename2)
                .path("path2")
                .sizeInBytes(2L)
                .build();

        var fd3 = new FileData.Builder()
                .filename(uniqueFilename3)
                .path("path3")
                .sizeInBytes(3L)
                .build();

        List<Action<FileData>> keepActionList = Stream.of( fd1, fd2, fd3 )
                .map( KeepAction::new )
                .collect( Collectors.toList() );

        var result = KeepActionItemsWithDuplicateFilenamesPartitioner.partition( keepActionList );

        assertThat( result.positivePartitionList() ).isEmpty();

        assertThat( result.negativePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 )
                .map( Action::data )
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }


    @Test
    void whenArgumentContainsKeepActionItemsWithUniqueFilenamesAndKeepActionItemsWithDuplicateFilenames_partitionOnKeepActionItemsWithDuplicateFilenames_ReturnsAPartitionResultWithAllDuplicateItemsInThePositiveListAndAllUniqueItemsInTheNegativeList() {

        var duplicateFilename = "duplicatefilename";
        var uniqueFilename1 = "uniquefilename1";
        var uniqueFilename2 = "uniquefilename2";

        var fd1 = new FileData.Builder()
                .filename(uniqueFilename1)
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path2")
                .sizeInBytes(2L)
                .build();

        var fd3 = new FileData.Builder()
                .filename(uniqueFilename2)
                .path("path3")
                .sizeInBytes(3L)
                .build();

        var fd4 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path4")
                .sizeInBytes(4L)
                .build();

        List<Action<FileData>> keepActionList = Stream.of( fd1, fd2, fd3, fd4 )
                .map( KeepAction::new )
                .collect( Collectors.toList() );

        var result = KeepActionItemsWithDuplicateFilenamesPartitioner.partition( keepActionList );

        assertThat( result.positivePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .map( Action::data )
                .containsExactlyInAnyOrder( fd2, fd4 );

        assertThat( result.negativePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .map( Action::data )
                .containsExactlyInAnyOrder( fd1, fd3 );
    }
}
