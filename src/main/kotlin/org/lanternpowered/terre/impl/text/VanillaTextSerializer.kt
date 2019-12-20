/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:JvmName("TextJsonSerializerKt")

package org.lanternpowered.terre.impl.text

import org.lanternpowered.terre.impl.item.ItemStackImpl
import org.lanternpowered.terre.impl.util.OptionalColor
import org.lanternpowered.terre.text.*
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.text.indexOf

internal fun TextImpl.toTaggedVanillaText(parentColor: OptionalColor = OptionalColor.empty()): FormattableText
    = toVanillaText(TaggedVanillaTextBuilder(), parentColor)

internal fun TextImpl.toPlainVanillaText(parentColor: OptionalColor = OptionalColor.empty()): FormattableText
    = toVanillaText(PlainVanillaTextBuilder(), parentColor)

/**
 * Converts this text component into a flattened text component.
 */
private fun TextImpl.toVanillaText(
    builder: AbstractVanillaTextBuilder, parentColor: OptionalColor
): FormattableText {
  appendToBuilder(builder, parentColor)
  return builder.build()
}

/**
 * A pattern to match message format groups.
 */
internal val formatPattern = "\\{([0-9]+)(?::[^}:]+)?}".toRegex()

/**
 * A builder to flatten text into optimized parts.
 */
private abstract class AbstractVanillaTextBuilder {

  protected val builder = StringBuilder()
  private val substitutions = mutableListOf<Text>()
  private var color = OptionalColor.empty()

  fun ensureCapacity(minimumCapacity: Int) {
    this.builder.ensureCapacity(minimumCapacity)
  }

  fun append(text: String, color: OptionalColor) {
    append(text, 0, text.length, color)
  }

  abstract fun appendLiteral(c: Char)

  fun append(text: String, start: Int, end: Int, color: OptionalColor) {
    switchColor(color)
    var index = text.indexOf(']', start, end)
    if (color.isEmpty && (index != -1 || text.indexOf('[', start, end) != -1)) {
      this.builder.ensureCapacity((end - start) * 2)
      for (i in start until end) {
        val c = text[i]
        if (c == '[' || c == ']') {
          appendLiteral(c)
        } else {
          this.builder.append(c)
        }
      }
      return
    }
    if (index == -1) {
      this.builder.append(text, start, end)
    } else {
      this.builder.append(text, start, index)
      while (true) {
        val next = text.indexOf(']', index + 1, end)
        resetColor()
        switchColor(color)
        if (next != -1) {
          this.builder.append(text, index, next)
        } else {
          this.builder.append(text, index, end)
          break
        }
        index = next
      }
    }
  }

  private fun switchColor(color: OptionalColor) {
    if (color != this.color) {
      if (this.color.isPresent) {
        stopColor()
      }
      if (color.isPresent) {
        startColor(color.value)
      }
      this.color = color
    }
  }

  abstract fun startColor(color: Color)

  abstract fun stopColor()

  abstract fun appendText(text: GlyphText)

  abstract fun appendText(text: ItemText)

  abstract fun appendText(text: AchievementText)

  fun append(text: GlyphText) {
    resetColor()
    appendText(text)
  }

  fun append(text: ItemText) {
    resetColor()
    appendText(text)
  }

  fun append(text: AchievementText) {
    resetColor()
    appendText(text)
  }

  fun appendSubstitution(text: Text, color: OptionalColor) {
    switchColor(color)
    this.builder.append('{').append(this.substitutions.size).append('}')
    this.substitutions += text
  }

  private fun resetColor() {
    if (this.color.isPresent) {
      stopColor()
    }
    this.color = OptionalColor.empty()
  }

  fun build(): FormattableText {
    resetColor()
    val content = this.builder.toString()
    return formattedTextOf(content, this.substitutions)
  }
}

private class PlainVanillaTextBuilder : AbstractVanillaTextBuilder() {

  override fun appendLiteral(c: Char) {
    this.builder.append(c)
  }

  override fun startColor(color: Color) {
  }

  override fun stopColor() {
  }

  override fun appendText(text: GlyphText) {
    this.builder.append(text.toPlain())
  }

  override fun appendText(text: ItemText) {
    this.builder.append(text.toPlain())
  }

  override fun appendText(text: AchievementText) {
    this.builder.append(text.toPlain())
  }
}

private class TaggedVanillaTextBuilder : AbstractVanillaTextBuilder() {

  override fun appendLiteral(c: Char) {
    this.builder.append("[l:").append(c).append(']')
  }

  override fun startColor(color: Color) {
    val hex = color.rgb.toString(16)
    this.builder.append("[c/").append(hex).append(':') // Color tag start
  }

  override fun stopColor() {
    this.builder.append(']')
  }

  override fun appendText(text: GlyphText) {
    val glyph = text.glyph as GlyphImpl
    this.builder.append("[g:").append(glyph.internalId).append(']')
  }

  override fun appendText(text: ItemText) {
    val itemStack = text.itemStack as ItemStackImpl
    // TODO: Add prefix
    this.builder.append("[i:").append(itemStack.item.numericId.value)
    if (itemStack.quantity > 1) {
      this.builder.append("/s").append(itemStack.quantity)
    }
    this.builder.append(']')
  }

  override fun appendText(text: AchievementText) {
    this.builder.append("[a:").append(text.achievement.id.id.toUpperCase()).append(']')
  }
}

/**
 * Flattens the text component structure in a list of plain components.
 */
private fun TextImpl.appendToBuilder(builder: AbstractVanillaTextBuilder, parentColor: OptionalColor) {
  val color = if (this.optionalColor.isPresent) this.optionalColor else parentColor
  when (this) {
    is LiteralTextImpl -> builder.append(this.literal, color)
    is LocalizedTextImpl -> builder.appendSubstitution(this, color)
    is AchievementTextImpl -> builder.append(this)
    is GlyphTextImpl -> builder.append(this)
    is ItemTextImpl -> builder.append(this)
    is GroupedTextImpl -> {
      for (child in this.children) {
        (child as TextImpl).appendToBuilder(builder, color)
      }
    }
    is FormattableTextImpl -> {
      val format = this.format
      builder.ensureCapacity(format.length)
      var index = 0
      while (index < format.length) {
        val match = formatPattern.find(format, index)
        if (match != null) {
          val substitutionIndex = match.groupValues[1].toInt()
          val substitution = if (substitutionIndex < this.substitutions.size)
            this.substitutions[substitutionIndex] else null

          val start = if (substitution == null) match.range.last + 1 else match.range.first
          if (start != index) {
            builder.append(format, index, start, color)
          }

          if (substitution != null) {
            (substitution as TextImpl).appendToBuilder(builder, color)
          }

          index = match.range.last + 1
        } else {
          builder.append(format, index, format.length, color)
          index = format.length
        }
      }
    }
    else -> throw IllegalArgumentException("Unsupported text type: $this")
  }
}
