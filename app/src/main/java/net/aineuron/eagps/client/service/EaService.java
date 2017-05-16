package net.aineuron.eagps.client.service;

import net.aineuron.eagps.model.database.Car;

import java.util.List;

import io.reactivex.Maybe;
import retrofit2.http.GET;

/**
 * Created by Vit Veres on 31.3.2016
 * as a part of AlTraceabilitySystem project.
 */
public interface EaService {
	@GET("categories")
	Maybe<List<Car>> getCars();
}
