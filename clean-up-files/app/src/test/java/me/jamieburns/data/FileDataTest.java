package me.jamieburns.data;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

public class FileDataTest {

    @Test
    public void newFileDataTest() {

        var expectedFileName = "test string";
        var expectedPath = "test path";
        var expectedSizeInBytes = 100L;
        var expectedChunkHash = Optional.of( "chunk hash string");
        var expectedFullHash = Optional.of( "full hash string" );

        var fd = new FileData( expectedFileName, expectedPath, expectedSizeInBytes, expectedChunkHash, expectedFullHash );
        
        assertThat( fd.filename() ).isEqualTo( expectedFileName );
        assertThat( fd.path() ).isEqualTo( expectedPath );
        assertThat( fd.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd.chunkHash() ).isEqualTo( expectedChunkHash );
        assertThat( fd.fullHash() ).isEqualTo( expectedFullHash );
    }


    @Test
    public void builderOnlyProvidedWithFilenamePathAndSizeInBytesShouldCreateAUsableFileDataInstance() {

        var expectedFileName = "test string";
        var expectedPath = "test path";
        var expectedSizeInBytes = 100L;

        var fd = new FileData.Builder()
                .filename( expectedFileName )
                .path( expectedPath )
                .sizeInBytes( expectedSizeInBytes )
                .build();
        
        assertThat( fd.filename() ).isEqualTo( expectedFileName );
        assertThat( fd.path() ).isEqualTo( expectedPath );
        assertThat( fd.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd.chunkHash() ).isEmpty();
        assertThat( fd.fullHash() ).isEmpty();
    }


    @Test
    public void builderProvidedWithAllFieldValuesShouldCreateAUsableFileDataInstance() {

        var expectedFileName = "test string";
        var expectedPath = "test path";
        var expectedSizeInBytes = 100L;
        var expectedChunkHash = "chunk hash string";
        var expectedFullHash = "full hash string";

        var fd = new FileData.Builder()
                .filename( expectedFileName)
                .path( expectedPath)
                .sizeInBytes( expectedSizeInBytes )
                .chunkHash( expectedChunkHash )
                .fullHash( expectedFullHash )
                .build();
        
        assertThat( fd.filename() ).isEqualTo( expectedFileName );
        assertThat( fd.path() ).isEqualTo( expectedPath );
        assertThat( fd.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd.chunkHash() ).contains( expectedChunkHash );
        assertThat( fd.fullHash() ).contains( expectedFullHash );
    }


    @Test
    public void builderProvidedWithAnExistingFileDataInstanceShouldCreateANewUsableFileDataInstance() {

        var expectedFileName = "test string";
        var expectedPath = "test path";
        var expectedSizeInBytes = 100L;
        var expectedChunkHash = Optional.of("chunk hash string");
        var expectedFullHash = Optional.of("full hash string");

        var fd1 = new FileData( expectedFileName, expectedPath, expectedSizeInBytes, expectedChunkHash, expectedFullHash );

        var fd2 = new FileData.Builder()
                .fromFileData( fd1 )
                .build();

        assertThat( fd2 ).isNotSameAs( fd1 );
        assertThat( fd2 ).isEqualTo( fd1 );
    }


    @Test
    public void builderProvidedWithAnExistingFileDataInstanceShouldBeAllowedToOverrideExistingValuesAndCreateANewUsableFileDataInstance() {

        var expectedFileName = "test string";
        var expectedPath = "test path";
        var expectedSizeInBytes = 100L;
        var expectedChunkHash = "chunk hash string";
        var expectedFullHash = "full hash string";

        var fd1 = new FileData( "fn", "p", 1, Optional.of("ch"), Optional.of("fh") );

        var fd2 = new FileData.Builder()
                .fromFileData( fd1 )
                .filename( expectedFileName )
                .path( expectedPath )
                .sizeInBytes( expectedSizeInBytes )
                .chunkHash( expectedChunkHash )
                .fullHash( expectedFullHash )
                .build();

        assertThat( fd2 ).isNotSameAs( fd1 );
        assertThat( fd2 ).isNotEqualTo( fd1 );
        assertThat( fd2.filename() ).isEqualTo( expectedFileName );
        assertThat( fd2.path() ).isEqualTo( expectedPath );
        assertThat( fd2.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd2.chunkHash() ).contains( expectedChunkHash );
        assertThat( fd2.fullHash() ).contains( expectedFullHash );
    }

    @Test
    public void builderProvidedWithAnExistingFileDataInstanceShouldBeAllowedToOverrideSomeExistingValuesAndCreateANewUsableFileDataInstance() {

        var expectedFileName = "overridden filename";
        var expectedPath = "original path";
        var expectedSizeInBytes = 100L;
        var expectedChunkHash = "overridden chunk hash string";
        var expectedFullHash = "original full hash string";

        var fd1 = new FileData( "filename will be overridden", expectedPath, expectedSizeInBytes, Optional.of("chunk hash string will be overridden"), Optional.of(expectedFullHash) );

        var fd2 = new FileData.Builder()
                .fromFileData( fd1 )
                .filename( expectedFileName )
                .chunkHash( expectedChunkHash )
                .build();

        assertThat( fd2 ).isNotSameAs( fd1 );
        assertThat( fd2 ).isNotEqualTo( fd1 );
        assertThat( fd2.filename() ).isEqualTo( expectedFileName );
        assertThat( fd2.path() ).isEqualTo( expectedPath );
        assertThat( fd2.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd2.chunkHash() ).contains( expectedChunkHash );
        assertThat( fd2.fullHash() ).contains( expectedFullHash );
    }


    @Test
    public void builderProvidedWithExistingValuesShouldBeAllowedToOverrideWithExistingFileDataAndCreateANewUsableFileDataInstance() {

        var expectedFileName = "test string";
        var expectedPath = "test path";
        var expectedSizeInBytes = 100L;
        var expectedChunkHash = "chunk hash string";
        var expectedFullHash = "full hash string";

        var fd1 = new FileData( expectedFileName, expectedPath, expectedSizeInBytes, Optional.of(expectedChunkHash), Optional.of(expectedFullHash) );

        var fd2 = new FileData.Builder()
                .filename( "fn" )
                .path( "p" )
                .sizeInBytes( 1 )
                .chunkHash( "ch" )
                .fullHash( "fh" )
                .fromFileData( fd1 )
                .build();

        assertThat( fd2 ).isNotSameAs( fd1 );
        assertThat( fd2 ).isEqualTo( fd1 );
        assertThat( fd2.filename() ).isEqualTo( expectedFileName );
        assertThat( fd2.path() ).isEqualTo( expectedPath );
        assertThat( fd2.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd2.chunkHash() ).contains( expectedChunkHash );
        assertThat( fd2.fullHash() ).contains( expectedFullHash );
    }   
}
