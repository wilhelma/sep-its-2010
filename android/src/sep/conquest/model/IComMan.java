package sep.conquest.model;

import java.util.UUID;

/**
 * The IComMan-interface enables a communication-manager to provide broadcast
 * messaging.
 * 
 * @author Andreas Wilhelm
 */
public interface IComMan {
    
    /**
     * Initiate a broadcast message to all registered participants at the
     * communication-manager.
     * 
     * @param request The request-message.
     */
    void broadcast(IRequest request);
    
    /**
     * addClient registers a client for participating the broadcast-
     * communication by the communication-manager.
     * 
     * @param id The ID of the client.
     * @param client The client which has to be added.
     */    
    void addClient(UUID id, IComClient client); //TODO UUID isn't necessary (client.getID())

    /**
     * removeClient removes a participant from the communication-manager.
     * 
     * @param id The ID of the client which has to be removed.
     */
    void removeClient(UUID id);
    
    /**
     * Returns the list of registered clients.
     * 
     * @return The clients.
     */
    IComClient[] getClients();
    
    /**
     * Get a specific client, if available. Otherwise it returns null.
     * 
     * @param id The id of the client.
     * @return The client.
     */
    IComClient getClient(UUID id);
}
