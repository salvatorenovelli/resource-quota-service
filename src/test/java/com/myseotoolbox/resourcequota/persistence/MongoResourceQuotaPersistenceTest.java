package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.quota4j.model.Quota;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import static org.hamcrest.MatcherAssert.assertThat;

@DataMongoTest
@Import({MongoResourceQuotaPersistence.class})
class MongoResourceQuotaPersistenceTest {

    @Autowired
    MongoResourceQuotaPersistence sut;

    @Test
    void shouldNotSaveDuplicatedResources() {
        sut.save(new Quota("1234", "classname1", "state1"));
        sut.save(new Quota("1234", "classname2", "state2"));

        assertThat(sut.findById("1234").get().defaultState(), Matchers.is("state2"));
    }
}