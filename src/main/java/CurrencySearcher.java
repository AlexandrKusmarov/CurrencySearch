import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class CurrencySearcher {
    private static Logger logger = LoggerFactory.getLogger(CurrencySearcher.class);
    private ArrayList<Currency> getCurrencyListByParsing() throws IOException, XMLStreamException {

        logger.info("Enter getCurrencyListByParsing() method.");
        URL url = new URL("http://www.cbr.ru/scripts/XML_daily.asp");
        InputStream input = url.openStream();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(input);
        Currency currency = null;
        ArrayList<Currency> currencyList = new ArrayList<>();

        try {
            int event = reader.getEventType();
            logger.info("Start reading the xml page...");
            while (true) {
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (reader.getName().getLocalPart().equals("Valute")) {
                            currency = new Currency();
                            if (reader.getAttributeName(0).getLocalPart().equals("ID")) {
                                currency.setId(reader.getAttributeValue(0));
                            }
                        } else if (reader.getName().getLocalPart().equals("NumCode") && currency != null) {
                            reader.next();
                            currency.setNumCode(reader.getText());
                        } else if (reader.getName().getLocalPart().equals("CharCode") && currency != null) {
                            reader.next();
                            currency.setCharCode(reader.getText());
                        } else if (reader.getName().getLocalPart().equals("Name") && currency != null) {
                            reader.next();
                            currency.setName(reader.getText());
                        } else if (reader.getName().getLocalPart().equals("Nominal") && currency != null) {
                            reader.next();
                            currency.setNominal(Integer.parseInt(reader.getText()));
                        } else if (reader.getName().getLocalPart().equals("Value") && currency != null) {
                            reader.next();
                            Double value = Double.parseDouble(reader.getText().replace(",", "."));
                            currency.setValue(value);
                            currencyList.add(currency);
                        }
                        break;
                }
                if (!reader.hasNext())
                    break;
                event = reader.next();
            }
            logger.info("Reading completed!");
        } finally {
            reader.close();
        }
        return currencyList;
    }

    private ArrayList<Currency> getConvertListToSingleNominal(ArrayList<Currency> currencies) {
        logger.info("Enter getConvertListToSingleNominal() method.");
        for (Currency currency : currencies) {
            if (currency.getNominal() > 1) {
                currency.setValue(currency.getValue() / currency.getNominal());
                currency.setNominal(1);
            }
        }
        return currencies;
    }

    private Currency getCheapestCurrency(ArrayList<Currency> currencies) {
        logger.info("Enter getCheapestCurrency() method.");
        return currencies.stream().min(Currency.comparator).get();
    }

    private Currency getMostExpensiveCurrency(ArrayList<Currency> currencies) {
        logger.info("Enter getMostExpensiveCurrency() method.");
        return currencies.stream().max(Currency.comparator).get();
    }

    public static void main(String[] args) {
        CurrencySearcher currencySearcher = new CurrencySearcher();
        ArrayList<Currency> singleNominalList;
        ArrayList<Currency> originalList;
        try {
            originalList = currencySearcher.getCurrencyListByParsing();
            logger.debug("The list contains {} currencies.",originalList.size());
            singleNominalList = currencySearcher.getConvertListToSingleNominal(originalList);

            Currency min = currencySearcher.getCheapestCurrency(singleNominalList);
            logger.debug("Самая дешевая валюта:  {}. Стоимость 1 номинала к рублю составляет: {} {}",
                    min.getName(),min.getValue(), min.getCharCode());
            System.out.printf("Самая дешевая валюта:  %s. Стоимость 1 номинала к рублю составляет: %s %s", min.getName(),min.getValue(), min.getCharCode());
            System.out.println();

            Currency max = currencySearcher.getMostExpensiveCurrency(singleNominalList);
            logger.debug("Самая дорогая валюта:  {}. Стоимость 1 номинала к рублю составляет: {} {}",
                    max.getName(),max.getValue(), max.getCharCode());
            System.out.printf("Самая дорогая валюта:  %s. Стоимость 1 номинала к рублю составляет: %s %s", max.getName(),max.getValue(), max.getCharCode());

        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
            logger.error(e.getMessage(),e);
        }
    }
}
