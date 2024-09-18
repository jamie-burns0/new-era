package me.jamieburns.actions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileDataBuilder;


public class KeepActionTest {

    @Test
    public void moveActionShouldReturnProvidedFileDataAsIs() {

        var expectedFileName = "fn";
        var expectedPath = "p";
        var expectedSizeInBytes = 1;

        var fd = new FileDataBuilder()
                .filename( expectedFileName )
                .path( expectedPath )
                .sizeInBytes( expectedSizeInBytes )
                .build();

        var ma = new KeepAction<>( fd );

        assertThat( ma.data() ).isEqualTo( fd );
        assertThat( ma.data().filename() ).isEqualTo( expectedFileName );
        assertThat( ma.data().path() ).isEqualTo( expectedPath );
        assertThat( ma.data().sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( ma.data().hash() ).isEmpty();

        var expectedHash = "hash";

        var fd2 = new FileDataBuilder()
            .filename( expectedFileName )
            .path( expectedPath )
            .sizeInBytes( expectedSizeInBytes )
            .hash( expectedHash )
            .build();

        var ma2 = new KeepAction<>( fd2 );

        assertThat( ma2.data() ).isEqualTo( fd2 );
        assertThat( ma2.data().filename() ).isEqualTo( expectedFileName );
        assertThat( ma2.data().path() ).isEqualTo( expectedPath );
        assertThat( ma2.data().sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( ma2.data().hash() ).contains( expectedHash );
    }
}
