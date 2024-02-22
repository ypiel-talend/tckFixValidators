package org.talend.sdk.validators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class TCKFixValidatorsTest {

    @Test
    void testFixDocumentationAnnotation() {
        String content = loadResourceFile("toFix/ToFix.java");
        String expected = loadResourceFile("fixed/ToFix.java");

        TCKFixValidators fixValidators = new TCKFixValidators();
        String updated = fixValidators.replaceDocumentation(content);

        Assertions.assertEquals(expected, updated);
    }

    @Test
    void testFixPlaceholders() {
        String content = loadResourceFile("toFix/Messages.properties");
        String expected = loadResourceFile("fixed/Messages.properties").trim();

        TCKFixValidators fixValidators = new TCKFixValidators();
        String updated = fixValidators.addMissingPlaceHolders(content);

        Assertions.assertEquals(expected, updated.trim());
    }


    private String loadResourceFile(String file) {
        // Get the ClassLoader
        ClassLoader classLoader = getClass().getClassLoader();

        // Get the URL of the file
        URL resource = classLoader.getResource(file);
        if (resource == null) {
            throw new RuntimeException(String.format("File not found: %s", file));
        } else {
            Path path = null;
            try {
                path = Paths.get(resource.toURI());
                String content = Files.readString(path);
                return content;
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(String.format("Can't read resource file '%s' : ", file, e.getMessage()), e);
            }
        }
    }

}