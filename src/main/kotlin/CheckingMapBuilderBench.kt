package bench

import kotlinx.benchmark.*
import org.openjdk.jmh.annotations.Fork

@Fork(1)
@State(Scope.Benchmark)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
public open class CheckingMapBuilderBench {
    @Param("10", "100", "1000", "10000")
    var size: Int = 0

    private var mapBuilder = CheckingMapBuilder<String, String>()

    @Setup
    fun setup() {
        mapBuilder = CheckingMapBuilder()

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