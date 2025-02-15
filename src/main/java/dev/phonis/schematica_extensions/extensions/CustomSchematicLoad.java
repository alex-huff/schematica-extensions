package dev.phonis.schematica_extensions.extensions;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import dev.phonis.schematica_extensions.config.ConfigurationManager;
import dev.phonis.schematica_extensions.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

public class CustomSchematicLoad
{

    public static final CustomSchematicLoad INSTANCE = new CustomSchematicLoad();

    private final Minecraft minecraft = Minecraft.getMinecraft();


    public void customLoadSchematic()
    {
        ForkJoinPool.commonPool().submit(() ->
        {
            try
            {
                File schematicDirectoryFile = Schematica.proxy.getPlayerSchematicDirectory(null, true);
                File tempFile = File.createTempFile("schematica-extensions", null);
                tempFile.deleteOnExit();
                String[] command = this.getCommand(schematicDirectoryFile, tempFile.getPath());
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    this.showErrorMessage("chooser program exited with non-zero exit code " + exitCode);
                    return;
                }
                List<String> choiceFileLines = new ArrayList<>();
                try (FileInputStream choiceFileInputStream = new FileInputStream(tempFile))
                {
                    BOMInputStream bomInputStream
                        = new BOMInputStream(choiceFileInputStream, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE);
                    String charsetName = bomInputStream.hasBOM() ? bomInputStream.getBOMCharsetName()
                                                                 : ByteOrderMark.UTF_8.getCharsetName();
                    BufferedReader choiceFileReader
                        = new BufferedReader(new InputStreamReader(bomInputStream, Charset.forName(charsetName)));
                    String currentLine = choiceFileReader.readLine();
                    while (currentLine != null)
                    {
                        choiceFileLines.add(currentLine);
                        currentLine = choiceFileReader.readLine();
                    }
                }
                Optional<String> schematicPathOptional = choiceFileLines.stream().filter(path -> !path.isEmpty())
                    .findFirst();
                String schematicPathString = schematicPathOptional.isPresent() ? schematicPathOptional.get()
                                                                               : this.removeLineTerminators(IOUtils.toString(process.getInputStream(), Charsets.UTF_8));
                File schematicFile = new File(schematicPathString);
                this.minecraft.addScheduledTask(() ->
                {
                    if (!this.tryLoadSchematic(schematicFile))
                    {
                        this.showErrorMessage("invalid schematic");
                    }
                });
            }
            catch (IOException | InterruptedException exception)
            {
                this.showErrorMessage(exception);
            }
        });
    }

    private String removeLineTerminators(String input)
    {
        return input.replaceAll("[\\r\\n]", "");
    }

    private String[] getCommand(File schematicDirectoryFile, String choiceFilePath)
    {
        return Arrays.stream(ConfigurationManager.INSTANCE.schematicCustomLoadCommand)
            .map(argument -> this.processArgument(argument, schematicDirectoryFile, choiceFilePath))
            .toArray(String[]::new);
    }

    private String processArgument(String argument, File schematicDirectory, String choiceFilePath)
    {
        return argument.replace("{schematics_directory}", schematicDirectory.getAbsolutePath())
            .replace("{choice_file}", choiceFilePath);
    }

    private void showErrorMessage(Exception exception)
    {
        exception.printStackTrace();
        this.showErrorMessage(exception.getMessage());
    }

    private void showErrorMessage(String message)
    {
        this.minecraft.addScheduledTask(() -> MinecraftUtil.sendLocalMessage("schematica load failed: " + message));
    }

    private boolean tryLoadSchematic(File schematicFile)
    {
        boolean succeeded
            = Schematica.proxy.loadSchematic(null, schematicFile.getParentFile(), schematicFile.getName());
        if (!succeeded)
        {
            return false;
        }
        ClientProxy.moveSchematicToPlayer(ClientProxy.schematic);
        return true;
    }

}
