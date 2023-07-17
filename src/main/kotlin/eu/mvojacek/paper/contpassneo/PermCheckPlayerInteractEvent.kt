package eu.mvojacek.paper.contpassneo

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class PermCheckPlayerInteractEvent(
    who: Player,
    action: Action,
    item: ItemStack?,
    clickedBlock: Block?,
    clickedFace: BlockFace,
    hand: EquipmentSlot? = EquipmentSlot.HAND,
    clickedPosition: Vector? = null
) : PlayerInteractEvent(who, action, item, clickedBlock, clickedFace, hand, clickedPosition)
