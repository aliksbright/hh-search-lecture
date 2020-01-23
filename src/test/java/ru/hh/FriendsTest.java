package ru.hh;


import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
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
import org.junit.Test;
import static ru.hh.friends.FriendsDownloadKt.getSpeechList;
import ru.hh.friends.Speech;
import ru.hh.search.Fields;
import ru.hh.search.SearchConfig;

import java.io.IOException;
import java.util.List;


public class FriendsTest extends AbstractSearchTest {

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

    @Test
    public void whatsUp() throws IOException {
        // how many replicas
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
    public void searchInSeason() throws IOException {
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
