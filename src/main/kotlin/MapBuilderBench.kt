package bench

import kotlinx.benchmark.*
import org.openjdk.jmh.annotations.Fork

@Fork(1)
@State(Scope.Benchmark)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
public open class MapBuilderBench {
    @Param("10", "100", "1000", "10000")
    var size: Int = 0

    private var mapBuilder = MapBuilder<String, String>()

    @Setup
    fun setup() {
        mapBuilder = MapBuilder()

        for (i in 0..<size) {
            mapBuilder[i.toString()] = (i shl 1).toString()
        }
    }

    @Benchmark
    fun iterateEntry(): Int {
        var sum = 0
        for (entry in mapBuilder) {
            sum += entry.key.length + entry.value.length
        }
        return sum
    }

    // For running with `-wm BULK`
    // It fails at some point in iteration. Specifically, when the `sum` gets >= `size`
    // The exception is thrown upon iterator.next() invocation
    @Benchmark
    fun iterateEntryFail(): Int {
        var sum = 0
        for (entry in mapBuilder) {
            val sumString = sum.toString()
            mapBuilder[sumString] = sumString
            sum += entry.key.length + entry.value.length
        }
        return sum
    }

    @Benchmark
    fun iterateKeyValue(): Int {
        var sum = 0
        for ((key, value) in mapBuilder) {
            sum += key.length + value.length
        }
        return sum
    }

    @Benchmark
    fun iterateAndSetValue(): Map<String, String> {
        for (entry in mapBuilder) {
            entry.setValue("null")
        }
        return mapBuilder
    }

    @Benchmark
    fun storeEntries(): Map<String, String> {
        var lastEntry = mapBuilder.entries.first()
        for (entry in mapBuilder) {
            entry.setValue(lastEntry.value)
            lastEntry = entry
        }
        return mapBuilder
    }
}