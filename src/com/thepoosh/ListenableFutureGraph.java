package com.thepoosh;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.*;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;

class ListenableFutureGraph {
	final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
	final Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

	AllDirectedPaths<String, DefaultEdge> paths = new AllDirectedPaths<>(graph);
	private final Random random = new Random();

	Optional<Route> getOptionalRoute(String origin, String destination, String carrier) {
		try {
			Thread.sleep(random.nextInt(1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (random.nextBoolean()) {
			return Optional.of(new Route(origin, destination, carrier));
		} else {
			return Optional.empty();
		}

	}

	void combineResults(Route route) {
		System.out.println(route);
		graph.addVertex(route.getOrigin());
		graph.addVertex(route.getDestination());
		graph.addEdge(route.getOrigin(), route.getDestination());
		if (graph.containsVertex("NY") && graph.containsVertex("London")) {
			System.out.println("bbb" + paths.getAllPaths("NY", "London", true, null));
		}

	}

	void run_program() {
		ImmutableList<ListenableFuture<Optional<Route>>> legs = ImmutableList.of(
				ImmutableList.of("JFK", "LHR", "A"), ImmutableList.of("JFK", "LHR", "B"),
				ImmutableList.of("NY", "JFK", "C"), ImmutableList.of("NY", "JFK", "D"),
				ImmutableList.of("LHR", "London", "X"), ImmutableList.of("LHR", "London", "Y"))
				.stream()
				.map(e -> service.submit(() -> getOptionalRoute(e.get(0), e.get(1), e.get(2))))
				.collect(ImmutableList.toImmutableList());
		legs.forEach(future -> Futures.addCallback(future, new FutureCallback<Optional<Route>>() {
			@Override
			public void onSuccess(Optional<Route> result) {
				if (result.isPresent()) {
					combineResults(result.get());
				}
			}

			@Override
			public void onFailure(Throwable t) {

			}

		}, service));



	}

	public static void main(String[] args) {
		ListenableFutureGraph obj = new ListenableFutureGraph();
		obj.run_program();

	}

}
