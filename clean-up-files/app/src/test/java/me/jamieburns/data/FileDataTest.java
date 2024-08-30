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
        var expectedHash = Optional.of( "hash string");

        var fd = new FileData( expectedFileName, expectedPath, expectedSizeInBytes, expectedHash );

        assertThat( fd.filename() ).isEqualTo( expectedFileName );
        assertThat( fd.path() ).isEqualTo( expectedPath );
        assertThat( fd.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd.hash() ).isEqualTo( expectedHash );
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
        assertThat( fd.hash() ).isEmpty();
    }


    @Test
    public void builderProvidedWithAllFieldValuesShouldCreateAUsableFileDataInstance() {

        var expectedFileName = "test string";
        var expectedPath = "test path";
        var expectedSizeInBytes = 100L;
        var expectedHash = "hash string";

        var fd = new FileData.Builder()
                .filename( expectedFileName)
                .path( expectedPath)
                .sizeInBytes( expectedSizeInBytes )
                .hash( expectedHash )
                .build();

        assertThat( fd.filename() ).isEqualTo( expectedFileName );
        assertThat( fd.path() ).isEqualTo( expectedPath );
        assertThat( fd.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
    }


    @Test
    public void builderProvidedWithAnExistingFileDataInstanceShouldCreateANewUsableFileDataInstance() {

        var expectedFileName = "test string";
        var expectedPath = "test path";
        var expectedSizeInBytes = 100L;
        var expectedHash = Optional.of("hash string");

        var fd1 = new FileData( expectedFileName, expectedPath, expectedSizeInBytes, expectedHash );

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
        var expectedHash = "hash string";

        var fd1 = new FileData( "fn", "p", 1, Optional.of("ch") );

        var fd2 = new FileData.Builder()
                .fromFileData( fd1 )
                .filename( expectedFileName )
                .path( expectedPath )
                .sizeInBytes( expectedSizeInBytes )
                .hash( expectedHash )
                .build();

        assertThat( fd2 ).isNotSameAs( fd1 );
        assertThat( fd2 ).isNotEqualTo( fd1 );
        assertThat( fd2.filename() ).isEqualTo( expectedFileName );
        assertThat( fd2.path() ).isEqualTo( expectedPath );
        assertThat( fd2.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd2.hash() ).contains( expectedHash );
    }

    @Test
    public void builderProvidedWithAnExistingFileDataInstanceShouldBeAllowedToOverrideSomeExistingValuesAndCreateANewUsableFileDataInstance() {

        var expectedFileName = "overridden filename";
        var expectedPath = "original path";
        var expectedSizeInBytes = 100L;
        var expectedHash = "overridden hash string";

        var fd1 = new FileData( "filename to override", expectedPath, expectedSizeInBytes, Optional.of("hash string to override") );

        var fd2 = new FileData.Builder()
                .fromFileData( fd1 )
                .filename( expectedFileName )
                .hash( expectedHash )
                .build();

        assertThat( fd2 ).isNotSameAs( fd1 );
        assertThat( fd2 ).isNotEqualTo( fd1 );
        assertThat( fd2.filename() ).isEqualTo( expectedFileName );
        assertThat( fd2.path() ).isEqualTo( expectedPath );
        assertThat( fd2.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd2.hash() ).contains( expectedHash );
    }


    @Test
    public void builderProvidedWithExistingValuesShouldBeAllowedToOverrideWithExistingFileDataAndCreateANewUsableFileDataInstance() {

        var expectedFileName = "test string";
        var expectedPath = "test path";
        var expectedSizeInBytes = 100L;
        var expectedHash = "hash string";

        var fd1 = new FileData( expectedFileName, expectedPath, expectedSizeInBytes, Optional.of(expectedHash) );

        var fd2 = new FileData.Builder()
                .filename( "fn" )
                .path( "p" )
                .sizeInBytes( 1 )
                .hash( "ch" )
                .fromFileData( fd1 )
                .build();

        assertThat( fd2 ).isNotSameAs( fd1 );
        assertThat( fd2 ).isEqualTo( fd1 );
        assertThat( fd2.filename() ).isEqualTo( expectedFileName );
        assertThat( fd2.path() ).isEqualTo( expectedPath );
        assertThat( fd2.sizeInBytes() ).isEqualTo( expectedSizeInBytes );
        assertThat( fd2.hash() ).contains( expectedHash );
    }
}
