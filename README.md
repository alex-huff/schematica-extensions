# SchematicaExtensions
## About
SchematicaExtensions is a Minecraft 1.8.9 Forge mod that adds various features to [Schematica](https://github.com/Lunatrius/Schematica).

## Dependencies
* [Schematica](https://github.com/Lunatrius/Schematica)

## Currently Implemented Features
* ### Moving Schematics
    * #### By Scrolling
        Move a schematic by one block in the direction you are facing by scrolling while left alt is held. Hold down the sprint key as well to move the schematic by a larger distance. You can configure this distance in the Mod Options Menu.

        https://github.com/user-attachments/assets/d0156947-adf9-4dc4-8d78-119c976f36f0

    * #### By Dragging
        Start dragging a schematic by clicking it while left alt is held. While left alt is held, scrolling changes the distance between you and the schematic. Releasing left alt will cause the schematic to be positioned up against the block face you are looking at. Click to position the schematic when it is in your desired position.

        https://github.com/user-attachments/assets/3bf2d804-2ad4-4a45-a340-6edd14dd6bb1

    * #### With an Anchor Block
        While holding left alt and the sprint key, clicking a block in the schematic will select it as an anchor. Click on the corresponding block in the real world to translate the schematic's position so that the two selected blocks align.

        https://github.com/user-attachments/assets/2e57fe91-e586-4c2b-86ea-8850adb1233f

    * #### With Two Anchor Blocks
        If two anchors are selected in the schematic and two corresponding anchors are selected in the real world, the schematic will be rotated, flipped, and translated so that all the anchors align.

        https://github.com/user-attachments/assets/4a9c9d57-4689-419d-808a-c9c7b79ec4e8

* ### Custom Load Command
    Quickly choose a schematic to load using an external command of your choice. You can configure the command used in the Mod Options menu. The default Windows command uses [WezTerm](https://wezterm.org/) and [fzf](https://junegunn.github.io/fzf/). The default command for other platforms uses [rofi](https://github.com/davatorium/rofi) as shown in the video below.

    https://github.com/user-attachments/assets/2b87bcc1-bed1-48ba-9224-41d7a1cb974f
