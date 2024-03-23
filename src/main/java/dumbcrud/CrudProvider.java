package fr.anthonyquere.dumbcrud;

import java.util.List;

/**
 * Describes a providers that handles stocking the Domain object
 * @param <Domain> the object that you want to manage with your CRUD
 */
public interface CrudProvider<Domain> {
    /**
     * Defines how a domain should be stored
     * @param domain instance of the class to store
     * @throws Exception if anything fails
     */
    void add(Domain domain) throws Exception;

    /**
     * List all domains stored
     * @throws Exception if anything fails
     */
    List<Domain> list() throws Exception;
}
