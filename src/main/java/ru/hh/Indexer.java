package ru.hh;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Indexer {

  private IndexWriter writer = null;

  public Indexer(String indexPath, Analyzer analyzer) throws IOException {
    Directory dir = FSDirectory.open(Paths.get(indexPath));
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    this.writer = new IndexWriter(dir, iwc);
  }

  public void addDoc(Document doc) throws IOException {
    writer.addDocument(doc);
  }

  public void closeWrite() throws IOException {
    writer.close();
  }
}
