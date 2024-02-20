package bench

import kotlinx.benchmark.*
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.infra.IterationParams
import org.openjdk.jmh.runner.IterationType

@Fork(1)
@State(Scope.Benchmark)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Warmup(iterations = 50, time = 200, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
open class PolymorphicMapsBenchmark {
    @Param("10", "100", "1000", "10000")
    var size: Int = 0

    @Param("false", "true")
    var forceCME: Boolean = false

    @Param("BASELINE", "CHECKING", "CACHING")
    var mapType: String = "BASELINE"

    var maps: Array<Map<String, String>> = allocateMaps()

    var map: Map<String, String> = maps.first()

    private fun fill(m: MutableMap<String, String>) {
        for (i in 0 ..< size) {
            m[i.toString()] = i.toString()
        }
    }

    private fun allocateMaps(): Array<Map<String, String>> {
        val maps: Array<Map<String, String>> = arrayOf(HashMap(), MapBuilder(), CheckingMapBuilder(), CachingMapBuilder())
        maps.forEach {
            fill(it as MutableMap)
        }
        return maps
    }

    private fun runCatching(block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            // that's okay
        }
    }

    private var idx = 0

    @Setup(Level.Iteration)
    fun setup(params: IterationParams) {
        maps = allocateMaps()
        // use different map types during the warmup to ensure that in a benchmark call site will be polymorphic
        if (params.type == IterationType.WARMUP) {
            map = maps[idx]
            idx = (idx + 1) % maps.size
        } else {
            map = when (mapType) {
                "BASELINE" -> maps[1] as MapBuilder<String, String>
                "CHECKING" -> maps[2] as CheckingMapBuilder<String, String>
                "CACHING" -> maps[3] as CachingMapBuilder<String, String>
                else -> throw IllegalStateException("mapType=$mapType")
            }
        }
        if (params.type == IterationType.WARMUP && forceCME) {
            val mapMut = map as MutableMap<String, String>
            val entries = mapMut.entries
            val (k, v) = entries.last()
            val first = entries.first()
            mapMut.remove(k)

            runCatching { first.key }
            runCatching { first.value }
            runCatching { first.setValue("blablabla") }

            mapMut[k] = v
        }
    }

    @Benchmark
    fun iterateEntry(): Int {
        var sum = 0
        for (entry in map) {
            sum += entry.key.length + entry.value.length
        }
        return sum
    }

    @Benchmark
    fun iterateKeyValue(): Int {
        var sum = 0
        for ((key, value) in map) {
            sum += key.length + value.length
        }
        return sum
    }

    @Benchmark
    fun iterateAndSetValue(): Map<String, String> {
        for (entry in map as MutableMap<String, String>) {
            entry.setValue("null")
        }
        return map
    }

    @Benchmark
    fun storeEntries(): Map<String, String> {
        var lastEntry = map.entries.first()
        for (entry in map as MutableMap<String, String>) {
            entry.setValue(lastEntry.value)
            lastEntry = entry
        }
        return map
    }
}