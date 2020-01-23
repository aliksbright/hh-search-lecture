package ru.hh;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Searcher {

  private IndexSearcher searcher;
  private Analyzer analyzer;
  private IndexReader reader;

  public Searcher(String index, Analyzer analyzer) throws IOException {
    reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    searcher = new IndexSearcher(reader);
    this.analyzer = analyzer;
  }

  public IndexReader getReader() {
    return reader;
  }

  public IndexSearcher getSearcher() {
    return searcher;
  }

  public TopDocs search(Query q) throws IOException {
    TopDocs docs = searcher.search(q, 100);
    return docs;
  }

  public List<Document> searchDocs(Query q) throws IOException {
    return searchDocs(q, null, null);
  }

  public List<Document> searchDocs(Query q, Integer limit, Sort sort) throws IOException {
    List<Document> docs = new ArrayList<>();
    if (limit == null) {
      limit = 10000;
    }
    TopDocs doc;
    if (sort == null) {
      doc = searcher.search(q, limit);
    } else {
      doc = searcher.search(q, limit, sort);
    }
    for (int i = 0; i < doc.scoreDocs.length; i++) {
      Document d = searcher.doc(doc.scoreDocs[i].doc);
      docs.add(d);
    }

    return docs;
  }



  public TopDocs search(String q) throws IOException, ParseException {
    String[] fields = {"seasonNumber", "text", "character", "seriesNumber"};
    MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
    return search(parser.parse(q));
  }

  public List<Document> searchDocs(String q) throws IOException, ParseException {
    String[] fields = {"seasonNumber", "text", "character", "seriesNumber"};
    MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
    return searchDocs(parser.parse(q));
  }


  public TopDocs search(String q, String field) throws IOException, ParseException {
    QueryParser parser = new QueryParser(field, analyzer);
    return search(parser.parse(q));
  }

  public List<Document> searchDocs(String q, String field) throws IOException, ParseException {
    QueryParser parser = new QueryParser(field, analyzer);
    return searchDocs(parser.parse(q));
  }
}


