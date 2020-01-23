package ru.hh;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;
import ru.hh.friends.Speech;
import ru.hh.search.Fields;

import java.util.List;

class AbstractSearchTest {

  Document speechToDoc(Speech speech) {
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
        0,
0
    );
  }

  void printResults(List<Document> docs) {
    for (int i = docs.size() - 1; i >= 0; i--) {
      Document d = docs.get(i);
      System.out.println(docToSpeech(d));
    }
  }

}
