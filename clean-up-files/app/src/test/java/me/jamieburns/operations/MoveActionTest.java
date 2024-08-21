package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;

public class MoveActionTest {

    @Test
    public void moveActionShouldReturnProvidedFileDataAsIs() {

        var expectedFileName = "fn";
        var expectedPath = "p";
        var expectedSizeInBytes = 1;

        var fd = new FileData.Builder()
                .filename( expectedFileName )
                .path( expectedPath )
                .sizeInBytes( expectedSizeInBytes )
                .build();

        var ma = new KeepAction<>( fd );

        assertThat( ma.data() ).isEqualTo( fd );
        assertThat( ma.data().filename() ).isEqualTo( expectedFileName );
        assertThat( ma.data().path() ).isEqualTo( expectedPath );
        assertThat( ma.data().sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( ma.data().chunkHash() ).isEmpty();
        assertThat( ma.data().fullHash() ).isEmpty();

        var expectedChunkHash = "ch";
        var expectedFullHash = "fh";

        var fd2 = new FileData.Builder()
            .filename( expectedFileName )
            .path( expectedPath )
            .sizeInBytes( expectedSizeInBytes )
            .chunkHash( expectedChunkHash )
            .fullHash( expectedFullHash )
            .build();

        var ma2 = new KeepAction<>( fd2 );

        assertThat( ma2.data() ).isEqualTo( fd2 );
        assertThat( ma2.data().filename() ).isEqualTo( expectedFileName );
        assertThat( ma2.data().path() ).isEqualTo( expectedPath );
        assertThat( ma2.data().sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( ma2.data().chunkHash() ).contains( expectedChunkHash );
        assertThat( ma2.data().fullHash() ).contains( expectedFullHash );
    }
}
