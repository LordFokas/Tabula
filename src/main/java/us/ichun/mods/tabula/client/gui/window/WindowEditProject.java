package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementNumberInput;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketGenericMethod;

import java.util.Locale;

public class WindowEditProject extends Window
{
    public WindowEditProject(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.editProject.title", true);

        ProjectInfo project = ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject);

        ElementTextInput text1 = new ElementTextInput(this, 10, 30, width - 20, 12, 1, "window.newProject.projIdent");
        text1.textField.setText(project.modelName);
        elements.add(text1);
        ElementTextInput text2 = new ElementTextInput(this, 10, 65, width - 20, 12, 2, "window.newProject.animName");
        text2.textField.setText(project.authorName);
        elements.add(text2);
        ElementNumberInput nums = new ElementNumberInput(this, 10, 100, 80, 12, 3, "window.newProject.txDimensions", 2, false, 0, (int)Short.MAX_VALUE);
        nums.textFields.get(0).setText(Integer.toString(project.textureWidth));
        nums.textFields.get(1).setText(Integer.toString(project.textureHeight));
        elements.add(nums);
        ElementNumberInput scale = new ElementNumberInput(this, 10, 135, 120, 12, 4, "window.newProject.projectScale", 3, true, (int)Short.MIN_VALUE, (int)Short.MAX_VALUE);
        scale.textFields.get(0).setText(String.format(Locale.ENGLISH, "%.2f", project.scale[0]));
        scale.textFields.get(1).setText(String.format(Locale.ENGLISH, "%.2f", project.scale[1]));
        scale.textFields.get(2).setText(String.format(Locale.ENGLISH, "%.2f", project.scale[2]));
        elements.add(scale);
        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newProject.projIdent"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newProject.animName"), posX + 11, posY + 55, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newProject.txDimensions"), posX + 11, posY + 90, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newProject.projectScale"), posX + 11, posY + 125, Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id > 0 && element.id != 3 && element.id != 4)
        {
            if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
            {
                String projName = "";
                String authName = "";
                int dimW = 0;
                int dimH = 0;
                double scaleX = 1.0F;
                double scaleY = 1.0F;
                double scaleZ = 1.0F;
                for(int i = 0; i < elements.size(); i++)
                {
                    if(elements.get(i) instanceof ElementTextInput)
                    {
                        ElementTextInput text = (ElementTextInput)elements.get(i);
                        if(text.id == 1)
                        {
                            projName = text.textField.getText();
                        }
                        else if(text.id == 2)
                        {
                            authName = text.textField.getText();
                        }
                    }
                    if(elements.get(i) instanceof ElementNumberInput)
                    {
                        ElementNumberInput nums = (ElementNumberInput)elements.get(i);
                        if(nums.id == 3)
                        {
                            dimW = Integer.parseInt(nums.textFields.get(0).getText());
                            dimH = Integer.parseInt(nums.textFields.get(1).getText());
                        }
                        else if(nums.id == 4)
                        {
                            scaleX = Double.parseDouble(nums.textFields.get(0).getText());
                            scaleY = Double.parseDouble(nums.textFields.get(1).getText());
                            scaleZ = Double.parseDouble(nums.textFields.get(2).getText());
                        }
                    }
                }
                if(projName.isEmpty())
                {
                    return;
                }
                if(!((GuiWorkspace)workspace).remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.editProject(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, projName, authName, dimW, dimH, scaleX, scaleY, scaleZ);
                }
                else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
                {
                    Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "editProject", ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, projName, authName, dimW, dimH, scaleX, scaleY, scaleZ));
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
