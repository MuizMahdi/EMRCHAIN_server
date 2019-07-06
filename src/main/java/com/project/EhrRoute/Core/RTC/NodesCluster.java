package com.project.EhrRoute.Core.RTC;
import com.project.EhrRoute.Models.Observer;
import com.project.EhrRoute.Models.Subject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;


/**
 * Manages a group of nodes in a network.
 * A cluster subscribes to the clusters container, and nodes of a network subscribe to a nodes cluster.
 */
public class NodesCluster implements Subject, Observer
{
    private HashMap<String, Observer> providingNodes; // Nodes of the cluster that send data, mapped by their UUID "<NodeUUID, Node>"
    private HashMap<String, Observer> consumingNodes; // Nodes that receive data
    private String networkUUID;

    public NodesCluster(String networkUUID) {
        this.networkUUID = networkUUID;
        this.providingNodes = new HashMap<>();
        this.consumingNodes = new HashMap<>();
    }


    @Override
    public void registerObserver(Observer observer) {
        registerProvider(observer);
        registerConsumer(observer);
    }

    @Override
    public void removeObserver(Observer node) {
        removeProvider(node);
        removeConsumer(node);
    }

    public void removeObserver(String nodeUUID) {
        removeProvider(nodeUUID);
        removeConsumer(nodeUUID);
    }

    /**
     * Broadcasts keep-alive data to all available providers and consumers in the cluster
     */
    @Override
    public void notifyObservers(Object notification) {
        System.out.println("---- { SENDING GLOBAL HEART BEAT [3] } ----");
        System.out.println("PROVIDERS: " + providingNodes.size() + "     CONSUMERS: " + consumingNodes.size());
        // Send keep-alive data to all providers
        keepAliveNodes(notification, providingNodes);
        // Send keep-alive data to all consumers
        keepAliveNodes(notification, consumingNodes);
    }

    @Override
    public void update() { }

    @Override
    public String getUUID() {
        return getNetworkUUID();
    }


    private void keepAliveNodes(Object notification, HashMap<String, Observer> nodesList) {
        if (!nodesList.isEmpty()) {

            // Cluster iterator
            Iterator nodesIterator = nodesList.entrySet().iterator();

            // Iterate through the cluster
            while (nodesIterator.hasNext()) {
                // Get each member of cluster (HashMap entry of the cluster)
                Map.Entry clusterEntry = (Map.Entry) nodesIterator.next();

                // Get each member node and node UUID
                Node node = (Node) clusterEntry.getValue();
                String nodeUUID = (String) clusterEntry.getKey();

                SseEmitter.SseEventBuilder message = SseEmitter.event().data(notification).name(NodeMessageType.HEART_BEAT.toString());

                try {
                    System.out.println("SENDING HEART BEAT TO NODE: " + nodeUUID);
                    // Send keep-alive data
                    node.getEmitter().send(message);
                }
                // In case an error occurs during event transmission
                catch (Exception Ex) {
                    System.out.println("REMOVING A MOTHERFUCKER FROM THE CLUSTERS LIST FFS");
                    // Remove provider map using iterator entry to avoid ConcurrentModificationException
                    nodesIterator.remove();
                    // Remove the provider from cluster
                    nodesList.remove(nodeUUID);
                    // Close connection
                    node.getEmitter().complete();
                }
            }
        }
    }

    public void registerProvider(Observer node) {
        providingNodes.put(node.getUUID(), node);
    }

    public void removeProvider(Observer node) {
        providingNodes.remove(node.getUUID());
    }

    public void removeProvider(String nodeUUID) {
        providingNodes.remove(nodeUUID);
    }

    public void registerConsumer(Observer node) {
        consumingNodes.put(node.getUUID(), node);
    }

    public void removeConsumer(Observer node) {
        consumingNodes.remove(node.getUUID());
    }

    public void removeConsumer(String nodeUUID) {
        consumingNodes.remove(nodeUUID);
    }

    /**
     * Finds a consuming node in the nodes cluster by the node's UUID
     * @param nodeUUID      UUID of the consuming node
     * @return              Optional of the node
     */
    public Optional<Observer> findConsumer(String nodeUUID) {
        Observer consumer = consumingNodes.get(nodeUUID);

        if (consumer == null) {
            return Optional.empty();
        }

        return Optional.of(consumer);
    }

    /**
     * Finds a Providing node in the nodes cluster by the node's UUID
     * @param nodeUUID      UUID of the consuming node
     * @return              Optional of the node
     */
    public Optional<Observer> findProvider(String nodeUUID) {
        Observer provider = providingNodes.get(nodeUUID);

        if (provider == null) {
            return Optional.empty();
        }

        return Optional.of(provider);
    }

    /**
     * Finds a random node from the providers of the cluster
     * @return      The node (observer) or an empty instance
     */
    public Optional<Observer> getRandomProvider() {
        Optional<Observer> randomNode;

        try {
            randomNode = providingNodes.values().parallelStream().findAny();
        }
        catch (NullPointerException Ex) {
            return Optional.empty();
        }

        return randomNode;
    }


    public String getNetworkUUID() {
        return networkUUID;
    }
    public HashMap<String, Observer> getProvidingNodes() {
        return providingNodes;
    }
    public HashMap<String, Observer> getConsumingNodes() {
        return consumingNodes;
    }

    public void setNetworkUUID(String networkUUID) {
        this.networkUUID = networkUUID;
    }
    public void setProvidingNodes(HashMap<String, Observer> providingNodes) {
        this.providingNodes = providingNodes;
    }
    public void setConsumingNodes(HashMap<String, Observer> consumingNodes) {
        this.consumingNodes = consumingNodes;
    }
}
