package br.com.guilhermealvessilve.resource;

import br.com.guilhermealvessilve.data.Author;
import br.com.guilhermealvessilve.repository.AuthorRepository;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/author")
public class AuthorResource {

    private final AuthorRepository repository;

    @Inject
    public AuthorResource(final AuthorRepository repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Author> getAllAuthors() {
        return repository.getAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postBook(final Author author) {
        repository.save(author);
        return Response.ok().build();
    }
}