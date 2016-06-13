package org.grails.datastore.gorm.neo4j.util;

import org.grails.datastore.mapping.reflect.ClassUtils;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.harness.internal.Ports;
import org.neo4j.server.ServerStartupException;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

import static org.neo4j.graphdb.factory.GraphDatabaseSettings.BoltConnector.EncryptionLevel.DISABLED;
import static org.neo4j.graphdb.factory.GraphDatabaseSettings.boltConnector;
import static org.neo4j.dbms.DatabaseManagementSystemSettings.data_directory;

/**
 * Helper class for starting a Neo4j 3.x embedded server
 *
 * @author Graeme Rocher
 * @since 6.0
 */
public class EmbeddedNeo4jServer {

    /**
     * @return Whether the embedded server capability is available or not
     */
    public static boolean isAvailable() {
        return ClassUtils.isPresent("org.neo4j.harness.ServerControls", EmbeddedNeo4jServer.class.getClassLoader());
    }

    /**
     * Start a server on a random free port
     *
     * @return The server controls
     * @param dataLocation The data location
     */
    public static ServerControls start(File dataLocation) throws IOException {
        return attemptStartServer(0, dataLocation);
    }


    /**
     * Start a server on the given address
     *
     * @param inetAddr The inet address
     *
     * @return The {@link ServerControls}
     */
    public static ServerControls start(InetSocketAddress inetAddr) {
        return start(inetAddr.getHostName(),inetAddr.getPort(), null);
    }

    /**
     * Start a server on the given address
     *
     * @param inetAddr The inet address
     *
     * @return The {@link ServerControls}
     */
    public static ServerControls start(InetSocketAddress inetAddr, File dataLocation) {
        return start(inetAddr.getHostName(),inetAddr.getPort(), dataLocation);
    }


    /**
     * Start a server on the given address
     *
     * @param address The address
     *
     * @return The {@link ServerControls}
     */
    public static ServerControls start(String address) {
        URI uri = URI.create(address);
        return start(new InetSocketAddress(uri.getHost(), uri.getPort()));
    }

    /**
     * Start a server on the given address
     *
     * @param address The address
     *
     * @return The {@link ServerControls}
     */
    public static ServerControls start(String address, File dataLocation) {
        URI uri = URI.create(address);
        return start(uri.getHost(), uri.getPort(), dataLocation);
    }

    /**
     * Start a server on the given address
     *
     * @param host The host
     * @param port The port
     *
     * @return The {@link ServerControls}
     */
    public static ServerControls start(String host, int port) {
        return start(host, port,  null);
    }
    /**
     * Start a server on the given address
     *
     * @param host The host
     * @param port The port
     *
     * @return The {@link ServerControls}
     */
    public static ServerControls start(String host, int port, File dataLocation) {
        String myBoltAddress = String.format("%s:%d", host, port);

        TestServerBuilder serverBuilder = TestServerBuilders.newInProcessBuilder()
                .withConfig(boltConnector("0").enabled, "true")
                .withConfig(boltConnector("0").encryption_level, DISABLED.name())
                .withConfig(boltConnector("0").address, myBoltAddress);
        if(dataLocation != null) {
            serverBuilder = serverBuilder.withConfig(data_directory,  dataLocation.getPath());
        }
        ServerControls serverControls = serverBuilder
                                            .newServer();

        return serverControls;
    }

    private static ServerControls attemptStartServer(int retryCount, File dataLocation) throws IOException {

        try {
            InetSocketAddress inetAddr = Ports.findFreePort("localhost", new int[]{7687, 64 * 1024 - 1});

            ServerControls serverControls = start(inetAddr, dataLocation);
            return serverControls;
        } catch (ServerStartupException sse) {
            if(retryCount < 4) {
                return attemptStartServer(++retryCount, dataLocation);
            }
            else {
                throw sse;
            }
        }
    }
}
