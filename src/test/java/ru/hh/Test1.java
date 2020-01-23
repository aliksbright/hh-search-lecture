package ru.hh;


import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import ru.hh.search.SearchConfig;

import java.io.IOException;


public class Test1 extends AbstractSearchTest {


    @Test
    public void howAboutRussianGrammar() throws IOException, ParseException {
        // analyzer
        Indexer indexer = new Indexer(SearchConfig.INDEX_PATH, new RussianAnalyzer());
    }

    private Document createSimpleDoc(String field, String text) {
        Document doc = new Document();
        Field pathField = new TextField(field, text, Field.Store.YES);
        doc.add(pathField);
        return doc;
    }

}
