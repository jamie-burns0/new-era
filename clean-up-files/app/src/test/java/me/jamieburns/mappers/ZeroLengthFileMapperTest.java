package me.jamieburns.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.RemoveAction;
import me.jamieburns.data.FileData;

public class ZeroLengthFileMapperTest {

        @Test
    void whenArgumentIsANullOrEmptyList_ZeroLengthFileMapper_ReturnsAnEmptyList() {
        assertThat( ZeroLengthFileMapper.toActionList( null )).isEmpty();
        assertThat( ZeroLengthFileMapper.toActionList( List.of() )).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlyItemsWithZeroLengthFiles_ZeroLengthFileMapper_ReturnsEachItemWrappedInARemoveAction() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( 0L )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( 0L )
                .build();

        assertThat( ZeroLengthFileMapper.toActionList( List.of( fd1, fd2 ) ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof RemoveAction)
                .map(Action::data)
                .containsExactlyInAnyOrder( fd1, fd2);
    }

    @Test
    void whenArgumentContainsOnlyItemsWithNonZeroLengthFiles_ZeroLengthFileMapper_ReturnsAnEmptyList() {

        var nonZeroLengthFile = 1L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( nonZeroLengthFile )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( nonZeroLengthFile )
                .build();

        assertThat( ZeroLengthFileMapper.toActionList( List.of( fd1, fd2 ) ) ) // List<Action<FileData>>
                .isNotNull()
                .isEmpty();
    }


    @Test
    void whenArgumentContainsItemsWithZeroLengthFilesAndItemsWithNonZeroLengthFiles_ZeroLengthFileMapper_ReturnsOnlyZeroLengthItemsWrappedInARemoveAction() {

        var zeroLengthFile = 0L;
        var nonZeroLengthFile = 1L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( zeroLengthFile )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( nonZeroLengthFile )
                .build();

        var fd3 = new FileData.Builder()
                .filename("file3")
                .path("path3")
                .sizeInBytes( zeroLengthFile )
                .build();

        assertThat( ZeroLengthFileMapper.toActionList( List.of( fd1, fd2, fd3 ) ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof RemoveAction)
                .map(Action::data)
                .containsExactlyInAnyOrder( fd1, fd3 );
    }
}