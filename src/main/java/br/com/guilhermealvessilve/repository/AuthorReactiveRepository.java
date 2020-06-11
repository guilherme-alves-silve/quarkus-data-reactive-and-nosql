package br.com.guilhermealvessilve.repository;

import br.com.guilhermealvessilve.data.Author;
import io.quarkus.mongodb.FindOptions;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.smallrye.mutiny.Uni;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class AuthorReactiveRepository {

    private final ReactiveMongoClient client;

    @Inject
    public AuthorReactiveRepository(final ReactiveMongoClient client) {
        this.client = client;
    }

    public Uni<Void> save(final Author author) {

        final var document = new Document()
                .append("name", author.getName())
                .append("address", author.getAddress());

        return getCollection().insertOne(document)
        .onItem()
        .ignore()
        .andContinueWithNull();
    }

    public Uni<List<Author>> getAll() {

        return getCollection().find()
                .map(this::documentToAuthor)
                .collectItems()
                .asList();
    }

    private ReactiveMongoCollection<Document> getCollection() {
        return client.getDatabase("quarkus_db")
                .getCollection("author");
    }

    public Uni<Author> get(String name) {

        return getCollection()
                .find(
                    new FindOptions()
                        .limit(1)
                        .filter(new BsonDocument("name", new BsonString(name)))
                )
                .map(this::documentToAuthor)
                .toUni();
    }

    private Author documentToAuthor(Document document) {
        return new Author(
                document.getString("name"),
                document.getString("address")
        );
    }
}
