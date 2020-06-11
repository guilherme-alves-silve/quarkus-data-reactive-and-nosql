package br.com.guilhermealvessilve.resource;

import br.com.guilhermealvessilve.data.Book;
import br.com.guilhermealvessilve.repository.BookRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@Path("/book")
public class BookResource {

    private static final Logger LOGGER = Logger.getLogger(BookResource.class);

    private final BookRepository repository;

    @Inject
    public BookResource(final BookRepository repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Book> getAllBooks() {
        return repository.getAll();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getBook(@PathParam("id") Integer id) {
        return repository.get(id)
                .onItem()
                .apply(book -> Objects.isNull(book)
                        ? Response.status(Response.Status.NOT_FOUND).build()
                        : Response.ok(book).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> postBook(final Book book) {
        return repository.save(book)
                        .onItem()
                        .apply(id -> {
                            try {
                                return Objects.isNull(id)
                                        ? Response.serverError().build()
                                        : Response.created(new URI("/book/" + id)).build();
                            } catch (URISyntaxException ex) {
                                LOGGER.error(ex);
                                return Response.serverError()
                                        .build();
                            }
                        });
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> deleteBook(@PathParam("id") Integer id) {
        return repository.delete(id)
                .onItem()
                .apply(deleted -> deleted? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem()
                .apply(status -> Response.status(status).build());
    }

    @POST
    @Path("/multi")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> postBooks(final List<Book> books) {
        return repository.save(books)
                .onItem()
                .apply(nothing -> Response.noContent().build());
    }
}