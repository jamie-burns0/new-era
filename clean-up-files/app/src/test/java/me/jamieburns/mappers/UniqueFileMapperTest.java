package me.jamieburns.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.KeepAction;
import me.jamieburns.data.FileDataBuilder;


public class UniqueFileMapperTest {

        @Test
    void whenArgumentIsANullOrEmptyList_UniqueFileMapper_ReturnsAnEmptyList() {
        assertThat( UniqueFileMapper.toActionList( null )).isEmpty();
        assertThat( UniqueFileMapper.toActionList( List.of() )).isEmpty();
    }


    @Test
    void whenArgumentContainsFileDataItems_UniqueFileMapper_ReturnsEachItemWrappedInAKeepAction() {

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( 0L )
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( 0L )
                .build();

        assertThat( UniqueFileMapper.toActionList( List.of( fd1, fd2 ) ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof KeepAction)
                .map(Action::data)
                .containsExactlyInAnyOrder( fd1, fd2);
    }
}