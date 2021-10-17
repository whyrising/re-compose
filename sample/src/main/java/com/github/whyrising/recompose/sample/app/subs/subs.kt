package com.github.whyrising.recompose.sample.app.subs

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.github.whyrising.recompose.regSub
import com.github.whyrising.recompose.regSubM
import com.github.whyrising.recompose.sample.app.Keys.formattedTime
import com.github.whyrising.recompose.sample.app.Keys.materialThemeColors
import com.github.whyrising.recompose.sample.app.Keys.primaryColor
import com.github.whyrising.recompose.sample.app.Keys.primaryColorName
import com.github.whyrising.recompose.sample.app.Keys.secondaryColor
import com.github.whyrising.recompose.sample.app.Keys.secondaryColorName
import com.github.whyrising.recompose.sample.app.Keys.statusBarDarkIcons
import com.github.whyrising.recompose.sample.app.Keys.time
import com.github.whyrising.recompose.sample.app.db.AppSchema
import com.github.whyrising.recompose.sample.util.toColor
import com.github.whyrising.recompose.subs.React
import com.github.whyrising.recompose.subscribe
import com.github.whyrising.y.collections.core.v
import com.github.whyrising.y.collections.vector.IPersistentVector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getTime(db: AppSchema, query: IPersistentVector<Any>): Date = db.time

fun getPrimaryColorName(db: AppSchema, query: IPersistentVector<Any>): String =
    db.primaryColor

fun getSecondaryColorName(
    db: AppSchema,
    query: IPersistentVector<Any>
): String = db.secondaryColor

fun stringToColor(colorName: String, query: IPersistentVector<Any>): Color {
    val (_, defaultColor) = query
    return toColor(
        stringColor = colorName.lowercase(),
        default = defaultColor as Color
    )
}

/**
 * @param colors is a vector of colors that should contain two colors:
 * primary and secondary in the given order
 * @param query is the query vector
 *
 * @return a new theme colors using the given primary and secondary colors
 */
fun themeColors(
    colors: IPersistentVector<Color>,
    query: IPersistentVector<Any>
): Colors {
    val (primaryColor, secondaryColor) = colors
    val (_, colorPalette) = query

    return (colorPalette as Colors).copy(
        primary = primaryColor,
        secondary = secondaryColor
    )
}

const val HH_MM_SS = "HH:mm:ss"

fun formattedTime(date: Date, query: IPersistentVector<Any>): String {
    val simpleDateFormat = SimpleDateFormat(HH_MM_SS, Locale.getDefault())
    return simpleDateFormat.format(date)
}

fun isLightColor(color: Color, queryVec: IPersistentVector<Any>): Boolean {
    return color.luminance() >= 0.5f
}

fun primaryColorNameReaction(query: IPersistentVector<Any>): React<String> =
    subscribe(v(primaryColorName))

fun secondaryColorNameReaction(query: IPersistentVector<Any>): React<String> =
    subscribe(v(secondaryColorName))

fun primSecondColorReaction(
    query: IPersistentVector<Any>
): IPersistentVector<React<Color>> {
    val (_, _, defaultColor) = query
    return v(
        subscribe(v(primaryColor, defaultColor)),
        subscribe(v(secondaryColor, defaultColor))
    )
}

fun timeReaction(query: IPersistentVector<Any>): React<Date> =
    subscribe(v(time))

fun secondaryColorReaction(query: IPersistentVector<Any>): React<Color> {
    val (_, defaultColor) = query
    return subscribe(v(secondaryColor, defaultColor))
}

fun regSubs() {
    regSub(time, ::getTime)

    regSub(primaryColorName, ::getPrimaryColorName)

    regSub(secondaryColorName, ::getSecondaryColorName)

    regSub(
        queryId = primaryColor,
        signalsFn = ::primaryColorNameReaction,
        computationFn = ::stringToColor
    )

    regSub(
        queryId = secondaryColor,
        signalsFn = ::secondaryColorNameReaction,
        computationFn = ::stringToColor
    )

    regSubM(
        queryId = materialThemeColors,
        signalsFn = ::primSecondColorReaction,
        computationFn = ::themeColors
    )

    regSub(
        queryId = formattedTime,
        signalsFn = ::timeReaction,
        computationFn = ::formattedTime
    )

    regSub(
        queryId = statusBarDarkIcons,
        signalsFn = ::secondaryColorReaction,
        computationFn = ::isLightColor
    )
}
