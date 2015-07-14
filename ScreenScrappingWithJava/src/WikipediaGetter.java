import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class WikipediaGetter {

	public static void main(String[] args) throws IOException {
		List<String[]> coffee = new ArrayList<String[]>();
		
		Document doc = Jsoup.connect("http://en.wikipedia.org/wiki/List_of_coffee_varieties").get();
		
		Elements wikiTables = doc.select("table.wikitable");
		
		System.out.println("Wikitables size is " + wikiTables.size());
		
		for (Element table : wikiTables) {
			if (table.html().contains("<td>Arabica</td>")) {
				
				//table found
				Elements rows = table.select("tr");
				for (Element row : rows){
					Elements cells = row.select("td");
					if (cells.size() == 0)
						continue;
					String[] line = new String[cells.size()];
					for (int i = 0; i < line.length; i++) {
						line[i] = cells.get(i).text();
					}
					coffee.add(line);
				}
				break;
				
			}
		}
		
		for (String[] variety : coffee) {
			System.out.println("----- " + variety[0] + " -----");
			System.out.println("Arabica: " + variety[1]);
			System.out.println("Region(s): " + variety[2]);
			System.out.println("Comments: " + variety[3]);
		}
		
		

	}

}
