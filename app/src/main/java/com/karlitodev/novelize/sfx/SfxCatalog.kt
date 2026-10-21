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
            keywords = listOf("sword", "blade", "clash", "parry", "épée", "lame", "fer", "entrechoquèrent", "ferraille")
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
            keywords = listOf("slash", "strike", "cut", "trancha", "coup d'épée", "entailla", "fendit l'air")
        ),
        SfxItem(
            id = "arrow_shot",
            title = "Arrow Shot",
            fileName = "arrow_shot.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("arrow", "bow", "bowstring", "flèche", "arc", "décoche", "trait")
        ),
        SfxItem(
            id = "punch_hit",
            title = "Punch Hit",
            fileName = "punch_hit.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("punch", "fist", "blow", "kick", "poing", "coup", "frappe", "baffe", "direct")
        ),
        SfxItem(
            id = "shield_block",
            title = "Shield Block",
            fileName = "shield_block.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("shield", "blocked", "bouclier", "bloqua", "parade", "égide")
        ),
        SfxItem(
            id = "explosion",
            title = "Explosion",
            fileName = "explosion.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("explosion", "blast", "detonation", "déflagration", "explosa", "détonation", "fracas")
        ),
        SfxItem(
            id = "monster_roar",
            title = "Monster Roar",
            fileName = "monster_roar.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("roar", "growl", "beast", "rugissement", "hurlement", "bête", "monstre", "grognement")
        ),
        SfxItem(
            id = "whoosh",
            title = "Whoosh",
            fileName = "whoosh.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("whoosh", "dash", "dodge", "esquive", "sillage", "fulgurance", "vif")
        ),
        SfxItem(
            id = "body_fall",
            title = "Body Fall",
            fileName = "body_fall.mp3",
            isLoop = false,
            category = SfxCategory.COMBAT,
            keywords = listOf("collapsed", "fell to the ground", "s'effondra", "tomba lourdement", "chuta")
        ),

        // Magic & Fantasy (11-20)
        SfxItem(
            id = "magic_cast",
            title = "Magic Cast",
            fileName = "magic_cast.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("spell", "chant", "incantation", "magie", "sortilège", "sort", "mana")
        ),
        SfxItem(
            id = "fireball",
            title = "Fireball",
            fileName = "fireball.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("fireball", "flame", "blaze", "boule de feu", "flammes", "incendie", "brasier")
        ),
        SfxItem(
            id = "ice_crystal",
            title = "Ice Crystal",
            fileName = "ice_crystal.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("ice", "frost", "frozen", "crystal", "glace", "gel", "cristal", "givre")
        ),
        SfxItem(
            id = "lightning_strike",
            title = "Lightning Strike",
            fileName = "lightning_strike.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("lightning", "thunderbolt", "electric", "éclair", "foudre", "tonnerre", "étincelle")
        ),
        SfxItem(
            id = "healing_chime",
            title = "Healing Chime",
            fileName = "healing_chime.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("heal", "recovery", "restored", "soin", "guérison", "restaura", "lueur bienveillante")
        ),
        SfxItem(
            id = "magic_circle",
            title = "Magic Circle",
            fileName = "magic_circle.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("magic circle", "barrier", "rune", "cercle magique", "barrière", "glyphe")
        ),
        SfxItem(
            id = "summoning",
            title = "Summoning",
            fileName = "summoning.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("summon", "invoked", "apparition", "invocation", "invoqua", "esprit")
        ),
        SfxItem(
            id = "teleport",
            title = "Teleport",
            fileName = "teleport.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("teleport", "portal", "flash of light", "téléportation", "portail", "disparut soudain")
        ),
        SfxItem(
            id = "dark_aura",
            title = "Dark Aura",
            fileName = "dark_aura.mp3",
            isLoop = true,
            category = SfxCategory.MAGIC,
            keywords = listOf("dark aura", "forbidden magic", "sinister aura", "aura sombre", "maléfique", "énergie noire")
        ),
        SfxItem(
            id = "level_up",
            title = "Level Up Chime",
            fileName = "level_up.mp3",
            isLoop = false,
            category = SfxCategory.MAGIC,
            keywords = listOf("level up", "status window", "skill acquired", "niveau supérieur", "compétence acquise", "statistiques")
        ),

        // Ambient Loops (21-30)
        SfxItem(
            id = "ambient_forest",
            title = "Forest Ambience",
            fileName = "ambient_forest.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("forest", "woods", "trees", "leaves", "forêt", "bois", "arbres", "clairière")
        ),
        SfxItem(
            id = "ambient_rain",
            title = "Rain",
            fileName = "ambient_rain.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("rain", "downpour", "shower", "pluie", "averse", "gouttes d'eau")
        ),
        SfxItem(
            id = "ambient_storm",
            title = "Thunderstorm",
            fileName = "ambient_storm.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("storm", "thunder", "tempête", "orage", "ouragan")
        ),
        SfxItem(
            id = "ambient_wind",
            title = "Wind",
            fileName = "ambient_wind.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("wind", "breeze", "gale", "vent", "brise", "souffle", "rafale")
        ),
        SfxItem(
            id = "ambient_ocean",
            title = "Ocean Waves",
            fileName = "ambient_ocean.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("ocean", "sea", "waves", "beach", "océan", "mer", "vagues", "rivage", "marée")
        ),
        SfxItem(
            id = "ambient_river",
            title = "River Stream",
            fileName = "ambient_river.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("river", "stream", "creek", "rivière", "ruisseau", "cours d'eau")
        ),
        SfxItem(
            id = "ambient_campfire",
            title = "Campfire",
            fileName = "ambient_campfire.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("campfire", "fire", "hearth", "feu de camp", "braises", "âtre")
        ),
        SfxItem(
            id = "ambient_night",
            title = "Night Crickets",
            fileName = "ambient_night.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("crickets", "quiet night", "nuit calme", "grillons", "nocturne")
        ),
        SfxItem(
            id = "ambient_city",
            title = "City Street",
            fileName = "ambient_city.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("city", "street", "market", "ville", "rues", "marché", "place publique")
        ),
        SfxItem(
            id = "ambient_tavern",
            title = "Tavern Ambience",
            fileName = "ambient_tavern.mp3",
            isLoop = true,
            category = SfxCategory.AMBIENT,
            keywords = listOf("tavern", "inn", "pub", "taverne", "auberge", "comptoir")
        ),

        // Places & Daily Life (31-38)
        SfxItem(
            id = "door_open",
            title = "Door Open",
            fileName = "door_open.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("door opened", "opened the door", "ouvrit la porte", "porte s'ouvrit")
        ),
        SfxItem(
            id = "door_slam",
            title = "Door Slam",
            fileName = "door_slam.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("door slammed", "slammed the door", "claqua la porte", "porte claqua")
        ),
        SfxItem(
            id = "door_knock",
            title = "Knock on Door",
            fileName = "door_knock.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("knocked on the door", "knock at the door", "frappa à la porte", "toqua")
        ),
        SfxItem(
            id = "steps_stone",
            title = "Stone Footsteps",
            fileName = "steps_stone.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("footsteps", "walked", "corridor", "pas", "marqua un pas", "marcha", "pavés")
        ),
        SfxItem(
            id = "steps_grass",
            title = "Grass Footsteps",
            fileName = "steps_grass.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("steps in the grass", "walked on grass", "pas dans l'herbe", "foulant l'herbe")
        ),
        SfxItem(
            id = "school_bell",
            title = "School Bell",
            fileName = "school_bell.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("school bell", "chime", "cloche de l'école", "sonnerie de classe")
        ),
        SfxItem(
            id = "mug_clink",
            title = "Mug Clink",
            fileName = "mug_clink.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("cheers", "clinked mugs", "toast", "trinquèrent", "chope", "verre")
        ),
        SfxItem(
            id = "page_turn",
            title = "Page Turn",
            fileName = "page_turn.mp3",
            isLoop = false,
            category = SfxCategory.PLACES,
            keywords = listOf("turned the page", "grimoire", "tourna la page", "feuilleta")
        ),

        // Mood & Tension (39-45)
        SfxItem(
            id = "heartbeat",
            title = "Heartbeat",
            fileName = "heartbeat.mp3",
            isLoop = true,
            category = SfxCategory.MOOD,
            keywords = listOf("heartbeat", "pulse", "terror", "battement de cœur", "pouls", "panique", "angoisse")
        ),
        SfxItem(
            id = "tension_drone",
            title = "Tension Drone",
            fileName = "tension_drone.mp3",
            isLoop = true,
            category = SfxCategory.MOOD,
            keywords = listOf("tension", "uneasy", "dread", "malaise", "frisson", "oppressant")
        ),
        SfxItem(
            id = "dramatic_sting",
            title = "Dramatic Sting",
            fileName = "dramatic_sting.mp3",
            isLoop = false,
            category = SfxCategory.MOOD,
            keywords = listOf("shock", "revelation", "plot twist", "stupeur", "sidéré", "révélation")
        ),
        SfxItem(
            id = "suspense_riser",
            title = "Suspense Riser",
            fileName = "suspense_riser.mp3",
            isLoop = false,
            category = SfxCategory.MOOD,
            keywords = listOf("suspense", "mounting fear", "suspense grandissant", "montée d'adrénaline")
        ),
        SfxItem(
            id = "eerie_wind",
            title = "Eerie Wind",
            fileName = "eerie_wind.mp3",
            isLoop = true,
            category = SfxCategory.MOOD,
            keywords = listOf("eerie", "ghostly", "spectral", "spectral", "lugubre", "vent fantomatique")
        ),
        SfxItem(
            id = "glass_shatter",
            title = "Glass Shatter",
            fileName = "glass_shatter.mp3",
            isLoop = false,
            category = SfxCategory.MOOD,
            keywords = listOf("shattered", "broken glass", "bris de verre", "verre brisé", "fracassa")
        ),
        SfxItem(
            id = "bell_toll",
            title = "Bell Toll",
            fileName = "bell_toll.mp3",
            isLoop = false,
            category = SfxCategory.MOOD,
            keywords = listOf("bell tolled", "funeral bell", "cloche sonna", "glas", "beffroi")
        ),

        // Travel & Vehicles (46-50)
        SfxItem(
            id = "horse_gallop",
            title = "Horse Gallop",
            fileName = "horse_gallop.mp3",
            isLoop = true,
            category = SfxCategory.TRAVEL,
            keywords = listOf("horse", "gallop", "riding", "cheval", "galop", "chevaucha", "monture")
        ),
        SfxItem(
            id = "carriage_wheels",
            title = "Carriage Wheels",
            fileName = "carriage_wheels.mp3",
            isLoop = true,
            category = SfxCategory.TRAVEL,
            keywords = listOf("carriage", "wagon", "cart", "carriole", "roues", "calèche", "charrette")
        ),
        SfxItem(
            id = "ship_creak",
            title = "Ship Creaking",
            fileName = "ship_creak.mp3",
            isLoop = true,
            category = SfxCategory.TRAVEL,
            keywords = listOf("ship", "boat", "sailing", "navire", "bateau", "coque en bois", "gréement")
        ),
        SfxItem(
            id = "train_pass",
            title = "Train Passing",
            fileName = "train_pass.mp3",
            isLoop = false,
            category = SfxCategory.TRAVEL,
            keywords = listOf("train", "locomotive", "railway", "train", "voie ferrée", "rame")
        ),
        SfxItem(
            id = "car_engine",
            title = "Car Engine",
            fileName = "car_engine.mp3",
            isLoop = true,
            category = SfxCategory.TRAVEL,
            keywords = listOf("car", "engine", "vehicle", "voiture", "moteur", "automobile")
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
