package dev.phonis.schematica_extensions.extensions;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import dev.phonis.schematica_extensions.config.ConfigurationManager;
import dev.phonis.schematica_extensions.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

public class CustomSchematicLoad
{

    public static final CustomSchematicLoad INSTANCE = new CustomSchematicLoad();

    private final Minecraft minecraft = Minecraft.getMinecraft();


    public void customLoadSchematic()
    {
        String[] command = this.getCommand();
        ForkJoinPool.commonPool().submit(() ->
        {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            try
            {
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    this.showErrorMessage("chooser program exited with non-zero exit code " + exitCode);
                    return;
                }
                File schematicFile = new File(IOUtils.toString(process.getInputStream()).replaceAll("\n", ""));
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

    private String[] getCommand()
    {
        File schematicDirectoryFile = Schematica.proxy.getPlayerSchematicDirectory(null, true);
        return Arrays.stream(ConfigurationManager.INSTANCE.schematicCustomLoadCommand)
            .map(argument -> this.processArgument(argument, schematicDirectoryFile)).toArray(String[]::new);
    }

    private String processArgument(String argument, File schematicDirectory)
    {
        return argument.replace("{directory}", schematicDirectory.getAbsolutePath());
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
