# Container Passthrough Neo

A simple Spigot plugin to enable opening containers through certain blocks and entities.

Currently supports PaperMC 1.20.1.

## Passthrough Blocks / Entities
 - Signs, including colored, glowing
 - Hanging signs
 - Paintings
 - Item Frames

## Supported containers
 - Any block with an inventory (chests, furnaces, shulker boxes, etc.)
 - Enderchests
 - [TODO] In the future, stonecutters, grindstones, anvils, and enchanting tables may be added
 - [TODO] Noteblocks, for redstone activation through signs or maps

## Features
 - Disable while sneaking (allows editing signs and placing items into itemframes)
 - Coloring, un/glowing, waxing signs (right click while holding the appropriate item)
 - Otherwise, the container behind the block is opened

## Attribution

For the inspiration and initial codebase, see [Attribution.md](./attribution/README.md).