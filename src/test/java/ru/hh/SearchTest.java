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


public class SearchTest {


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



    @Test
    public void createFriendsSeriesIndex() throws IOException {
        // downloading and creating...
        List<Speech> speeches = getSpeechList();

        Indexer indexer = new Indexer(SearchConfig.INDEX_PATH, new EnglishAnalyzer());
        speeches.forEach(speech -> {
            Document document = speechToDoc(speech);
            try {
                indexer.addDoc(document);
                System.out.println("indexing doc -> " + speech);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        indexer.closeWrite();
    }

    private Document speechToDoc(Speech speech) {
        Document doc = new Document();
        TextField text = new TextField(Fields.TEXT.name(), speech.getPhrase(), Field.Store.YES);
        doc.add(new StringField(Fields.CHARACTER.name(), speech.getCharacter(), Field.Store.YES));
        doc.add(new SortedDocValuesField(Fields.CHARACTER_DV.name(), new BytesRef(speech.getCharacter().strip())));

        doc.add(text);
        doc.add(new TextField(Fields.SEASON.name(), speech.getSeason(), Field.Store.YES));
        doc.add(new TextField(Fields.LINK.name(), speech.getLink(), Field.Store.YES));
        doc.add(new TextField(Fields.SERIES.name(), speech.getSeries(), Field.Store.YES));
        doc.add(new IntPoint(Fields.SERIES_NUMBER.name(), speech.getSeriesNumber()));
        doc.add(new IntPoint(Fields.SEASON_NUMBER.name(), speech.getSeasonNumber()));
        doc.add(new NumericDocValuesField(Fields.SEASON_NUMBER_DV.name(), speech.getSeasonNumber()));
        doc.add(new StoredField(Fields.SEASON_NUMBER_DV.name(), speech.getSeasonNumber()));
        return doc;
    }

    private Speech docToSpeech(Document document) {
        return new Speech(
            document.getField(Fields.LINK.name()).stringValue(),
            document.getField(Fields.SEASON.name()).stringValue(),
            document.getField(Fields.SERIES.name()).stringValue(),
            document.getField(Fields.CHARACTER.name()).stringValue(),
            document.getField(Fields.TEXT.name()).stringValue(),
            document.getField(Fields.SEASON_NUMBER.name()).numericValue().intValue(),
            document.getField(Fields.SERIES_NUMBER.name()).numericValue().intValue()
        );
    }

    private void printResults(List<Document> docs) {
        for (int i = docs.size() - 1; i >= 0; i--) {
            Document d = docs.get(i);
            System.out.println(docToSpeech(d));
        }
    }

    @Test
    public void whatsUp() throws IOException {
        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new EnglishAnalyzer());
        IndexReader reader = searcher.getReader();
        System.out.println("doc counts : " + reader.numDocs());

    }

    @Test
    public void bitching() throws IOException {
        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new EnglishAnalyzer());
        int helloFreq = searcher.getReader().docFreq(new Term(Fields.TEXT.name(), "bitch"));
        System.out.println("how many b*tches: " + helloFreq);
    }

    @Test
    public void bitching2() throws IOException {
        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new EnglishAnalyzer());
        Query query = new TermQuery(new Term(Fields.TEXT.name(), "bitch"));

        var docs = searcher.searchDocs(query);
        printResults(docs);
    }

    @Test
    public void namesStartBy() throws IOException {
        // prefix j
        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new EnglishAnalyzer());
        Query q = new PrefixQuery(new Term(Fields.CHARACTER.name(), "j"));

        System.out.println("count : " + searcher.search(q).totalHits);

        List<Document> docs = searcher.searchDocs(q);

        printResults(docs);
    }

    @Test
    public void howYouDoing() throws IOException, ParseException {
        // phrase
        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new EnglishAnalyzer());

        List<Document> docs = searcher.searchDocs("'how you doin'", Fields.TEXT.name());
        System.out.println(docs.size());

        printResults(docs);
    }

    @Test
    public void ohMyGoood() throws IOException {
        // fuuuzzy
        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new EnglishAnalyzer());
        FuzzyQuery query = new FuzzyQuery(new Term(Fields.TEXT.name(), "gawd"), 2);

        System.out.println("count : " + searcher.search(query).totalHits);

        List<Document> docs = searcher.searchDocs(query);
        printResults(docs);

    }

    @Test
    public void searchInSeason() throws IOException, ParseException {
        // range & and


        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new EnglishAnalyzer());
        Query seasonQuery = IntPoint.newRangeQuery(Fields.SEASON_NUMBER.name(), 1, 5);
        Query character = new TermQuery(new Term(Fields.CHARACTER.name(), "janice"));

        Query ohMy = new PhraseQuery(4, Fields.TEXT.name(), "oh", "my", "god");
        Query q = new BooleanQuery.Builder()
            .add(character, BooleanClause.Occur.MUST)
            .add(seasonQuery, BooleanClause.Occur.MUST)
            .add(ohMy, BooleanClause.Occur.MUST)
            .build();

        System.out.println(searcher.search(q).totalHits);

        printResults(searcher.searchDocs(q));

    }

    @Test
    public void oldMeansBottom() throws IOException {
        // sort
        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new EnglishAnalyzer());

        Sort sort = new Sort(new SortField(Fields.SEASON_NUMBER_DV.name(), SortField.Type.INT, true));
        Query seasonQuery = IntPoint.newRangeQuery(Fields.SEASON_NUMBER.name(), 1, 5);


        printResults(searcher.searchDocs(seasonQuery, 100, sort));
    }

    @Test
    public void letsTalkAboutHeroes() throws IOException {
        // how many replicas
        // group
        Searcher searcher = new Searcher(SearchConfig.INDEX_PATH, new EnglishAnalyzer());

        GroupingSearch groupingSearch = new GroupingSearch(Fields.CHARACTER_DV.name());
        Sort sort = new Sort(new SortField(Fields.CHARACTER_DV.name(), SortField.Type.STRING));

        groupingSearch.setGroupSort(sort);

        Query query = new MatchAllDocsQuery();

        TopGroups groupsResult = groupingSearch.search(searcher.getSearcher(), query, 0, 1000);
        System.out.println(groupsResult.groups.length);

        for (int i = 0; i < groupsResult.groups.length; i++) {
            var group = groupsResult.groups[i];
            Document doc = searcher.getSearcher().doc(group.scoreDocs[0].doc);
            System.out.println(doc.getField(Fields.CHARACTER.name()).stringValue() + " " + group.totalHits);
        }
    }




}
