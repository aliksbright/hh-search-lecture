package ru.hh;


import static java.util.Arrays.asList;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.util.BytesRef;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static ru.hh.friends.FriendsDownloadKt.getSpeechList;
import ru.hh.friends.Speech;
import ru.hh.search.Fields;
import ru.hh.search.SearchConfig;

import java.io.IOException;
import java.util.List;


public class Test1 extends AbstractSearchTest {


    @Test
    public void howAboutRussianGrammar() throws IOException, ParseException {
        // analyzer
        Indexer indexer = new Indexer(SearchConfig.INDEX_PATH, new RussianAnalyzer());
        asList("мировая война", "мировой океан", "мир труд май").forEach(doc -> {
            Document document = createSimpleDoc("content", doc);
            try {
                indexer.addDoc(document);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        indexer.closeWrite();

        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new RussianAnalyzer());
        long hits = searcher.search("мировые", "content").totalHits;

        assertEquals(hits, 2);

        List<Document> docs = searcher.searchDocs("мировые", "content");
        for (int i = docs.size() - 1; i >= 0; i--) {
            Document d = docs.get(i);
            System.out.println(d.getField("content").stringValue());
        }
    }

    private Document createSimpleDoc(String field, String text) {
        Document doc = new Document();
        Field pathField = new TextField(field, text, Field.Store.YES);
        doc.add(pathField);
        return doc;
    }

}
