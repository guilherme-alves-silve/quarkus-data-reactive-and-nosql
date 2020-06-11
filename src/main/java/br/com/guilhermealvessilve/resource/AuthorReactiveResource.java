package br.com.guilhermealvessilve.resource;

import br.com.guilhermealvessilve.data.Author;
import br.com.guilhermealvessilve.repository.AuthorReactiveRepository;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/author/async")
@Produces(MediaType.APPLICATION_JSON)
public class AuthorReactiveResource {

    private final AuthorReactiveRepository repository;

    @Inject
    public AuthorReactiveResource(final AuthorReactiveRepository repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Author>> getAllAuthors() {
        return repository.getAll();
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Author> getAuthor(@PathParam("name") String name) {
        return repository.get(name);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Author> postBook(final Author author) {
        return repository.save(author)
                .onItem()
                .ignore()
                .andSwitchTo(() -> getAuthor(author.getName()));
    }
}