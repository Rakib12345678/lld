package com.rocky.shorturl.service;


import com.rocky.shorturl.model.CounterSeq;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceService {

    private static final String SEQ_NAME = "url_seq";

    @Autowired
    private MongoOperations mongoOperations;

    @PostConstruct
    public void init() {
        // ensure counter document exists
        if (mongoOperations.findById(SEQ_NAME, CounterSeq.class) == null) {
            mongoOperations.save(new CounterSeq(SEQ_NAME, 0));
        }
    }

    public long getNextSequence() {
        Query query = new Query(Criteria.where("_id").is(SEQ_NAME));
        Update update = new Update().inc("seq", 1);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
        CounterSeq counter = mongoOperations.findAndModify(query, update, options, CounterSeq.class);
        return counter.getSeq();
    }
}