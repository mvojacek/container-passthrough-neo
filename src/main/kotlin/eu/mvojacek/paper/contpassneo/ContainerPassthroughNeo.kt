package eu.mvojacek.paper.contpassneo

import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.block.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import java.util.WeakHashMap

class ContainerPassthroughNeo : JavaPlugin(), Listener {
    private lateinit var config: Config

    private val passthroughEntities: MutableSet<EntityType> = HashSet()
    private val passthroughBlocks: MutableSet<Material> = HashSet()

    private val enderChestOpeningTracker = EnderChestOpeningTracker()

    override fun onEnable() {
        config = Config(this.getConfig())
        config.addDefaults()
        config.fileConfiguration.options().copyDefaults(true)
        saveConfig()

        if (!config.enable) return;

        passthroughEntities.clear()
        if (config.passthroughPainting) passthroughEntities.add(EntityType.PAINTING)
        if (config.passthroughItemFrame) passthroughEntities.add(EntityType.ITEM_FRAME)
        if (config.passthroughGlowItemFrame) passthroughEntities.add(EntityType.GLOW_ITEM_FRAME)

        passthroughBlocks.clear()
        if (config.passthroughSign) passthroughBlocks.addAll(Materials.signMaterials)

        if (config.enderChestTracking) enderChestOpeningTracker.init()

        server.pluginManager.registerEvents(this, this)

        logger.info("Container Passthrough Neo active!")
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        // if sneaking enabled, dont bypass when sneaking
        if (config.disableWhileSneaking && event.player.isSneaking) return

        // ignore offhand clicks
        if (event.hand == EquipmentSlot.OFF_HAND) return

        val clickedEntity = event.rightClicked

        // check if this entity can be bypassed
        if (!passthroughEntities.contains(clickedEntity.type)) return

        // all checks passed, do player raycast to find the possible container

        // raycast, return if nothing hit
        val result = event.player.rayTraceBlocks(5.0, FluidCollisionMode.NEVER) ?: return
        val hitBlock = result.hitBlock ?: return

        // try for enderchest at this location
        if (tryOpeningEnderchest(event.player, hitBlock)) {
            event.isCancelled = true
            return
        }

        val hitBlockFace = result.hitBlockFace ?: return
        // get container at target or return
        val container = hitBlock.state as? Container ?: return

        // try opening the container
        tryOpeningContainer(event.player, container, hitBlock, hitBlockFace)
        // cancel the event regardless of success
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        // if sneaking enabled, dont bypass when sneaking
        if (config.disableWhileSneaking && event.player.isSneaking) return

        // ignore offhand clicks
        if (event.hand == EquipmentSlot.OFF_HAND) return

        // only bypass rightclick actions
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        // there must be a clicked block for us to bypass something
        val clickedBlock = event.clickedBlock ?: return

        // ignore out perm check events
        if (event is PermCheckPlayerInteractEvent) return

        // check if this block can be bypassed
        if (!passthroughBlocks.contains(clickedBlock.type)) return
        val sign = clickedBlock.state as? Sign

        // get held material, nullable
        val heldItemMaterial = event.item?.type

        // when clicking a sign with an item, check if an action can be done on the sign
        if (sign != null && heldItemMaterial != null) {
            when {
                config.signAllowDye && Materials.isDye(heldItemMaterial) -> return
                config.signAllowEditGlow && Materials.canEditSignGlow(heldItemMaterial) -> return
                config.signAllowWax && Materials.canWaxSign(heldItemMaterial) -> return
            }
        }

        // all passthrough checks done, now check if the block behind is an openable inventory

        // get the block behind the clicked block
        val blockBehind = clickedBlock.getRelative(event.blockFace.oppositeFace)

        // try for enderchest at this location
        if (tryOpeningEnderchest(event.player, blockBehind)) {
            event.isCancelled = true
            return
        }

        // get block container, or return
        val container = blockBehind.state as? Container ?: return

        // try opening the container
        tryOpeningContainer(event.player, container, blockBehind, event.blockFace)
        // regardless of success, cancel the event
        event.isCancelled = true
    }

    private fun tryOpeningContainer(player: Player, container: Container, block: Block, face: BlockFace) {
        // inventory already open, nothing to do
        if (player.openInventory.topInventory == container.inventory) return

        // check permissions before opening
        if (!hasPermissionToOpenContainer(player, block, face)) return

        player.openInventory(container.inventory)
    }

    private fun hasPermissionToOpenContainer(player: Player, block: Block, face: BlockFace): Boolean {
        // test if player can open the container (permissions, other plugins) by firing an event on the container
        val interactEvent = PermCheckPlayerInteractEvent(
            player, Action.RIGHT_CLICK_BLOCK, player.inventory.itemInMainHand, block, face
        )
        server.pluginManager.callEvent(interactEvent)

        return interactEvent.useInteractedBlock() != Event.Result.DENY
    }

    // Opens player enderchest if the clock is an enderchest and returns true. Returns false otherwise.
    private fun tryOpeningEnderchest(player: Player, block: Block): Boolean {
        if (block.type != Material.ENDER_CHEST) return false
        if (player.openInventory.topInventory != player.enderChest) {
            player.openInventory(player.enderChest)

            // we already succeeded in the main function, now optionally trigger opening handler for a nicer effect
            if (config.enderChestTracking) {
                val enderchest = block.state as? EnderChest
                if (enderchest != null)
                    enderChestOpeningTracker.enderChestOpenedBy(player, enderchest)
            }
        }
        return true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun onInventoryClosed(event: InventoryCloseEvent) {
        if (!config.enderChestTracking) return

        if (event.inventory == event.player.enderChest) {
            enderChestOpeningTracker.enderChestClosedBy(event.player)
        }
    }
}

private class EnderChestOpeningTracker {
    private val enderChestOpenings = WeakHashMap<EnderChest, Int>()
    private val openedEnderChests = WeakHashMap<HumanEntity, EnderChest>()

    fun init() {}

    fun enderChestOpenedBy(player: HumanEntity, enderChest: EnderChest) {
        if (openedEnderChests.putIfAbsent(player, enderChest) == null) {
            // no ender chest was opened by this player, increase count
            enderChestOpenings.compute(enderChest) { _, count -> (count ?: 0) + 1 }!!
            // ender chest must now be open
            if (!enderChest.isOpen)
                enderChest.open()
        }
    }

    fun enderChestClosedBy(player: HumanEntity) {
        // try get enderchest linked to player opening, or return
        val enderChest = openedEnderChests.remove(player) ?: return
        // decrease this enderchest's counter, if it reaches 0 or less, remove from map and return 0
        val nowOpened = enderChestOpenings.compute(enderChest) { _, count ->
            val newCount = (count ?: 0) - 1
            if (newCount <= 0) return@compute null
            newCount
        } ?: 0
        // if opened count is now 0, close enderchest
        if (nowOpened == 0 && enderChest.isOpen)
            enderChest.close()
    }
}