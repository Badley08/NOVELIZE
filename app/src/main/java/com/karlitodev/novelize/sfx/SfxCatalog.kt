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

enum class SfxCategory(val displayName: String) {
    COMBAT("Combat & Action"),
    MAGIC("Magic & Fantasy"),
    AMBIENT("Ambient Loops"),
    PLACES("Places & Daily Life"),
    MOOD("Mood & Tension"),
    TRAVEL("Travel & Vehicles")
}

/**
 * Predefined catalog of all 50 sound effects matching the project SFX specification.
 */
object SfxCatalog {
    val items = listOf(
        // Combat & Action (1-10)
        SfxItem(
            id = "sword_clash",
            title = "Sword Clash",
            fileName = "sword_clash.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("sword clash", "blades clashed", "parried", "épée", "lame", "entrechoquèrent", "ferraille")
        ),
        SfxItem(
            id = "sword_draw",
            title = "Sword Draw",
            fileName = "sword_draw.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("sheath", "drew his sword", "drew her sword", "unsheathed", "dégaina", "fourreau", "tira son épée")
        ),
        SfxItem(
            id = "sword_slash",
            title = "Sword Slash",
            fileName = "sword_slash.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("slash", "strike", "trancha", "coup d'épée", "entailla", "fendit l'air")
        ),
        SfxItem(
            id = "arrow_shot",
            title = "Arrow Shot",
            fileName = "arrow_shot.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("arrow", "bowstring", "flèche", "tir à l'arc", "décoche", "flèches")
        ),
        SfxItem(
            id = "punch_hit",
            title = "Punch Hit",
            fileName = "punch_hit.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("punch", "fist", "coup de poing", "frappa du poing", "baffe", "uppercut")
        ),
        SfxItem(
            id = "shield_block",
            title = "Shield Block",
            fileName = "shield_block.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("shield", "blocked with his shield", "bouclier", "bloqua le coup", "parade au bouclier")
        ),
        SfxItem(
            id = "explosion",
            title = "Explosion",
            fileName = "explosion.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("explosion", "detonation", "déflagration", "explosa", "détonation", "fracas")
        ),
        SfxItem(
            id = "monster_roar",
            title = "Monster Roar",
            fileName = "monster_roar.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("roar", "growl", "rugissement", "hurlement de bête", "grognement féroce")
        ),
        SfxItem(
            id = "whoosh",
            title = "Whoosh",
            fileName = "whoosh.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("whoosh", "dash", "dodge", "esquive rapide", "fulgurance")
        ),
        SfxItem(
            id = "body_fall",
            title = "Body Fall",
            fileName = "body_fall.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("collapsed", "fell to the ground", "s'effondra", "tomba lourdement", "chuta au sol")
        ),

        // Magic & Fantasy (11-20)
        SfxItem(
            id = "magic_cast",
            title = "Magic Cast",
            fileName = "magic_cast.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("incantation", "sortilège", "formule magique", "lance un sort", "mana")
        ),
        SfxItem(
            id = "fireball",
            title = "Fireball",
            fileName = "fireball.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("fireball", "boule de feu", "gerbe de flammes", "brasier ardent")
        ),
        SfxItem(
            id = "ice_crystal",
            title = "Ice Crystal",
            fileName = "ice_crystal.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("ice crystal", "frost", "cristal de glace", "givre mordant", "congélation")
        ),
        SfxItem(
            id = "lightning_strike",
            title = "Lightning Strike",
            fileName = "lightning_strike.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("lightning strike", "thunderbolt", "éclair", "foudre", "coup de tonnerre")
        ),
        SfxItem(
            id = "healing_chime",
            title = "Healing Chime",
            fileName = "healing_chime.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("healing magic", "sort de soin", "guérison miraculeuse", "lueur bienveillante")
        ),
        SfxItem(
            id = "magic_circle",
            title = "Magic Circle",
            fileName = "magic_circle.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("magic circle", "runic barrier", "cercle magique", "barrière runique")
        ),
        SfxItem(
            id = "summoning",
            title = "Summoning",
            fileName = "summoning.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("summoning ritual", "invoqua la créature", "rituel d'invocation")
        ),
        SfxItem(
            id = "teleport",
            title = "Teleport",
            fileName = "teleport.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("teleported", "magic portal", "téléportation", "portail dimensionnel")
        ),
        SfxItem(
            id = "dark_aura",
            title = "Dark Aura",
            fileName = "dark_aura.mp3",
            isLoop = true,
            category = SfxCategory.MAGIC,
            keywords = listOf("dark aura", "forbidden magic", "aura maléfique", "énergie sombre")
        ),
        SfxItem(
            id = "level_up",
            title = "Level Up Chime",
            fileName = "level_up.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("level up", "status window", "niveau supérieur", "montée de niveau")
        ),

        // Ambient Loops (21-30)
        SfxItem(
            id = "ambient_forest",
            title = "Forest Ambience",
            fileName = "ambient_forest.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("dense forest", "deep woods", "forêt profonde", "au cœur de la forêt")
        ),
        SfxItem(
            id = "ambient_rain",
            title = "Rain",
            fileName = "ambient_rain.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("heavy rain", "downpour", "pluie battante", "averse torrentielle")
        ),
        SfxItem(
            id = "ambient_storm",
            title = "Thunderstorm",
            fileName = "ambient_storm.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("thunderstorm", "tempête déchaînée", "orage violent")
        ),
        SfxItem(
            id = "ambient_wind",
            title = "Wind",
            fileName = "ambient_wind.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("howling wind", "vent glacial", "rafale sifflante")
        ),
        SfxItem(
            id = "ambient_ocean",
            title = "Ocean Waves",
            fileName = "ambient_ocean.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("ocean waves", "mer agitée", "fracas des vagues")
        ),
        SfxItem(
            id = "ambient_river",
            title = "River Stream",
            fileName = "ambient_river.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("flowing river", "ruisseau limpide", "cours d'eau tumultueux")
        ),
        SfxItem(
            id = "ambient_campfire",
            title = "Campfire",
            fileName = "ambient_campfire.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("campfire", "crépitement du feu", "feu de camp")
        ),
        SfxItem(
            id = "ambient_night",
            title = "Night Crickets",
            fileName = "ambient_night.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("night crickets", "nuit paisible", "chant des grillons")
        ),
        SfxItem(
            id = "ambient_city",
            title = "City Street",
            fileName = "ambient_city.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("bustling city", "marché animé", "foule citadine")
        ),
        SfxItem(
            id = "ambient_tavern",
            title = "Tavern Ambience",
            fileName = "ambient_tavern.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("noisy tavern", "auberge bruyante", "salle de taverne")
        ),

        // Places & Daily Life (31-38)
        SfxItem(
            id = "door_open",
            title = "Door Open",
            fileName = "door_open.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("opened the door", "ouvrit la porte", "porte s'ouvrit")
        ),
        SfxItem(
            id = "door_slam",
            title = "Door Slam",
            fileName = "door_slam.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("slammed the door", "claqua la porte", "porte claqua avec force")
        ),
        SfxItem(
            id = "door_knock",
            title = "Knock on Door",
            fileName = "door_knock.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("knocked at the door", "frappa à la porte", "toqua à la porte")
        ),
        SfxItem(
            id = "steps_stone",
            title = "Stone Footsteps",
            fileName = "steps_stone.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("footsteps on stone", "bruit de pas lourds", "pas résonnèrent")
        ),
        SfxItem(
            id = "steps_grass",
            title = "Grass Footsteps",
            fileName = "steps_grass.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("footsteps in the grass", "foulant l'herbe haute")
        ),
        SfxItem(
            id = "school_bell",
            title = "School Bell",
            fileName = "school_bell.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("school bell", "cloche de l'académie", "sonnerie de l'école")
        ),
        SfxItem(
            id = "mug_clink",
            title = "Mug Clink",
            fileName = "mug_clink.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("clinked glasses", "trinquèrent joyeusement", "choc des chopes")
        ),
        SfxItem(
            id = "page_turn",
            title = "Page Turn",
            fileName = "page_turn.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("turned the page", "tourna la page", "feuilleta le grimoire")
        ),

        // Mood & Tension (39-45)
        SfxItem(
            id = "heartbeat",
            title = "Heartbeat",
            fileName = "heartbeat.mp3",
            isLoop = true,
            category = SfxCategory.MOOD,
            keywords = listOf("heart pounded", "heart beating fast", "battements de cœur affolés", "pouls qui s'accélère")
        ),
        SfxItem(
            id = "tension_drone",
            title = "Tension Drone",
            fileName = "tension_drone.mp3",
            isLoop = true,
            category = SfxCategory.MOOD,
            keywords = listOf("oppressive silence", "chilling tension", "tension insoutenable", "silence oppressant")
        ),
        SfxItem(
            id = "dramatic_sting",
            title = "Dramatic Sting",
            fileName = "dramatic_sting.mp3",
            isLoop = false,
            category = SfxCategory.MOOD,
            keywords = listOf("sudden revelation", "plot twist", "stupeur totale", "révélation choquante")
        ),
        SfxItem(
            id = "suspense_riser",
            title = "Suspense Riser",
            fileName = "suspense_riser.mp3",
            isLoop = false,
            category = SfxCategory.MOOD,
            keywords = listOf("mounting dread", "suspense grandissant", "angoisse montante")
        ),
        SfxItem(
            id = "eerie_wind",
            title = "Eerie Wind",
            fileName = "eerie_wind.mp3",
            isLoop = true,
            category = SfxCategory.MOOD,
            keywords = listOf("eerie whistling", "sinister gust", "souffle spectral", "vent sinistre")
        ),
        SfxItem(
            id = "glass_shatter",
            title = "Glass Shatter",
            fileName = "glass_shatter.mp3",
            isLoop = false,
            category = SfxCategory.MOOD,
            keywords = listOf("glass shattered", "window smashed", "verre se brisa", "vitre vola en éclats")
        ),
        SfxItem(
            id = "bell_toll",
            title = "Bell Toll",
            fileName = "bell_toll.mp3",
            isLoop = false,
            category = SfxCategory.MOOD,
            keywords = listOf("funeral bell", "death knell", "glas funèbre", "le glas sonna")
        ),

        // Travel & Vehicles (46-50)
        SfxItem(
            id = "horse_gallop",
            title = "Horse Gallop",
            fileName = "horse_gallop.mp3",
            isLoop = true,
            category = SfxCategory.TRAVEL,
            keywords = listOf("horse galloping", "riding fast", "cheval au galop", "galop effréné")
        ),
        SfxItem(
            id = "carriage_wheels",
            title = "Carriage Wheels",
            fileName = "carriage_wheels.mp3",
            isLoop = true,
            category = SfxCategory.TRAVEL,
            keywords = listOf("carriage rolled", "cart wheels", "calèche s'ébranla", "roues de la charrette")
        ),
        SfxItem(
            id = "ship_creak",
            title = "Ship Creaking",
            fileName = "ship_creak.mp3",
            isLoop = true,
            category = SfxCategory.TRAVEL,
            keywords = listOf("ship creaked", "wooden hull", "coque du navire", "grincement du bateau")
        ),
        SfxItem(
            id = "train_pass",
            title = "Train Passing",
            fileName = "train_pass.mp3",
            isLoop = false,
            category = SfxCategory.TRAVEL,
            keywords = listOf("train whistled", "locomotive rushed", "sifflet du train", "passage du train")
        ),
        SfxItem(
            id = "car_engine",
            title = "Car Engine",
            fileName = "car_engine.mp3",
            isLoop = true,
            category = SfxCategory.TRAVEL,
            keywords = listOf("engine roared", "car accelerated", "moteur vrombit", "démarrage de la voiture")
        )
    )

    private fun matchesKeyword(text: String, keyword: String): Boolean {
        val pattern = Regex("(?:^|[^\\p{L}\\p{N}])" + Regex.escape(keyword) + "(?:$|[^\\p{L}\\p{N}])", RegexOption.IGNORE_CASE)
        return pattern.containsMatchIn(text)
    }

    fun findSfxCue(text: String): SfxItem? {
        if (text.length < 8) return null
        return items.firstOrNull { item ->
            !item.isLoop && item.keywords.any { matchesKeyword(text, it) }
        }
    }

    fun findAmbientCue(text: String): SfxItem? {
        if (text.length < 12) return null
        return items.firstOrNull { item ->
            item.isLoop && item.keywords.any { matchesKeyword(text, it) }
        }
    }
}
