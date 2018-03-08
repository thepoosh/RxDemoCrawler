package com.thepoosh

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import java.util.*
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    //ListenableFutureGraph.main(args)

    var shouldRun = true
    val graph: Graph<String, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)
    val paths = AllDirectedPaths(graph)

    getData()
            .subscribeBy(
                    onNext = {
                        it.ifPresent {
                            println(it)
                            graph.addVertex(it.origin)
                            graph.addVertex(it.destination)
                            graph.addEdge(it.origin, it.destination)
                            if (graph.containsVertex("NY") && graph.containsVertex("London")) {
                                println(paths.getAllPaths("NY", "London", true, null))
                            }
                        }
                    },
                    onError = {
                        it.printStackTrace()
                    },
                    onComplete = {
                        shouldRun = false
                        println("YAY, we're done!!!")

                    }
            )

    while (shouldRun) {
        Thread.sleep(100)
    }

}

data class Route(val origin: String, val destination: String, val vendor: String) {

    fun combine(other: Route): Route {
        return Route(origin, other.destination, "${vendor},${other.vendor}")
    }
}

fun getData(): Observable<Optional<Route>> {
    val list = Lists.newArrayList(ImmutableList.of("JFK", "LHR", "A"),
            ImmutableList.of("JFK", "LHR", "B"),
            ImmutableList.of("NY", "JFK", "C"),
            ImmutableList.of("NY", "JFK", "D"),
            ImmutableList.of("LHR", "London", "X"),
            ImmutableList.of("LHR", "London", "Y"))

    return Observable.zip(
            Observable.interval(1000, TimeUnit.MILLISECONDS).take(6),
            Observable.fromIterable(list),
            BiFunction<Long, ImmutableList<String>, ImmutableList<String>>({ i, lst -> lst }))
            .map {
                when {
                    Random().nextBoolean() -> Optional.of(Route(it[0], it[1], it[2]))
                    else -> Optional.empty<Route>()
                }
            }


}






