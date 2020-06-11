package br.com.guilhermealvessilve.repository;

import br.com.guilhermealvessilve.data.Person;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@ApplicationScoped
public class PersonRepository {

    private static final String NAME_COL = "name";
    private static final String ADDRESS_COL = "address";

    private static final String PERSON_TABLE = "Persons";

    private final DynamoDbClient client;

    @Inject
    public PersonRepository(final DynamoDbClient client) {
        this.client = client;
    }

    public void save(final Person person) {
        client.putItem(putItemRequest(person));
    }

    public List<Person> getAll() {
        return client.scanPaginator(scanRequest())
                .items()
                .stream()
                .map(this::toPerson)
                .collect(toList());
    }

    public Person get(final String name) {
        final var item = client.getItem(getItemRequest(name))
                .item();
        return toPerson(item);
    }

    private Person toPerson(Map<String, AttributeValue> item) {
        return new Person(
                item.get(NAME_COL).s(),
                item.get(ADDRESS_COL).s()
        );
    }

    private ScanRequest scanRequest() {
        return ScanRequest.builder()
                .tableName(PERSON_TABLE)
                .attributesToGet(NAME_COL, ADDRESS_COL)
                .build();
    }

    private GetItemRequest getItemRequest(final String name) {
        final var item = new HashMap<String, AttributeValue>();
        item.put(NAME_COL, AttributeValue.builder().s(name).build());
        return GetItemRequest.builder()
                .tableName(PERSON_TABLE)
                .key(item)
                .attributesToGet(NAME_COL, ADDRESS_COL)
                .build();
    }

    private PutItemRequest putItemRequest(final Person person) {
        final var item = new HashMap<String, AttributeValue>();
        item.put(NAME_COL, AttributeValue.builder().s(person.getName()).build());
        item.put(ADDRESS_COL, AttributeValue.builder().s(person.getAddress()).build());
        return PutItemRequest.builder()
                .tableName(PERSON_TABLE)
                .item(item)
                .build();
    }
}
