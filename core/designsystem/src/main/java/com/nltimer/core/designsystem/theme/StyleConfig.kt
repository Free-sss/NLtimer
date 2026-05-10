package com.nltimer.core.designsystem.theme

enum class CornerPreset(val scale: Float) {
    COMPACT(0.25f),
    STANDARD(1.0f),
    ROUNDED(2.0f),
    SOFT(3.0f);
    companion object { val DEFAULT = STANDARD }
}

enum class BorderPreset(val scale: Float) {
    NONE(0.0f),
    THIN(0.5f),
    STANDARD(1.0f),
    THICK(2.0f);
    companion object { val DEFAULT = STANDARD }
}

enum class AlphaPreset(val scale: Float) {
    SUBTLE(0.5f),
    STANDARD(1.0f),
    VIVID(1.5f),
    SOLID(2.0f);
    companion object { val DEFAULT = STANDARD }
}

data class StyleConfig(
    val cornerPreset: CornerPreset = CornerPreset.DEFAULT,
    val borderPreset: BorderPreset = BorderPreset.DEFAULT,
    val alphaPreset: AlphaPreset = AlphaPreset.DEFAULT,
    val cornerScale: Float? = null,
    val borderScale: Float? = null,
    val alphaScale: Float? = null,
    val expressiveness: ExpressivenessPreset = ExpressivenessPreset.DEFAULT,
    val cardColorStrategy: CardColorStrategy = CardColorStrategy.DEFAULT,
    val iconContainerSize: IconContainerSize = IconContainerSize.DEFAULT,
    val timerTypography: TimerTypography = TimerTypography.DEFAULT,
    val wavyProgress: WavyProgressLevel = WavyProgressLevel.DEFAULT,
)

fun StyleConfig.effectiveCornerScale(): Float = cornerScale ?: cornerPreset.scale
fun StyleConfig.effectiveBorderScale(): Float = borderScale ?: borderPreset.scale
fun StyleConfig.effectiveAlphaScale(): Float = alphaScale ?: alphaPreset.scale
