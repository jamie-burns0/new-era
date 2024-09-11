package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import java.util.List;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;
import me.jamieburns.operations.GroupFilesSupport.PartitionResult;

public class GroupFilesSupport_PartitionTest {

    @Test
    void whenArgumentIsNullOrEmpty_PartitionOnZeroLengthFileSize_ReturnsAnEmptyPartitionResult() {

        assertThat( GroupFilesSupport.partitionOnZeroLengthFileSize( (List<FileData>) null ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));

        assertThat( GroupFilesSupport.partitionOnZeroLengthFileSize( List.of() ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));
    }


    @Test
    void whenArgumentContainsOnlyAZeroLengthFileSizeList_PartitionOnZeroLengthFileSize_ReturnsAFullPositiveListAndAnEmptyNegativeList() {

        var zeroLengthFileSize = 0L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( zeroLengthFileSize )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( zeroLengthFileSize )
                .build();

        var list = List.of(fd1, fd2);

        var result = GroupFilesSupport.partitionOnZeroLengthFileSize( list );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2);

        assertThat( result.negativePartitionList() ).isEmpty();
    }


    @Test
    void whenArgumentContainsNoZeroLengthFileSizeList_PartitionOnZeroLengthFileSize_ReturnsAnEmptyPositiveListAndAFullNegativeList() {

        var nonZeroLengthFileSize = 1L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( nonZeroLengthFileSize )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( nonZeroLengthFileSize )
                .build();

        var list = List.of(fd1, fd2);

        var result = GroupFilesSupport.partitionOnZeroLengthFileSize( list );

        assertThat( result.positivePartitionList() ).isEmpty();

        assertThat( result.negativePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @Test
    void whenArgumentContainsBothZeroAndNonZeroLengthFileSizeLists_PartitionOnZeroLengthFileSize_ReturnsAPositiveListContainingAllZeroLengthFileItemsAndANegativeListContainingAllNonZeroLengthFileItems() {

        var zeroLengthFileSize = 0L;
        var nonZeroLengthFileSize = 1L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( zeroLengthFileSize )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( zeroLengthFileSize )
                .build();

        var fd3 = new FileData.Builder()
                .filename("file3")
                .path("path3")
                .sizeInBytes( nonZeroLengthFileSize )
                .build();

        var list = List.of(fd1, fd2, fd3);

        var result = GroupFilesSupport.partitionOnZeroLengthFileSize( list );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2 );

        assertThat( result.negativePartitionList() ) // Map<Long, List<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 1 )
                .containsExactlyInAnyOrder( fd3 );
    }


    @Test
    void whenArgumentIsNullOrEmpty_partitionOnUniqueFileSizes_ReturnsAnEmptyPartitionResult() {

        assertThat( GroupFilesSupport.partitionOnUniqueFileSizes( null ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));

        assertThat( GroupFilesSupport.partitionOnUniqueFileSizes( List.of() ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));
    }


    @Test
    void whenArgumentContainsOnlyItemsWithUniqueFileSizes_partitionOnUniqueFileSizes_ReturnsAPartitionResultWithAnEmptyNegativeListAndAllItemsInThePositiveList() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(2L)
                .build();

        var fd3 = new FileData.Builder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(3L)
                .build();

        var result = GroupFilesSupport.partitionOnUniqueFileSizes( List.of(fd1, fd2, fd3) );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 )
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );

        assertThat( result.negativePartitionList() ).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlyItemsWithDuplicateFileSizes_partitionOnUniqueFileSizes_ReturnsAPartitionResultWithAnEmptyPositiveListAndAllItemsInTheNegativeList() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(1L)
                .build();

        var fd3 = new FileData.Builder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(1L)
                .build();

        var result = GroupFilesSupport.partitionOnUniqueFileSizes( List.of(fd1, fd2, fd3) );

        assertThat( result.positivePartitionList() ).isEmpty();

        assertThat( result.negativePartitionList() ) // List<FileData>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 )
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }


    @Test
    void whenArgumentContainsItemsWithUniqueFileSizesAndItemsWithDuplicateFileSizes_partitionOnUniqueFileSizes_ReturnsAPartitionResultWithAllUniqueItemsInThePositiveListAndAllDuplicateItemsInTheNegativeList() {

        var uniqueFileSize1 = 1L;
        var uniqueFileSize2 = 2L;
        var duplicateFileSize = 3L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(uniqueFileSize1)
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(duplicateFileSize)
                .build();

        var fd3 = new FileData.Builder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(uniqueFileSize2)
                .build();

        var fd4 = new FileData.Builder()
                .filename("file4")
                .path("path4")
                .sizeInBytes(duplicateFileSize)
                .build();

                var result = GroupFilesSupport.partitionOnUniqueFileSizes( List.of(fd1, fd2, fd3, fd4) );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd3 );

        assertThat( result.negativePartitionList() ) // List<FileData>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd2, fd4 );
    }
}