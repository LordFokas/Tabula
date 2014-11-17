package us.ichun.mods.tabula.client.mainframe.core;

import com.google.gson.Gson;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.util.MD5Checksum;
import net.minecraft.client.Minecraft;
import us.ichun.mods.tabula.Tabula;
import us.ichun.mods.tabula.client.gui.window.Window;
import us.ichun.mods.tabula.client.gui.window.WindowOpenProject;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProjectHelper
{
    public static ProjectInfo createProjectFromJson(String ident, String s)
    {
        Gson gson = new Gson();
        ProjectInfo info = gson.fromJson(s, ProjectInfo.class);
        info.identifier = ident;
        return info;
    }

    @SideOnly(Side.CLIENT)
    public static ProjectInfo createProjectFromJsonHost(String ident, String s)
    {
        Gson gson = new Gson();
        ProjectInfo info = gson.fromJson(s, ProjectInfo.class);
        info.identifier = ident;

        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            for(int i = workspace.levels.size() - 1; i >= 0; i--)
            {
                for(int j = workspace.levels.get(i).size() - 1; j >= 0; j--)
                {
                    Window window = workspace.levels.get(i).get(j);
                    if(window instanceof WindowOpenProject)
                    {
                        if(((WindowOpenProject)window).openingJson != null && ((WindowOpenProject)window).openingJson.equals(s))
                        {
                            info.saveFile = ((WindowOpenProject)window).openingFile;
                            info.saveFileMd5 = MD5Checksum.getMD5Checksum(info.saveFile);

                            window.workspace.removeWindow(window);
                            break;
                        }
                    }
                }
            }
        }

        return info;
    }

    @SideOnly(Side.CLIENT)
    public static void addProjectToManager(ProjectInfo project)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            workspace.projectManager.updateProject(project);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void updateProjectTexture(String ident, BufferedImage image)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            for(ProjectInfo project : workspace.projectManager.projects)
            {
                if(project.identifier.equals(ident))
                {
                    if(project.ignoreNextImage)
                    {
                        project.ignoreNextImage = false; //received texture you've just set;
                    }
                    else if(project.textureFile != null)
                    {
                        project.textureFile = null; //texture file updated for other reasons.. no longer yours. Stop listening.
                    }
                    project.bufferedTexture = image;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void receiveChat(String message)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            workspace.windowChat.chatHolder.text.add(message);
            //TODO play a sound
        }
    }

    public static boolean saveProject(ProjectInfo info, File file)
    {
        try
        {
            file.getParentFile().mkdirs();

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            out.setLevel(9);
            out.putNextEntry(new ZipEntry("model.json"));

            byte[] data = (new Gson()).toJson(info).getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            if(info.bufferedTexture != null)
            {
                out.putNextEntry(new ZipEntry("texture.png"));
                ImageIO.write(info.bufferedTexture, "png", out);
            }
            out.closeEntry();

            out.close();

            info.saved = true;
            return true;
        }
        catch(Exception e)
        {
            Tabula.console("Failed to save model: " + info.modelName, true);
            e.printStackTrace();
            return false;
        }
    }
}
