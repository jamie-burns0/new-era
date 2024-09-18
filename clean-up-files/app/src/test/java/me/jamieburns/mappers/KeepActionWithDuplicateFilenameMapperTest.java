package me.jamieburns.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.KeepAction;
import me.jamieburns.actions.KeepWithRenameAction;
import me.jamieburns.data.FileData;
import me.jamieburns.data.FileDataBuilder;

public class KeepActionWithDuplicateFilenameMapperTest {

    @Test
    void whenArgumentIsNullOrEmpty_KeepActionWithDuplicateFilenameMapper_ReturnsAnEmptyList() {
        assertThat(KeepActionWithDuplicateFilenameMapper.toActionList(null)).isEmpty();
        assertThat(KeepActionWithDuplicateFilenameMapper.toActionList(List.of())).isEmpty();
    }

    @Test
    void whenArgumentContainsOnlyKeepActionsItemsWithDuplicateFileDataItems_KeepActionWithDuplicateFilenameMapper_ReturnsAListWithOneDuplicateFileDataItemWrappedInAKeepActionAndAllOtherDuplicateItemsWrappedInAKeepWithRenameAction() {

        var duplicateFilename = "duplicatefilename";

        var fd1 = new FileDataBuilder()
                .filename(duplicateFilename)
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename(duplicateFilename)
                .path("path2")
                .sizeInBytes(2L)
                .build();

        var fd3 = new FileDataBuilder()
                .filename(duplicateFilename)
                .path("path3")
                .sizeInBytes(3L)
                .build();

        List<Action<FileData>> list = Stream.of( fd1, fd2, fd3 )
                .map( KeepAction::new )
                .collect( Collectors.toList() );

        var result = KeepActionWithDuplicateFilenameMapper.toActionList( list );

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3);

        assertThat(result)
                .elements( 0 )
                .extracting( "class")
                .containsExactly( KeepAction.class );

        assertThat(result)
                .elements( 0 )
                .map( Action::data )
                .containsExactlyInAnyOrder( fd1 );

        assertThat(result)
                .elements( 1, 2 )
                .extracting( "class")
                .containsExactly( KeepWithRenameAction.class, KeepWithRenameAction.class );

        assertThat(result)
                .elements( 1, 2 )
                .map(Action::data)
                .containsExactlyInAnyOrder( fd2, fd3 );
    }


    @Test
    void whenArgumentContainsOnlyUniqueItems_KeepActionWithDuplicateFilenameMapper_ReturnsAListWithAllItemsWrappedInAKeepAction() {

        var uniqueFilename1 = "filename1";
        var uniqueFilename2 = "filename2";
        var uniqueFilename3 = "filename3";

        var fd1 = new FileDataBuilder()
                .filename(uniqueFilename1)
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename(uniqueFilename2)
                .path("path2")
                .sizeInBytes(2L)
                .build();

        var fd3 = new FileDataBuilder()
                .filename(uniqueFilename3)
                .path("path3")
                .sizeInBytes(3L)
                .build();

        List<Action<FileData>> list = Stream.of( fd1, fd2, fd3 )
                .map( KeepAction::new )
                .collect( Collectors.toList() );

        var result = KeepActionWithDuplicateFilenameMapper.toActionList( list );

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
    void whenArgumentContainsBothUniqueKeepActionItemsAndDuplicateKeepActionItemLists_KeepActionWithDuplicateFilenameMapper_ReturnsAListWithAllUniqueFileDataItemsWrappedInAKeepActionAndOneDuplicateFileDataItemWrappedInAKeepActionAndAllOtherDuplicateItemsWrappedInAKeepWithRenameAction() {

        var duplicateFilename = "fileD";
        var uniqueFilename = "fileU";

        var fd1 = new FileDataBuilder()
                .filename(duplicateFilename)
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileDataBuilder()
                .filename(duplicateFilename)
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var fd3 = new FileDataBuilder()
                .filename( uniqueFilename )
                .path( "path3")
                .sizeInBytes( 3L )
                .build();

        List<Action<FileData>> list = Stream.of(fd1, fd2, fd3)
                .map( KeepAction::new )
                .collect(Collectors.toList());

        var actionList = KeepActionWithDuplicateFilenameMapper.toActionList( list );

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 );

        assertThat( actionList )
                .extracting( "class" )
                .containsExactly( KeepAction.class, KeepWithRenameAction.class, KeepAction.class );

        assertThat( actionList )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }
}