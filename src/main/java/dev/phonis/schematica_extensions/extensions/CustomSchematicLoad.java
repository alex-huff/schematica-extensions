package dev.phonis.schematica_extensions.extensions;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import dev.phonis.schematica_extensions.config.ConfigurationManager;
import dev.phonis.schematica_extensions.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
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
                File tempFile = File.createTempFile("schematica-extensions", null);
                tempFile.deleteOnExit();
                String[] command = this.getCommand(tempFile.getPath());
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    this.showErrorMessage("chooser program exited with non-zero exit code " + exitCode);
                    return;
                }
                Optional<String> schematicPathOptional = Files.readAllLines(tempFile.toPath(), Charsets.UTF_8).stream()
                    .map(this::removeLineTerminators).filter(path -> !path.isEmpty()).findFirst();
                String schematicPath = schematicPathOptional.isPresent() ? schematicPathOptional.get()
                                                                         : this.removeLineTerminators(IOUtils.toString(process.getInputStream(), Charsets.UTF_8));
                File schematicFile = new File(schematicPath);
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

    private String[] getCommand(String choiceFilePath)
    {
        File schematicDirectoryFile = Schematica.proxy.getPlayerSchematicDirectory(null, true);
        return Arrays.stream(ConfigurationManager.INSTANCE.schematicCustomLoadCommand)
            .map(argument -> this.processArgument(argument, schematicDirectoryFile, choiceFilePath))
            .toArray(String[]::new);
    }

    private String processArgument(String argument, File schematicDirectory, String choiceFilePath)
    {
        return argument.replace("{schematic_directory}", schematicDirectory.getAbsolutePath())
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
