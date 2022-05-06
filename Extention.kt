// © Copyright 2021 Alireza Kamran

package katie.module.internal

internal var logger: LogFunc = Logger.On

internal const val millisInSecond = 1000
internal const val millisInMinute = 60000
internal const val millisInHour = 3600000
internal fun timeFormat(time: String) = when {
    time.length < 4 -> time
    time.length < 5 || time[time.length - 5] <= '5' -> time.substring(0, time.length - 3) + DotSign + time.substring(time.length - 3)
    time[time.length - 5] > '5' && time.length < 7 || time.isDigitsOnly() -> {
        var t = time.toLong()
        val h = if (t >= millisInHour) (t / millisInHour).also { t -= it * millisInHour } else 0
        val m = t / millisInMinute
        t -= m * millisInMinute
        val s = t / millisInSecond
        t -= s * millisInSecond
        where(h != 0L, "$h$ColonSign") + "$m$ColonSign" + where(s < 10, "0") + "$s$DotSign$t" }
    else -> time }

internal fun <T> draw(obj: T?, vararg params: Any?) =
    if (obj is Exception) obj.message + NewLine + obj.stackTrace else obj.toString()

internal inline fun <T> log(
    time: Boolean = true,
    noinline tFormat: (String) -> String = ::returnIt,
    turn: Boolean = time,
    block: () -> T) =
    block().also { log(it, time = time, tFormat = tFormat, turn = turn) }
internal inline fun <T> log(
    time: Boolean = true,
    noinline tFormat: (String) -> String = ::returnIt,
    turn: Boolean = time,
    noinline draw: Supplier?,
    block: () -> T) =
    block().also { log(it?.let { draw?.invoke(it) } ?: draw(it, it?.let { it::class }), time = time, tFormat = tFormat, turn = turn) }
internal inline fun <T, P : Pair<T, *>> logp(
    time: Boolean = true,
    noinline tFormat: (String) -> String = ::returnIt,
    turn: Boolean = time,
    block: () -> P?) =
    block()?.let { log(it.first, it.second, time, tFormat, turn); it.first }
internal inline fun <T, P : Pair<T, *>> logp(
    time: Boolean = true,
    noinline tFormat: (String) -> String = ::returnIt,
    turn: Boolean = time,
    noinline draw: Supplier?,
    block: () -> P?) =
    block()?.let { log(it.first?.let { draw?.invoke(it) ?: draw(it, it::class) }, it.second, time, tFormat, turn); it.first }
internal fun <T> log(
    time: Boolean = true,
    tFormat: (String) -> String = ::returnIt,
    turn: Boolean = time,
    name: String? = EmptyString,
    value: T): T {
    log(if (name.isNullOrEmpty()) value.toString() else variable(name, value), time = time, tFormat = tFormat, turn = turn)
    return value }
internal fun <T> log(
    time: Boolean = true,
    tFormat: (String) -> String = ::returnIt,
    turn: Boolean = time,
    draw: Supplier?, value: T): T {
    log(draw?.invoke(value as Any) ?: draw(value, value!!::class), time = time, tFormat = tFormat, turn = turn)
    return value }

internal fun log(
    msg: Any?,
    tag: Any? = EmptyString,
    time: Boolean = true,
    tFormat: (String) -> String = ::timeFormat,
    turn: Boolean = time,
    indent: UByte = 0u,
    mark: Char = DefaultRulerMark,
    offset: String = EmptyString,
    repeat: Int = DefaultTabSize,
    filter: () -> Boolean = { msg !== null || tag !== null }) {
    if (filter()) logger(msg, tag, time, tFormat, turn, indent, mark, offset, repeat) }

internal fun parse(
    msg: Any?,
    title: Any? = EmptyString,
    time: Boolean = true,
    tFormat: (String) -> String = ::timeFormat,
    turn: Boolean = time,
    indent: UByte = 0u,
    mark: Char = DefaultRulerMark,
    offset: String = EmptyString,
    repeat: Int = DefaultTabSize,
    timer: (() -> Any?)? = null,
    turner: (() -> Any?)? = null,
    vararg params: Any?) =
    title?.let { pin(it.toString(), where(time, timer?.invoke().toString()), where(turn, turner?.invoke().toString()), tFormat) } + message(draw(msg, params), title.toString(), indent, mark, offset, repeat)

internal fun pin(title: String, time: String, turn: String, tFormat: (String) -> String = ::timeFormat) = when {
    title_is_very_long(title) -> space(DefaultVeryLongPinMark) + title + NewLine + tick(time, turn, tFormat) + NewLine
    title_is_long(title) -> wrap(title) + where(DefaultLongPinMark.isNotEmpty() || title.isNotEmpty()) + tick(time, turn, tFormat) + NewLine
    else -> (time.isNotEmpty() || turn.isNotEmpty()).let { space(threadPinMark) + space(title) + tick(time, turn, tFormat, it) } }

