package br.com.guilhermealvessilve.repository;

import br.com.guilhermealvessilve.data.Author;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class AuthorRepository {

    private final MongoClient client;

    @Inject
    public AuthorRepository(final MongoClient client) {
        this.client = client;
    }

    public void save(final Author author) {

        final var document = new Document()
                .append("name", author.getName())
                .append("address", author.getAddress());
        getCollection().insertOne(document);
    }

    public List<Author> getAll() {

        try (final var cursor = getCollection().find().iterator()) {

            return Stream.iterate(cursor, MongoCursor::hasNext, UnaryOperator.identity())
                    .map(MongoCursor::next)
                    .map(document -> new Author(
                            document.getString("name"),
                            document.getString("address")
                    ))
                    .collect(toList());
        }
    }

    private MongoCollection<Document> getCollection() {
        return client.getDatabase("quarkus_db")
                .getCollection("author");
    }
}
