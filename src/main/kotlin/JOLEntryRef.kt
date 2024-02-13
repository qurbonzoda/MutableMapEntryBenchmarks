package bench

import org.openjdk.jol.info.ClassLayout
import org.openjdk.jol.vm.VM

fun main() {
    println(VM.current().details())
    println(ClassLayout.parseClass(MapBuilder.EntryRef::class.java).toPrintable())
    println(ClassLayout.parseClass(CheckingMapBuilder.EntryRef::class.java).toPrintable())
}
