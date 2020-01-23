package ru.hh;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.List;

public class Searcher {

  private IndexSearcher searcher;
  private Analyzer analyzer;
  private IndexReader reader;

  public Searcher(String index, Analyzer analyzer) throws IOException {

  }

  public IndexReader getReader() {
    return reader;
  }

  public IndexSearcher getSearcher() {
    return searcher;
  }

  public TopDocs search(Query q) throws IOException {
    return null;
  }

  public List<Document> searchDocs(Query q) throws IOException {
    return searchDocs(q, null, null);
  }

  public List<Document> searchDocs(Query q, Integer limit, Sort sort) throws IOException {
    return null;
  }

  public TopDocs search(String q) throws IOException, ParseException {
    return null;
  }

  public List<Document> searchDocs(String q) throws IOException, ParseException {
    return null;
  }


  public TopDocs search(String q, String field) throws IOException, ParseException {
    return null;
  }

  public List<Document> searchDocs(String q, String field) throws IOException, ParseException {
    return null;
  }
}


