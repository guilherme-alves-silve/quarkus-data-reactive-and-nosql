package br.com.guilhermealvessilve.resource;

import br.com.guilhermealvessilve.data.Category;
import br.com.guilhermealvessilve.data.Library;
import br.com.guilhermealvessilve.repository.CategoryRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Path("/category")
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private final CategoryRepository repository;

    @Inject
    public CategoryResource(CategoryRepository repository) {
        this.repository = repository;
    }

    @GET
    public CompletionStage<List<Category>> getAllCategories() {
        return repository.getAll();
    }

    @GET
    @Path("{id}")
    public CompletionStage<Response> getLibrary(@PathParam("id") Long id) {
        return repository.findByIdOptional(id)
                .thenApply(optCategory -> optCategory.map(category -> Response.ok(category).build())
                                                     .orElseGet(() -> Response.status(Status.NOT_FOUND).build()))
                .exceptionally(throwable -> Response.serverError().build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CompletionStage<Response> postCategory(final Category category) {
        return repository.save(category)
                .thenApply(savedCategory -> Response.created(URI.create("/category/" + savedCategory.getId())).build())
                .exceptionally(throwable -> Response.serverError().build());
    }

    @DELETE
    @Path("{id}")
    public CompletionStage<Response> deleteLibrary(@PathParam("id") Long id) {
        return repository.delete(id)
                .thenApply(result -> result? Status.NO_CONTENT : Status.NOT_FOUND)
                .thenApply(status -> Response.status(status).build())
                .exceptionally(throwable -> Response.serverError().build());
    }
}