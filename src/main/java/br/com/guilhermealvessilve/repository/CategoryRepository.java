package br.com.guilhermealvessilve.repository;

import br.com.guilhermealvessilve.data.Category;
import org.jboss.logging.Logger;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.ResultCursor;
import org.neo4j.driver.exceptions.NoSuchRecordException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class CategoryRepository {

    private static final Logger LOGGER = Logger.getLogger(CategoryRepository.class);

    private final Driver driver;

    @Inject
    public CategoryRepository(final Driver driver) {
        this.driver = driver;
    }

    public CompletionStage<List<Category>> getAll() {
        final var session = driver.asyncSession();
        return session.runAsync("MATCH (c:Category) RETURN c ORDER BY c.id")
                .thenCompose(cursor -> cursor.listAsync(this::toCategory))
                .thenCompose(categories -> closeSessionAndReturn(session, categories))
                .whenComplete((categories, throwable) -> failedOperation(throwable, session));
    }

    public CompletionStage<Optional<Category>> findByIdOptional(Long id) {
        final var session = driver.asyncSession();
        return session.runAsync("MATCH (c:Category) WHERE id(c) = $id RETURN c",
                                Values.parameters("id", id))
                  .thenCompose(ResultCursor::singleAsync)
                  .handle((record, throwable) -> {
                        Optional<Category> optCategory;
                        if (throwable != null) {
                            Throwable source = (throwable instanceof CompletionException)
                                    ? throwable.getCause()
                                    : throwable;

                            if (source instanceof NoSuchRecordException) {
                                optCategory = Optional.empty();
                            } else {
                                throw new RuntimeException(source);
                            }
                        } else {
                            optCategory = Optional.of(toCategory(record));
                        }

                        return optCategory;
                  })
                .thenCompose(categories -> closeSessionAndReturn(session, categories))
                .whenComplete((categories, throwable) -> failedOperation(throwable, session));
    }

    public CompletionStage<Category> save(final Category category) {
        final var session = driver.asyncSession();
        return session.writeTransactionAsync(transaction ->
                transaction.runAsync("CREATE (c:Category {name: $name, section: $section, genre: $genre}) RETURN c",
                            Values.parameters(
                                    "name", category.getName(),
                                    "section", category.getSection(),
                                    "genre",category.getGenre()
                            ))
                            .thenCompose(ResultCursor::singleAsync)
        ).thenApply(this::toCategory)
        .thenCompose(savedCategory -> closeSessionAndReturn(session, savedCategory))
        .whenComplete((categories, throwable) -> failedOperation(throwable, session));
    }

    public CompletionStage<Boolean> delete(Long id) {
        final var session = driver.asyncSession();
        return session.writeTransactionAsync(transaction ->
            transaction.runAsync(
                "MATCH (c:Category) WHERE id(c) = $id DELETE c",
                Values.parameters("id", id)
            )
            .thenCompose(ResultCursor::consumeAsync)
            .thenApply(resultSummary -> resultSummary.counters().nodesDeleted() > 0)
        )
        .thenCompose(response -> closeSessionAndReturn(session, response))
        .whenComplete((response, throwable) -> failedOperation(throwable, session));
    }

    private Category toCategory(Record record) {
        final var node = record.get("c").asNode();
        return new Category(
                node.id(),
                node.get("name").asString(),
                node.get("section").asInt(),
                node.get("genre").asString()
        );
    }

    private <T> CompletionStage<T> closeSessionAndReturn(AsyncSession session, T object) {
        return session.closeAsync()
                .thenApply(it -> object);
    }

    private <T> void failedOperation(Throwable throwable, AsyncSession session) {

        if (throwable != null) {
            LOGGER.error(throwable);

            session.closeAsync()
                    .exceptionally(th -> {
                        LOGGER.error("The session was already closed: " + th.getMessage());
                        return null;
                    });

            throw new RuntimeException("Failed to list categories", throwable);
        }
    }
}
