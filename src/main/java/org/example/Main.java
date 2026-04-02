package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

record Product(String title, String author, String price) {
}

public class Main {

    public static void main(String[] args) {
        String url = "https://www.bokus.com/";
        var bookList = scrapeBokus(url);

        bookList.forEach(Main::printProduct);
    }

    public static List<Product> scrapeBokus(String url) {
        List<Product> productsFound = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select(".product-item__info");

            for (Element el : elements) {
                Product p = extractProductData(el);
                if (!p.title().isEmpty()) {
                    productsFound.add(p);
                }
            }
        } catch (IOException e) {
            System.err.println("Kunde inte hämta sidan: " + e.getMessage());
        }
        return productsFound;
    }

    private static Product extractProductData(Element el) {
        String title = el.select(".product-item__title, .product-item__link").text().trim();
        String author = el.select(".product-item__authors").text().trim();
        String price = parsePrice(el);

        return new Product(title, author, price);
    }

    private static String parsePrice(Element el) {
        Optional<String> nice = Optional.of(el.select(".pricing__price--nice").text())
                .filter(s -> !s.isBlank());

//        Optional<String> overridden = Optional.of(el.select(".pricing__price--overridden").text())
//                .filter(s -> !s.isBlank());

        Optional<String> normal = Optional.of(el.select(".pricing__price").text())
                .filter(s -> !s.isBlank());

        return nice.map(p -> p + "kr (Kampanjpris)")
               // .or(() -> overridden.map(p -> p + "kr (overridden)"))
                .or(() -> normal.map(p -> p + "kr (Ordinarie)"))
                .orElse("Pris saknas");
    }

    private static void printProduct(Product p) {
        System.out.println("Bok:   " + p.title());
        if (!p.author().isEmpty()) System.out.println("Av:     " + p.author());
        System.out.println("Pris:   " + p.price());
        System.out.println("-------------------------");
    }
}