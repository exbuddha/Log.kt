// © Copyright 2021 Alireza Kamran

package katie.module.internal

internal var logger: LogFunc = Logger.On

internal fun <T> draw(obj: T?, vararg params: Any?): String =
    "$obj"

internal inline fun <T, P : Pair<T, *>> logp(time: Boolean = true, turn: Boolean = time, block: () -> P?) =
    block()?.let { log(it.first, it.second, time, turn); it.first }

internal inline fun <T, P : Pair<T, *>> logp(time: Boolean = true, turn: Boolean = time, noinline draw: Supplier?, block: () -> P?) =
    block()?.let { log(it.first?.let { draw?.invoke(it) ?: draw(it, it::class) }, it.second, time, turn); it.first }

internal inline fun <T> log(time: Boolean = true, turn: Boolean = time, noinline draw: Supplier?, block: () -> T) =
    block().also { log(it?.let { draw?.invoke(it) } ?: draw(it, it?.let { it::class }), time, turn) }

internal inline fun <T> log(time: Boolean = true, turn: Boolean = time, block: () -> T) =
    block().also { log(it, time, turn) }

internal fun <T> log(time: Boolean = true, turn: Boolean = time, name: String? = EmptyString, value: T): T {
    log(if (name.isNullOrEmpty()) "$value" else variable(name, value), time, turn)
    return value
}

internal fun <T> log(time: Boolean = true, turn: Boolean = time, draw: Supplier?, value: T): T {
    log(draw?.invoke(value as Any) ?: draw(value, value!!::class), time, turn)
    return value
}

internal fun log(
    msg: Any?,
    tag: Any? = EmptyString,
    time: Boolean = true,
    turn: Boolean = time,
    indent: UByte = 0u,
    mark: Char = DefaultRulerMark,
    offset: String = EmptyString,
    repeat: Int = DefaultTabSize,
    filter: () -> Boolean = { msg !== null || tag !== null }
) { if (filter()) logger(msg, tag, time, turn, indent, mark, offset, repeat) }

internal fun parse(
    msg: Any?,
    title: Any? = EmptyString,
    time: Boolean = true,
    turn: Boolean = time,
    indent: UByte = 0u,
    mark: Char = DefaultRulerMark,
    offset: String = EmptyString,
    repeat: Int = DefaultTabSize,
    timer: (() -> Any?)? = null,
    turner: (() -> Any?)? = null,
    vararg params: Any?
) = title?.let { pin("$it", where(time, "${timer?.invoke()}"), where(turn, "${turner?.invoke()}")) } + message(draw(msg, params), "$title", indent, mark, offset, repeat)

internal fun pin(title: String, time: String, turn: String) =
    when {
        title_is_very_long(title) -> space(DefaultVeryLongPinMark) + title + NewLine + tick(time, turn) + NewLine
        title_is_long(title) -> wrap(title) + where(DefaultLongPinMark.isNotEmpty() || title.isNotEmpty()) + tick(time, turn) + NewLine
        else -> (time.isNotEmpty() || turn.isNotEmpty()).let { space(DefaultShortPinMark) + space(title) + tick(time, turn, it) + where(it) }
    }

internal fun message(msg: String, title: String = EmptyString, indent: UByte = 0u, mark: Char = DefaultRulerMark, offset: String = EmptyString, repeat: Int = DefaultTabSize) =
    when {
        title_is_long(title) -> if (indent > 0u) msg.replace(NewLine, NewLine + space(indent)) else msg
        indent_is_implicit(indent) -> space(indent) + msg
        else -> tab(indent, mark, offset, repeat) + msg
    }

internal fun tick(time: String, turn: String, separate: Boolean = turn.isNotEmpty()) =
    put(TimeTag, time) + where(separate) + put(TurnTag, turn)

internal fun box(title: String, text: String?, wrap: Array<Char> = DefaultWrapMark, separator: Char = DefaultSeparatorMark, pad: String = EmptyString, indent: Boolean = false) =
    where(indent).let { wrap[0] + it + title + pad + separator + unless(separator.isWhitespace()) + text + it + wrap[1] }

internal fun put(title: String, text: String?, showNull: Boolean = true, showEmpty: Boolean = true) =
    where(showNull || text !== null && (showEmpty && text.isEmpty() || text.isNotEmpty()), box(title, text))

internal fun wrap(title: String, mark: String = DefaultLongPinMark, pad: String = OneSpace) =
    mark.isNotEmpty().let { mark + where(it, pad) + title + where(it, pad) + mark }

internal fun wrap(title: String, mark: Array<Char>, pad: String = OneSpace) =
    mark.isNotEmpty().let { mark[0] + where(it, pad) + title + where(it, pad) + mark[1] }

internal fun variable(name: String, value: Any?, pad: UByte = 1u, sign: String = EqualSign, leftAlign: Boolean = sign === ColonSign || sign === QuestionSign || sign === OneSpace) =
    name + where(leftAlign, EmptyString, space(pad)) + sign + space(pad) + value

internal fun <T> where(happens: Boolean, mark: T, skip: String = EmptyString) =
    if (happens) "$mark" else skip

internal fun where(happens: Boolean) =
    where(happens, OneSpace)

internal fun unless(happens: Boolean) =
    where(happens, EmptyString, OneSpace)

internal fun space(indent: UByte) =
    concat(EmptyString, OneSpace, indent.toInt())

internal fun space(word: String) =
    word + unless(word.isEmpty())

internal fun tab(indent: UByte, mark: Char = DefaultRulerMark, offset: String = EmptyString, repeat: Int = DefaultTabSize): String {
    var tab = EmptyString
    val mark = "$mark"
    for (i in 1..indent.toInt())
        tab += where(i % repeat == 0, mark, OneSpace)
    return offset + tab
}

internal fun concat(init: String, str: String, n: Int): String {
    var out = init
    for (i in 1..n)
        out += str
    return out
}

internal const val TimeTag = "Time"
internal const val TurnTag = "Turn"
internal const val DefaultTabSize = 4
internal const val LongTitleThreshold = 16
internal const val VeryLongTitleThreshold = 64
internal const val ImplicitIndent: UByte = 8u
internal const val DefaultShortPinMark = "--"
internal const val DefaultLongPinMark = "----------------"
internal const val DefaultVeryLongPinMark = "-->>"
internal val DefaultWrapMark = arrayOf('[' , ']')
internal const val DefaultSeparatorMark = ':'
internal const val DefaultRulerMark = '|'
internal const val EmptyString = ""
internal const val OneSpace = " "
internal const val NewLine = "\n"
internal const val ColonSign = ":"
internal const val EqualSign = "="
internal const val QuestionSign = "?"

internal fun title_is_long(title: String) =
    title.length >= LongTitleThreshold
internal fun title_is_very_long(title: String) =
    title.length >= VeryLongTitleThreshold
internal fun indent_is_implicit(indent: UByte) =
    indent < ImplicitIndent

internal enum class Logger : LogFunc {
    On {
        override fun invoke(p1: Any?, p2: Any?, p3: Boolean, p4: Boolean, p5: UByte, p6: Char, p7: String, p8: Int) =
            println(parse(p1, p2, p3, p4, p5, p6, p7, p8))
    },
    Off {
        override fun invoke(p1: Any?, p2: Any?, p3: Boolean, p4: Boolean, p5: UByte, p6: Char, p7: String, p8: Int) {}
    };
}

internal typealias Supplier = Any.() -> Any?
internal typealias LogFunc = (Any?, Any?, Boolean, Boolean, UByte, Char, String, Int) -> Unit
