package pl.zablocki.searcher.services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.springframework.stereotype.Service;
import pl.zablocki.searcher.dto.PageDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearcherService {


    private StandardAnalyzer analyzer;
    private Directory index;

    public SearcherService() {
        analyzer = new StandardAnalyzer();
        index = new RAMDirectory();
    }

    public void add(String url) {

        try {

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = null;
            writer = new IndexWriter(index, config);
            writer.commit();

            org.jsoup.nodes.Document page = null;

            page = Jsoup.connect(url).get();

            String title = page.title();
            String body = page.body().text();
            String date = LocalDateTime.now().toString();
            addDoc(writer, title, url, body, date);
        } catch (IOException e) {

            e.printStackTrace();
        }

    }


    private static void addDoc(IndexWriter w, String title, String url, String body, String date) throws IOException {
        Document doc = new Document();

        doc.add(new TextField("title", title, Field.Store.YES));

        doc.add(new StoredField("url", url));

        doc.add(new TextField("body", body, Field.Store.NO));

        doc.add(new StringField("date", date, Field.Store.YES));

        w.addDocument(doc);
        w.close();
    }

    public List<PageDto> search(String phrase) {
        try {
            Query q = new MultiFieldQueryParser(
                    new String[]{"title", "body", "date"},
                    analyzer).parse(phrase);
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, 10);
            ScoreDoc[] scoreDocs = docs.scoreDocs;
            List<PageDto> returnList = new ArrayList<>();

            System.out.println("Found " + scoreDocs.length + " hits.");
            for (int i = 0; i < scoreDocs.length; ++i) {
                int docId = scoreDocs[i].doc;
                Document d = searcher.doc(docId);
                returnList.add(new PageDto(d.get("title"), d.get("url")));
            }
            return returnList;

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
