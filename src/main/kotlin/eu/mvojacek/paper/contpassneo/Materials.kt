package eu.mvojacek.paper.contpassneo

import org.bukkit.Material

@Suppress("MemberVisibilityCanBePrivate")
object Materials {
    val signMaterials: Set<Material> = Material.entries.filterTo(HashSet()) { it.name.endsWith("_SIGN") }

    val dyeMaterials: Set<Material> = Material.entries.filterTo(HashSet()) { it.name.endsWith("_DYE") }

    val signWaxMaterial: Material = Material.HONEYCOMB
    val signGlowMaterial: Material = Material.GLOW_INK_SAC
    val signUnglowMaterial: Material = Material.INK_SAC

    fun isSign(material: Material): Boolean = signMaterials.contains(material)
    fun isDye(material: Material): Boolean = dyeMaterials.contains(material)
    fun canWaxSign(material: Material): Boolean = material == signWaxMaterial

    fun canGlowSign(material: Material): Boolean = material == signGlowMaterial

    fun canUnglowSign(material: Material): Boolean = material == signUnglowMaterial

    fun canEditSignGlow(material: Material): Boolean = canGlowSign(material) || canUnglowSign(material)
}