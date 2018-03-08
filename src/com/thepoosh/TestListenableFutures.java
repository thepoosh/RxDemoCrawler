package com.thepoosh;


import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class TestListenableFutures {
	final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
	final Random random = new Random();
	final Set<Route> trickled = new HashSet<>();
	final ReentrantLock lock = new ReentrantLock();


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

	static Optional<Route> combineOptionals(Optional<Route> route1, Optional<Route> route2) {
		if (route1.isPresent() && route2.isPresent()) {
			return Optional.of(route1.get().combine(route2.get()));
		} else if (route1.isPresent()) {
			return route1;
		} else {
			return route2;
		}
	}


	void run_example() {
		System.out.println("aaaaaaaaaaaaa");
		ImmutableList<ListenableFuture<Optional<Route>>> mainLegs =
				ImmutableList.of(ImmutableList.of("JFK", "LHR", "A"), ImmutableList.of("JFK", "LHR", "B"))
						.stream()
						.map(e -> service.submit(() -> getOptionalRoute(e.get(0), e.get(1), e.get(2))))
						.collect(ImmutableList.toImmutableList());
		ImmutableList<ListenableFuture<Optional<Route>>> pickups = ImmutableList.of(ImmutableList.of("NY", "JFK", "C"), ImmutableList.of("NY", "JFK", "D"))
				.stream()
				.map(e -> service.submit(() -> getOptionalRoute(e.get(0), e.get(1), e.get(2))))
				.collect(ImmutableList.toImmutableList());
		ImmutableList<ListenableFuture<Optional<Route>>> deliveries = ImmutableList.of(ImmutableList.of("LHR", "London", "X"), ImmutableList.of("LHR", "London", "Y"))
				.stream()
				.map(e -> service.submit(() -> getOptionalRoute(e.get(0), e.get(1), e.get(2))))
				.collect(ImmutableList.toImmutableList());



		for (ListenableFuture<Optional<Route>> mainLeg : mainLegs) {

			for (ListenableFuture<Optional<Route>> pickup : pickups) {

				for (ListenableFuture<Optional<Route>> delivery : deliveries) {
					Futures.addCallback(Futures.allAsList(pickup, mainLeg, delivery), new FutureCallback<List<Optional<Route>>>() {
						@Override
						public void onSuccess(List<Optional<Route>> result) {
							if (result.get(1).isPresent()) {
								Optional<Route> combined = result.stream().reduce(Optional.empty(), (a, b) -> TestListenableFutures.combineOptionals(a, b));
								lock.lock();
								if (trickled.add(combined.get())) {
									System.out.println(combined.get());
								}
								lock.unlock();

							}


						}

						@Override
						public void onFailure(Throwable t) {

						}
					}, service);

				}
			}
		}
	}

	public static void main(String[] args) {
		TestListenableFutures futures = new TestListenableFutures();
		futures.run_example();


	}

}
