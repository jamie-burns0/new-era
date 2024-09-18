package me.jamieburns.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.KeepAction;
import me.jamieburns.actions.RemoveAction;
import me.jamieburns.data.FileData;
import me.jamieburns.data.FileDataBuilder;

public class DuplicateFilesMapperTest {

    @Test
    void whenArgumentIsNullOrEmpty_DuplicateFilesMapper_ReturnsAnEmptyList() {
        assertThat(DuplicateFilesMapper.toActionList(null)).isEmpty();
        assertThat(DuplicateFilesMapper.toActionList(Map.of())).isEmpty();
    }

    @Test
    void whenArgumentContainsOnlyDuplicateItems_DuplicateFilesMapper_ReturnsAListWithOneDuplicateItemWrappedInAKeepActionAndAllOtherDuplicateItemsWrappedInARemoveAction() {

        var duplicateSize1 = 1L;
        var duplicateHash1 = "duplicatehash1";
        var duplicateSize2 = 2L;
        var duplicateHash2 = "duplicatehash2";

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(duplicateSize1)
                .hash(duplicateHash1)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(duplicateSize1)
                .hash(duplicateHash1)
                .build();

        var fd3 = new FileDataBuilder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(duplicateSize2)
                .hash(duplicateHash2)
                .build();

        var fd4 = new FileDataBuilder()
                .filename("file4")
                .path("path4")
                .sizeInBytes(duplicateSize2)
                .hash(duplicateHash2)
                .build();

        var map = Stream.of( fd1, fd2, fd3, fd4 )
                .collect( Collectors.groupingBy( FileData::hash ) );

        var result = DuplicateFilesMapper.toActionList( map );

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(4);

        assertThat(result)
                .elements( 0, 2 )
                .extracting( "class")
                .containsExactly( KeepAction.class, KeepAction.class );

        assertThat(result)
                .elements( 0, 2 )
                .map( Action::data )
                .containsExactlyInAnyOrder( fd1, fd3 );

        assertThat(result)
                .elements( 1, 3 )
                .extracting( "class")
                .containsExactly( RemoveAction.class, RemoveAction.class );

        assertThat(result)
                .elements( 1, 3 )
                .map(Action::data)
                .containsExactlyInAnyOrder( fd2, fd4 );
    }


    @Test
    void whenArgumentContainsOnlyUniqueItems_DuplicateFilesMapper_ReturnsAListWithAllItemsWrappedInAKeepAction() {

        var uniqueHash1 = "uniquehash1";
        var uniqueHash2 = "uniquehash2";
        var uniqueHash3 = "uniquehash3";

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .hash(uniqueHash1)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(2L)
                .hash(uniqueHash2)
                .build();

        var fd3 = new FileDataBuilder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(3L)
                .hash(uniqueHash3)
                .build();

        var map = Stream.of( fd1, fd2, fd3 )
                .collect( Collectors.groupingBy( FileData::hash ) );

        var result = DuplicateFilesMapper.toActionList( map );

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .extracting( "class" )
                .containsExactly( KeepAction.class, KeepAction.class, KeepAction.class );

        assertThat(result)
                .map( Action::data )
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }

    @Test
    void whenArgumentContainsBothUniqueItemListsAndDuplicateItemLists_DuplicateFilesMapper_ReturnsAllUniqueItemsWrappedInAKeepActionAnForAllDuplicateItemsOneDuplicateItemWrappedInAKeepActionAndAllOtherDuplicateItemsWrappedInARemoveAction() {

        var singleItemSize = 1L;
        var multipleItemSize = 2L;

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(multipleItemSize)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(multipleItemSize)
                .build();

        var fd3 = new FileDataBuilder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(singleItemSize)
                .build();

        var map = Stream.of(fd1, fd2, fd3)
                        .collect(Collectors.groupingBy(FileData::sizeInBytes));

        var actionList = DuplicateFilesMapper.toActionList(map);

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 );

        assertThat( actionList )
                .extracting( "class" )
                .containsExactly( KeepAction.class, KeepAction.class, RemoveAction.class );

        assertThat( actionList )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactly( fd3, fd1, fd2);

    }
}