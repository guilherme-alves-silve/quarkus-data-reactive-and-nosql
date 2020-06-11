package br.com.guilhermealvessilve.resource;

import br.com.guilhermealvessilve.data.Person;
import br.com.guilhermealvessilve.repository.PersonRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/person")
public class PersonResource {

    private final PersonRepository repository;

    @Inject
    public PersonResource(final PersonRepository repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getAllPersons() {
        return repository.getAll();
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Person getPerson(@PathParam("name") String id) {
        return repository.get(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postPerson(final Person person) {
        repository.save(person);
        return Response.noContent().build();
    }
}