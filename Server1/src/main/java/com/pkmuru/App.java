package com.pkmuru;

import com.hazelcast.config.*;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {


        String CACHE_MAP_NAME = "dl-expansion";

        Config config = new Config()
                .setInstanceName(AppConfig.INSTANCE_NAME)
                .setNetworkConfig(new NetworkConfig()
                        .setPort(AppConfig.HAZELCAST_PORT)
                        .setPortAutoIncrement(false)
                        .setJoin(createJoinConfig()));

        ClusterManager mgr = new HazelcastClusterManager();

        config.getMapConfigs().put(CACHE_MAP_NAME, createMapConfig(CACHE_MAP_NAME));


        ((HazelcastClusterManager) mgr).setConfig(config);


        VertxOptions options = new VertxOptions()
                .setClusterManager(mgr)
                .setClusterPort(AppConfig.CLUSTER_PORT);

        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                AppConfig.vertx = res.result();
                AppConfig.vertx.deployVerticle(MyRestVerticle.class.getName());

            } else {
                // failed!
            }
        });


    }

    private static final List<String> clusterHosts = Arrays.asList("192.168.1.188:8000", "192.168.1.188:7000");

    private static JoinConfig createJoinConfig() throws SocketException {
        TcpIpConfig tcpipConfig = new TcpIpConfig()

                .setEnabled(true)
                .setConnectionTimeoutSeconds(5);
        clusterHosts.forEach(tcpipConfig::addMember);
        return new JoinConfig()
                .setTcpIpConfig(tcpipConfig)
                .setMulticastConfig(
                        new MulticastConfig()
                                .setEnabled(false));
    }

    private static MapConfig createMapConfig(final String cacheMapName) {
        return new MapConfig()
                .setName(cacheMapName)
                .setInMemoryFormat(InMemoryFormat.BINARY)
                .setEvictionPolicy(EvictionPolicy.NONE)
                .setTimeToLiveSeconds(60)
                .setStatisticsEnabled(false);
    }
}
