package eu.mvojacek.paper.contpassneo

import org.bukkit.configuration.file.FileConfiguration
import kotlin.reflect.KProperty


class Config(
    inner: FileConfiguration
) : ConfigBase(inner) {
    var enable: Boolean by configBool("enable", true)

    var passthroughPainting by configBool("passthrough.painting", true)

    var passthroughItemFrame by configBool("passthrough.item_frame", true)

    var passthroughGlowItemFrame by configBool("passthrough.glow_item_frame", true)

    var passthroughSign by configBool("passthrough.sign", true)

    var signAllowDye by configBool("sign.allow_dye", true)

    var signAllowEditGlow by configBool("sign.allow_edit_glow", true)

    var signAllowWax by configBool("sign.allow_wax", true)

    var disableWhileSneaking by configBool("disable_while_sneaking", true)

    var allowEnderChest by configBool("allow_enderchest", true)

    var enderChestTracking by configBool("debug.track_opened_enderchests_for_animation", true)
}


open class ConfigBase(
    val fileConfiguration: FileConfiguration
) {
    private val defaultsMap: MutableMap<String, Any> = HashMap()

    fun addDefaults() {
        fileConfiguration.addDefaults(defaultsMap)
    }

    @Suppress("SameParameterValue")
    protected fun configBool(key: String, default: Boolean? = null) =
        cachedConfig(key, default) { getBoolean(key) }

    private fun <T> cachedConfig(
        key: String,
        default: T? = null,
        getter: FileConfiguration.(String) -> T
    ): CachedConfigValue<T> {
        if (default != null) defaultsMap[key] = default
        return CachedConfigValue(key, { k -> fileConfiguration.getter(k) }, fileConfiguration::set)
    }

    protected class CachedConfigValue<T>(
        private val key: String,
        private val getter: (String) -> T,
        private val setter: (String, T) -> Unit
    ) {
        private var cachedValue: T? = null

        private var value: T
            get() {
                if (cachedValue == null) {
                    cachedValue = getter(key)
                }
                return cachedValue!!
            }
            set(value) {
                setter(key, value)
                cachedValue = value
            }

        operator fun getValue(config: Config, property: KProperty<*>): T {
            return value
        }

        operator fun setValue(config: Config, property: KProperty<*>, value: T) {
            this.value = value
        }
    }
}



