package br.com.guilhermealvessilve.repository;

import br.com.guilhermealvessilve.data.Library;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class LibraryRepository {

    public Optional<Library> findByIdOptional(String id) {
        return Optional.ofNullable(Library.findById(new ObjectId(id)));
    }
}
