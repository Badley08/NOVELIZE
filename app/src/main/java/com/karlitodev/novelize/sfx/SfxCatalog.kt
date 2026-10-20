package com.karlitodev.novelize.sfx

/**
 * Metadata descriptor for sound effects and ambient loops.
 */
data class SfxItem(
    val id: String,
    val title: String,
    val fileName: String,
    val isLoop: Boolean,
    val category: SfxCategory,
    val keywords: List<String>
)

enum class SfxCategory {
    COMBAT,
    MAGIC,
    AMBIENT,
    PLACES,
    MOOD,
    TRAVEL
}

/**
 * Predefined catalog of sound effects matching the project SFX checklist.
 */
object SfxCatalog {
    val items = listOf(
        SfxItem(
            id = "sword_clash",
            title = "Sword Clash",
            fileName = "sword_clash.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("sword", "blade", "clash", "parry", "épée", "lame", "fer")
        ),
        SfxItem(
            id = "sword_slash",
            title = "Sword Slash",
            fileName = "sword_slash.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("slash", "strike", "cut", "trancha", "coup d'épée", "entailla")
        ),
        SfxItem(
            id = "explosion",
            title = "Explosion",
            fileName = "explosion.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("explosion", "blast", "detonation", "déflagration", "explosa")
        ),
        SfxItem(
            id = "magic_cast",
            title = "Magic Cast",
            fileName = "magic_cast.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("spell", "chant", "incantation", "magie", "sortilège", "sort")
        ),
        SfxItem(
            id = "fireball",
            title = "Fireball",
            fileName = "fireball.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("fireball", "flame", "blaze", "boule de feu", "flammes", "incendie")
        ),
        SfxItem(
            id = "lightning_strike",
            title = "Lightning Strike",
            fileName = "lightning_strike.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("lightning", "thunderbolt", "éclair", "foudre")
        ),
        SfxItem(
            id = "ambient_forest",
            title = "Forest Ambience",
            fileName = "ambient_forest.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("forest", "woods", "trees", "leaves", "forêt", "bois", "arbres")
        ),
        SfxItem(
            id = "ambient_rain",
            title = "Rain",
            fileName = "ambient_rain.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("rain", "downpour", "shower", "pluie", "averse")
        ),
        SfxItem(
            id = "ambient_storm",
            title = "Thunderstorm",
            fileName = "ambient_storm.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("storm", "thunder", "tempête", "orage", "tonnerre")
        ),
        SfxItem(
            id = "ambient_campfire",
            title = "Campfire",
            fileName = "ambient_campfire.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("campfire", "fire", "hearth", "feu de camp", "braises")
        ),
        SfxItem(
            id = "door_open",
            title = "Door Open",
            fileName = "door_open.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("door opened", "opened the door", "ouvrit la porte", "porte s'ouvrit")
        ),
        SfxItem(
            id = "steps_stone",
            title = "Stone Footsteps",
            fileName = "steps_stone.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("footsteps", "walked", "paved", "pas", "marqua un pas", "marcha")
        ),
        SfxItem(
            id = "heartbeat",
            title = "Heartbeat",
            fileName = "heartbeat.mp3",
            isLoop = true,
            category = SfxCategory.MOOD,
            keywords = listOf("heartbeat", "pulse", "terror", "battement de cœur", "pouls", "panique")
        )
    )

    fun findSfxCue(text: String): SfxItem? {
        val lower = text.lowercase()
        return items.firstOrNull { item ->
            !item.isLoop && item.keywords.any { lower.contains(it) }
        }
    }

    fun findAmbientCue(text: String): SfxItem? {
        val lower = text.lowercase()
        return items.firstOrNull { item ->
            item.isLoop && item.keywords.any { lower.contains(it) }
        }
    }
}
