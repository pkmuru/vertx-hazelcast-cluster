package com.pkmuru;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

public class MyRestVerticle extends AbstractVerticle {


    @Override
    public void start(Future<Void> startFuture) throws Exception {

        Vertx vertx = getVertx();
        Router router = Router.router(vertx);

        router.route("/get1").handler(this::handleGet1);

        router.route("/write1").handler(this::handleWrite1);


        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(AppConfig.REST_PORT);

        startFuture.complete();
    }

    public void handleGet1(RoutingContext routingContext) {

        String key=routingContext.request().getParam("key");

        String identity = "- get From -" + AppConfig.INSTANCE_NAME;


        readFromCache(key).thenAccept(o -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/plain")
                    .end(o.toString() + identity);


        });

    }


    private void handleWrite1(RoutingContext routingContext) {


        String key=routingContext.request().getParam("key");
        String value=routingContext.request().getParam("value") + "-Set-by-" + AppConfig.INSTANCE_NAME;


        writeCache(key, value).thenAccept(o -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/plain")
                    .end("Hellow write1");
        });


    }


    public CompletableFuture<Object> writeCache(final String key, final String value) {
        CompletableFuture<Object> future = new CompletableFuture();

        getVertx().sharedData().getAsyncMap(AppConfig.MY_DL_EXPANSION_MAP, asyncMapAsyncResult -> {

            asyncMapAsyncResult.result().put(key, value, voidAsyncResult -> {

                if (voidAsyncResult.succeeded()) {
                    System.out.println("Written completed successfullt....");
                    future.complete(null);
                } else {
                    System.out.println(voidAsyncResult.cause().toString());
                }

            });
        });
        return future;

    }

    private CompletableFuture<Object> readFromCache(final String cacheKey) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        try {
            this.getVertx().sharedData().getAsyncMap(AppConfig.MY_DL_EXPANSION_MAP, asyncMapAsyncResult -> {
                if (asyncMapAsyncResult.succeeded()) {
                    asyncMapAsyncResult.result().get(cacheKey, objectAsyncResult -> {
                        future.complete(objectAsyncResult.result());
                    });
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("Error reading cache for key " + cacheKey);
        }

        return future;
    }


}
