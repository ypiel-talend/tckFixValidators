package org.talend.sdk.validators;

import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CommandLine.Command(name = "TCKFixValidators", description = "Fix TCK connectors to be aligned with talend-component-maven-plugin ValidateComponent mojo.")
public class TCKFixValidators implements Runnable {

    private final static List<String> tckValidatorsKeys = Arrays.asList();

    @CommandLine.Option(names = {"--folder"}, description = "Project to fix source folder.")
    private String folder;

    @CommandLine.Option(names = {"--dry-run"}, description = "Don't write anything.")
    private boolean dryRun;

    @CommandLine.Option(names = {"--list-all"}, description = "List all checked files.")
    private boolean listAll;

    @CommandLine.Option(names = {"--fix-documentations"}, description = "Fix @Documentation: uppercase 1st letter and finish by a dot.")
    private boolean fixDocumentation;

    @CommandLine.Option(names = {"--fix-placeholders"}, description = "Fix missing _placeholder in i18n files.")
    private boolean fixPlaceholder;

    @Override
    public void run() {
        System.out.println("Execution on folder: " + folder);
        if (dryRun) {
            System.out.println("dry-run execution...");
        }

        Path startPath = Paths.get(folder);
        if (!Files.isDirectory(startPath)) {
            throw new RuntimeException(String.format("The given file is not a valid directory: %s.", folder));
        }

        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (fixDocumentation && attrs.isRegularFile() && file.getFileName().toString().endsWith(".java")) {
                        if (listAll) {
                            System.out.println(String.format("Check java file %s", file));
                        }
                        try {
                            processJavaFile(file);
                        } catch (IOException e) {
                            throw new RuntimeException(String.format("Can't process the java file: %s", file), e);
                        }
                    } else if (fixPlaceholder && attrs.isRegularFile() && file.getFileName().toString().endsWith(".properties") && file.getFileName().toString().startsWith("Messages")) {
                        if (listAll) {
                            System.out.println(String.format("Check i18n file %s", file));
                        }
                        try {
                            processI18nFile(file);
                        } catch (IOException e) {
                            throw new RuntimeException(String.format("Can't process the i18n file: %s", file), e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can't process java files: %s", e.getMessage()), e);
        }

    }

    /**
     * Add _placeholder everywhere a _displayname exist in i18n properties files.
     *
     * @param file
     * @throws IOException
     */
    @Deprecated // Better to use the talend-component-maven-plugin plugin with -Dtalend.validation.internationalization.autofix=true option
    private void processI18nFile(Path file) throws IOException {
        String content = Files.readString(file);

        String updated = addMissingPlaceHolders(content);

        if (!content.equals(updated)) {
            String dry = "";
            if (!dryRun) {
                Files.writeString(file, updated);
            } else {
                dry = " (dryrun: no write)";
            }
            System.out.println(String.format("\tUpdate file%s : %s", dry, file));
            System.out.println("/!\\ Better to use the talend-component-maven-plugin plugin with -Dtalend.validation.internationalization.autofix=true option");
        }
    }

    public String addMissingPlaceHolders(String content) {
        String DISPLAYNAME_SUFFIX = "._displayName";
        int displayNameLength = DISPLAYNAME_SUFFIX.length();

        List<String> keys = content.lines()
                .filter(e -> e.indexOf("=") > 2)
                .map(e -> e.substring(0, e.indexOf("=")).trim()).collect(Collectors.toList());

        Collections.sort(keys);

        List<String> missingPlaceHolders = keys.stream().filter(k -> k.endsWith(DISPLAYNAME_SUFFIX))
                .map(k -> k.substring(0, k.length() - displayNameLength) + "._placeholder")
                .filter(k -> Collections.binarySearch(keys, k) < 0)
                .collect(Collectors.toList());

        if (!missingPlaceHolders.isEmpty()) {
            StringBuilder sb = new StringBuilder(content);
            sb.append("\n# Automatically added missing _placeholder:");
            missingPlaceHolders.forEach(e -> sb.append("\n").append(e).append(" = "));
            return sb.toString();
        } else {
            return content;
        }
    }

    private void processJavaFile(Path file) throws IOException {
        String content = Files.readString(file);
        String modifiedContent = content;
        if (fixDocumentation) {
            modifiedContent = replaceDocumentation(modifiedContent);
        } else {
            System.out.println(String.format("Don't fix @Documentation for %s", file));
            return;
        }
        if (!content.equals(modifiedContent)) {
            String dry = "";
            if (!dryRun) {
                Files.writeString(file, modifiedContent);
            } else {
                dry = " (dryrun: no write)";
            }
            System.out.println(String.format("\tUpdate file%s : %s", dry, file));
        }
    }

    /**
     * TCK \@Documentation annotation value must start by an uppercase and finish by a dot.
     *
     * @param content
     * @return
     */
    public String replaceDocumentation(String content) {
        Pattern pattern = Pattern.compile("@Documentation\\(\"(.*?)\"\\)");
        Matcher matcher = pattern.matcher(content);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String foundText = matcher.group(1);

            if (foundText != null) {
                foundText = foundText.trim();
            }

            if (foundText.isEmpty()) {
                foundText = "Undefined.";
            }

            String modifiedText = capitalizeFirstLetter(foundText);
            modifiedText = ensureEndsWithPeriod(modifiedText);
            modifiedText = modifiedText.replaceAll("\\\\", "\\\\\\\\");
            modifiedText = modifiedText.replaceAll("\\$", "\\\\\\$");
            matcher.appendReplacement(sb, "@Documentation(\"" + modifiedText + "\")");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private String ensureEndsWithPeriod(String text) {
        if (!text.endsWith(".")) {
            return text + ".";
        }
        return text;
    }


    public static void main(String[] args) {
        List<String> argsAsList = Arrays.asList(args);

        CommandLine commandLine = new CommandLine(new TCKFixValidators());
        if (argsAsList.isEmpty() || argsAsList.contains("--help") || argsAsList.contains("-h")) {
            commandLine.usage(System.out);
        } else {
            int exitCode = commandLine.execute(args);
            System.exit(exitCode);
        }
    }

}
