package com.karlitodev.novelize.sample

import com.karlitodev.novelize.model.Book
import com.karlitodev.novelize.model.DocumentFormat
import com.karlitodev.novelize.parser.DefaultNovelParser

/**
 * Built-in demo light novel showcases voices, sound effects, translation, and chapter quizzes.
 */
object SampleNovels {

    val demoBook = Book(
        id = "demo_chrono_blade",
        title = "Chrono Blade: The Shattered Eclipse",
        author = "Karlito Dev",
        coverPath = null,
        dominantColorHex = 0xFF6C5CE7,
        format = DocumentFormat.TXT,
        filePath = "built_in:chrono_blade.txt",
        language = "en",
        totalChapters = 2
    )

    const val demoContent = """
CHAPTER 1: The Whispering Sanctuary

The dense canopy of the whispering forest shaded the mossy stone trail. Cool raindrops began tapping against the ancient leaves, a calm rain echoing through the twilight.

Ren gripped the hilt of his twin daggers, scanning the shadowy perimeter.

"Do you sense the disturbance ahead, Lyra?" Ren asked quietly.

"Yes. A surge of forbidden magic is gathering near the seal," replied Lyra, adjusting her enchanted sapphire circlet.

A sudden rumble shook the bedrock. The gentle drizzle mutated into a violent thunderstorm as lightning struck a jagged obelisk ahead.

"Stay back!" Ren shouted.

A colossal shadow lunged through the mist. Ren unsheathed his blade with a sharp sword slash and met the beast's claws in a deafening sword clash! Sparks showered the wet stones.

"Flames of purification, heed my call—Fireball!" cried Lyra, releasing a searing sphere of flame that detonated against the intruder.

The creature staggered back into the dark foliage, leaving behind an ornate glowing sigil pulsing upon the ground.

Ren exhaled slowly as his tense heartbeat gradually steadied in the quiet drizzle.

"This is only the vanguard," Lyra murmured softly. "We must reach the sanctuary gates before nightfall."

CHAPTER 2: The Echoes of the Spire

Stone footsteps echoed through the subterranean corridor of the ancient spire. The heavy door opened with a slow, grinding screech.

Ren and Lyra stepped into the circular library chamber. Countless grimoires hovered silently within glowing levitation rings.

"Look at this inscription," Ren pointed to a silver tablet on the central pedestal.

"It is written in an archaic dialect of the First Age," Lyra observed, tracing the carved runes with her fingertip.

"Can you translate its meaning?" Ren asked.

"It speaks of the Eclipse Key. Whoever unites the three celestial relics shall command the tides of time itself."

Outside, distant thunder rolled across the mountain peaks as their next trial drew near.
"""

    fun getDemoChapters() = DefaultNovelParser().parse(demoContent, demoBook.title)
}
