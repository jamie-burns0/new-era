package me.jamieburns.actions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;

public class RemoveActionTest {

    @Test
    public void removeActionShouldReturnProvidedFileDataAsIs() {

        var expectedFileName = "fn";
        var expectedPath = "p";
        var expectedSizeInBytes = 1;

        var fd = new FileData.Builder()
                .filename( expectedFileName )
                .path( expectedPath )
                .sizeInBytes( expectedSizeInBytes )
                .build();

        var ra = new RemoveAction<>( fd );

        assertThat( ra.data() ).isEqualTo( fd );
        assertThat( ra.data().filename() ).isEqualTo( expectedFileName );
        assertThat( ra.data().path() ).isEqualTo( expectedPath );
        assertThat( ra.data().sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( ra.data().hash() ).isEmpty();

        var expectedHash = "hash";

        var fd2 = new FileData.Builder()
            .filename( expectedFileName )
            .path( expectedPath )
            .sizeInBytes( expectedSizeInBytes )
            .hash( expectedHash )
            .build();

        var ra2 = new RemoveAction<>( fd2 );

        assertThat( ra2.data() ).isEqualTo( fd2 );
        assertThat( ra2.data().filename() ).isEqualTo( expectedFileName );
        assertThat( ra2.data().path() ).isEqualTo( expectedPath );
        assertThat( ra2.data().sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( ra2.data().hash() ).contains( expectedHash );
    }
}