internal fun message(msg: String, title: String = EmptyString, indent: UByte = 0u, mark: Char = DefaultRulerMark, offset: String = EmptyString, repeat: Int = DefaultTabSize) = when {
    title_is_long(title) -> if (indent > 0u) msg.replace(NewLine, NewLine + space(indent)) else msg
    indent_is_implicit(indent) -> insert(space(indent) + msg)
    else -> tab(indent, mark, offset, repeat) + msg }

internal fun tick(time: String, turn: String, tFormat: (String) -> String = ::timeFormat, separate: Boolean = turn.isNotEmpty()) =
    put(TimeTag, tFormat(time)) + where(separate) + where(turn.isNotEmpty(), put(TurnTag, turn))

internal fun box(title: String, text: String?, wrap: Array<Char> = DefaultWrapMark, separator: Char = DefaultSeparatorMark, pad: String = EmptyString, indent: Boolean = false) =
    where(indent).let { wrap[0] + it + title + pad + separator + unless(separator.isWhitespace()) + text + it + wrap[1] }

internal fun put(title: String, text: String?, showNull: Boolean = true, showEmpty: Boolean = true, showBlank: Boolean = true) =
    where(showNull || text !== null && ((showEmpty && text.isEmpty() || text.isNotEmpty()) || (showBlank && text.isBlank() || text.isNotBlank()) || text.isNotEmpty()), box(title, text))

internal fun put(title: String, obj: Any?, showNull: Boolean = true) =
    where(showNull || obj !== null, box(title, draw(obj)))

internal fun wrap(title: String, mark: String = DefaultLongPinMark, pad: String = OneSpace) =
    where(mark.isNotEmpty(), pad).let { mark + it + title + it + mark }

internal fun wrap(title: String, mark: Array<Char>, pad: String = OneSpace) =
    where(mark.isNotEmpty(), pad).let { mark[0] + it + title + it + mark[1] }

internal fun variable(name: String, value: Any?, pad: UByte = 1u, sign: String = EqualSign, leftAlign: Boolean = sign === ColonSign || sign === QuestionSign || sign === OneSpace) =
    name + where(leftAlign, EmptyString, space(pad)) + sign + space(pad) + value

internal fun <T> where(happens: Boolean, mark: T, skip: String = EmptyString) =
    if (happens) mark.toString() else skip

internal fun where(happens: Boolean) =
    where(happens, OneSpace)

internal fun unless(happens: Boolean) =
    where(happens, EmptyString, OneSpace)

internal fun space(word: String) =
    word + where(word.isNotEmpty())

internal fun space(word: String, happens: Boolean = word.isNotEmpty()) =
    word + where(happens)

internal fun space(word: String, indent: UByte = 1u) =
    word + where(word.isNotEmpty(), space(indent))

internal fun insert(word: String) =
    where(word.isNotEmpty()) + word

internal fun insert(word: String, happens: Boolean = word.isNotEmpty()) =
    where(happens) + word

internal fun insert(word: String, indent: UByte = 1u) =
    where(word.isNotEmpty(), space(indent)) + word

internal fun space(indent: UByte) =
    concat(EmptyString, OneSpace, indent.toInt())

internal fun tab(indent: UByte, mark: Char = DefaultRulerMark, offset: String = EmptyString, repeat: Int = DefaultTabSize): String {
    var tab = EmptyString
    val mark = mark.toString()
    for (i in 1..indent.toInt())
        tab += where(i % repeat == 0, mark, OneSpace)
    return offset + tab }

internal fun concat(init: String, str: String, n: Int): String {
    var out = init
    for (i in 1..n)
        out += str
    return out }

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
internal const val DotSign = "."
internal const val EqualSign = "="
internal const val QuestionSign = "?"

internal const val AsyncMsgPinMark1 = DefaultShortPinMark
internal const val AsyncMsgPinMark2 = "||"
internal val threadPinMark: String
    get() {
        var mark = AsyncMsgPinMark1
        lastSeenThread = Thread.currentThread().also { if (it != lastSeenThread) mark = AsyncMsgPinMark2 }
        return mark }
private var lastSeenThread = Thread.currentThread()

internal fun title_is_long(title: String) =
    title.length >= LongTitleThreshold
internal fun title_is_very_long(title: String) =
    title.length >= VeryLongTitleThreshold
internal fun indent_is_implicit(indent: UByte) =
    indent < ImplicitIndent

internal fun <T> returnIt(it: T) = it
private fun String.isDigitsOnly() = all { it.isDigit() }

internal var logFunc: (Any?) -> Unit = ::println
internal enum class Logger : LogFunc {
    On { override fun invoke(p1: Any?, p2: Any?, p3: Boolean, p4: (String) -> String, p5: Boolean, p6: UByte, p7: Char, p8: String, p9: Int) =
        logFunc(parse(p1, p2, p3, p4, p5, p6, p7, p8, p9)) },
    Off { override fun invoke(p1: Any?, p2: Any?, p3: Boolean, p4: (String) -> String, p5: Boolean, p6: UByte, p7: Char, p8: String, p9: Int) {} }; }

internal typealias Supplier = Any.() -> Any?
internal typealias LogFunc = (Any?, Any?, Boolean, (String) -> String, Boolean, UByte, Char, String, Int) -> Unit
