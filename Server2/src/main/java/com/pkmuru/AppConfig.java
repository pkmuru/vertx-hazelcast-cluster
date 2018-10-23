package com.pkmuru;

import io.vertx.core.Vertx;

public class AppConfig {


    public static Vertx vertx;
    public static String MY_DL_EXPANSION_MAP = "DL-EXPANSION-RESULTS";

    public static String INSTANCE_NAME="MY-CLUSTER_NODE_2";

    public static int REST_PORT=8001;
    public static int HAZELCAST_PORT=8000;
    public static int CLUSTER_PORT=8002;

}
