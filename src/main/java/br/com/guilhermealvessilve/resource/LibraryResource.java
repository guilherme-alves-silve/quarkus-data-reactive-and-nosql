package br.com.guilhermealvessilve.resource;

import br.com.guilhermealvessilve.data.Library;
import br.com.guilhermealvessilve.repository.LibraryRepository;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@Path("/library")
@Produces(MediaType.APPLICATION_JSON)
public class LibraryResource {

    private final LibraryRepository repository;

    @Inject
    public LibraryResource(LibraryRepository repository) {
        this.repository = repository;
    }

    @GET
    public List<Library> getAllLibraries() {
        return Library.listAll();
    }

    @GET
    @Path("{id}")
    public Response getLibrary(@PathParam("id") String id) {
        return repository.findByIdOptional(id)
                .map(library -> Response.ok(library).build())
                .orElseGet(() -> Response.status(Status.NOT_FOUND).build());
    }

    @PUT
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postOrPutLibrary(final Library library) {
        library.persistOrUpdate();
        return Response.ok(library).build();
    }
}