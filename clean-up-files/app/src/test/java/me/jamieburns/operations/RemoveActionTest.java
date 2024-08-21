package me.jamieburns.operations;

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
        assertThat( ra.data().chunkHash() ).isEmpty();
        assertThat( ra.data().fullHash() ).isEmpty();

        var expectedChunkHash = "ch";
        var expectedFullHash = "fh";

        var fd2 = new FileData.Builder()
            .filename( expectedFileName )
            .path( expectedPath )
            .sizeInBytes( expectedSizeInBytes )
            .chunkHash( expectedChunkHash )
            .fullHash( expectedFullHash )
            .build();

        var ra2 = new KeepAction<>( fd2 );

        assertThat( ra2.data() ).isEqualTo( fd2 );
        assertThat( ra2.data().filename() ).isEqualTo( expectedFileName );
        assertThat( ra2.data().path() ).isEqualTo( expectedPath );
        assertThat( ra2.data().sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( ra2.data().chunkHash() ).contains( expectedChunkHash );
        assertThat( ra2.data().fullHash() ).contains( expectedFullHash );
    }
}
