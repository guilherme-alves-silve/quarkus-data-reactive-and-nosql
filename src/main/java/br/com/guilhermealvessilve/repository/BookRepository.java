package br.com.guilhermealvessilve.repository;

import br.com.guilhermealvessilve.data.Book;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class BookRepository {

    private static final Logger LOGGER = Logger.getLogger(BookRepository.class);

    private final MySQLPool pool;

    @Inject
    public BookRepository(MySQLPool pool) {
        this.pool = pool;
    }

    public void onStartup(@Observes StartupEvent event) {

        LOGGER.info("Configuring database");

        pool.query("DROP TABLE IF EXISTS books;").execute()
            .flatMap(it -> pool.query("CREATE TABLE books(" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "pages INT NOT NULL" +
                    ");").execute())
            .flatMap(it -> pool.query("INSERT INTO books (title, pages) VALUES ('Quarkus Reactive Programming', 124), ('Java 11 OCP', 421);").execute())
            .await()
            .indefinitely();
    }

    public Multi<Book> getAll() {

        return pool.query("SELECT * FROM books;")
                .execute()
                .onItem()
                .produceMulti(rowSet -> Multi.createFrom()
                        .items(() -> StreamSupport.stream(rowSet.spliterator(), false)))
                .onItem()
                .apply(this::rowToBook);
    }

    public Uni<Book> get(Integer id) {

        return pool.preparedQuery("SELECT * FROM books WHERE id = ?;")
                .execute(Tuple.of(id))
                .onItem()
                .apply(RowSet::iterator)
                .onItem()
                .apply(it -> it.hasNext()? rowToBook(it.next()) : null);
    }

    public Uni<Integer> save(Book book) {

        return pool.preparedQuery("INSERT INTO books (title, pages) VALUES (?, ?);")
                    .execute(Tuple.of(
                            book.getTitle(),
                            book.getPages()
                    ))
                    .flatMap(r -> pool.query("SELECT LAST_INSERT_ID();").execute())
                    .onItem()
                    .apply(RowSet::iterator)
                    .onItem()
                    .apply(row -> row.hasNext()
                            ? row.next().getInteger(0)
                            : null);
    }

    @SuppressWarnings("unchecked")
    public Uni<Void> save(List<Book> books) {

        return pool.begin()
            .flatMap(transaction -> {

                List<Uni<RowSet<Row>>> inserts = books.stream()
                        .map(book -> transaction.preparedQuery("INSERT INTO books (title, pages) VALUES (?, ?);")
                                .execute(Tuple.of(book.getTitle(), book.getPages())))
                        .collect(toList());

                final var firstInsert = inserts.get(0);
                final var insertsPos = inserts.stream()
                        .skip(1)
                        .collect(toList());

                return firstInsert.and()
                        .unis(insertsPos)
                        .combinedWith(Function.identity())
                        .onItem()
                        .produceUni(x -> transaction.commit())
                        .onFailure()
                        .recoverWithUni(x -> transaction.rollback());
            });
    }

    public Uni<Boolean> delete(Integer id) {

        return pool.preparedQuery("DELETE FROM books WHERE id = ?;")
                .execute(Tuple.of(id))
                .onItem()
                .apply(rowSet -> rowSet.rowCount() == 1);
    }

    private Book rowToBook(Row row) {
        return new Book(
                row.getInteger("id"),
                row.getString("title"),
                row.getInteger("pages")
        );
    }
}
