plugins {
    id("net.neoforged.moddev") version "2.0.148" apply false
    // for normal there should be "me.modmuss50.mod-publish-plugin", here just for self use.
    id("me.afk.mod-publish-plugin") version "2.2.1" apply false
}

stonecutter active "26.3"


// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${properties["mod.version"]}\";"
    swaps["minecraft"] = "\"${current.version}\";"
    constants["release"] = properties["mod.id"] != "template"

    replacements {
        string(current.parsed >= "26.2") {
            replace("Minecraft.getInstance().screen", "Minecraft.getInstance().gui.screen()")
            replace("mc.screen", "mc.gui.screen()")
        }

        string(current.parsed >= "26.1.2") {
            replace("renderWidget", "extractContents")
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("screen.getGuiLeft()", "screen.getLeftPos()")
            replace("screen.getGuiTop()", "screen.getTopPos()")
            replace("screen.getXSize()", "screen.getImageWidth()")
            replace("screen.getXSize()", "screen.getImageWidth()")
            replace("player.displayClientMessage", "player.sendSystemMessage")
        }

        string(current.parsed >= "1.21.11" && current.parsed < "26.1.2") {
            replace("renderWidget", "renderContents")
        }

        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}
