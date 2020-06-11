package br.com.guilhermealvessilve.data;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "library")
public class Library extends PanacheMongoEntity {

    private String name;

    private String address;

    private String foundation;
}
